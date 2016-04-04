package com.twilio.client;

import com.google.common.base.Joiner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a token that will grant someone access to resources
 * within Twilio.
 */
public class TwilioCapability extends CapabilityToken {

    private String accountSid;
    private String authToken;
    private List<String> scopes;

    // Default Data
    // private String defaultClientName = null;

    // Incoming Parameter holding until generate token time
    private boolean buildIncomingScope = false;
    private String incomingClientName = null;

    // Outgoing Paramater holding until generate token time
    private boolean buildOutgoingScope = false;
    private String appSid = null;
    private String outgoingClientName = null;
    private Map<String, String> outgoingParams = null;

    /**
     * Create a new TwilioCapability with zero permissions. Next steps are to
     * grant access to resources by configuring this token through the functions
     * allowXXXX.
     *
     * @param accountSid
     *            the account sid to which this token is granted access
     * @param authToken
     *            the secret key used to sign the token. Note, this auth token
     *            is not visible to the user of the token.
     */
    public TwilioCapability(String accountSid, String authToken) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.scopes = new ArrayList<>();
    }

    private String buildScopeString(String serivce, String priviledge,
                                    Map<String, String> params) {

        StringBuilder scope = new StringBuilder();

        scope.append("scope:");
        scope.append(serivce);
        scope.append(":");
        scope.append(priviledge);

        if (params != null && params.size() > 0) {
            String paramsJoined = generateParamString(params);

            scope.append("?");
            scope.append(paramsJoined);
        }

        return scope.toString();
    }

    /**
     * Allow the user of this token to make outgoing connections.
     *
     * @param appSid
     *            the application to which this token grants access
     */
    public void allowClientOutgoing(String appSid) {
        allowClientOutgoing(appSid, null);
    }

    /**
     * Allow the user of this token to make outgoing connections.
     *
     * @param appSid
     *            the application to which this token grants access
     * @param params
     *            signed parameters that the user of this token cannot
     *            overwrite.
     */
    public void allowClientOutgoing(String appSid, Map<String, String> params) {
        this.buildOutgoingScope = true;
        this.outgoingParams = params;
        this.appSid = appSid;
    }

    private String generateParamString(Map<String, String> params) {
        List<String> keyValues = new ArrayList<String>();
        for (String key : params.keySet()) {
            String value = params.get(key);

            try {
                key = URLEncoder.encode(key, "UTF-8");
                value = URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                continue;
            }

            keyValues.add(key + "=" + value);
        }

        return Joiner.on("&").join(keyValues);
    }

    /**
     * If the user of this token should be allowed to accept incoming
     * connections then configure the TwilioCapability through this method and
     * specify the client name.
     *
     * @param clientName name of the Twilio Client
     */
    public void allowClientIncoming(String clientName) {
        // Save the default client name
        this.incomingClientName = clientName;
        this.buildIncomingScope = true;
    }

    /**
     * Allow the user of this token to access their event stream.
     *
     * @param filters key/value filters to apply to the event stream
     */
    public void allowEventStream(Map<String, String> filters) {
        Map<String, String> value = new LinkedHashMap<>();
        value.put("path", "/2010-04-01/Events");
        if (filters != null) {
            String paramsJoined = generateParamString(filters);
            value.put("params", paramsJoined);
        }

        this.scopes.add(this.buildScopeString("stream", "subscribe", value));
    }

    /**
     * Generates a new token based on the credentials and permissions that
     * previously has been granted to this token.
     *
     * @return the newly generated token that is valid for 3600 seconds
     * @throws DomainException if unable to generate token
     */
    public String generateToken() throws DomainException {
        return generateToken(3600);
    }

    /**
     * Generates a new token based on the credentials and permissions that
     * previously has been granted to this token.
     *
     * @param ttl the number of seconds before this token expires
     * @return the newly generated token that is valid for ttl seconds
     * @throws DomainException if unable to generate token
     */
    public String generateToken(long ttl) throws DomainException {

        // Build these scopes lazily when we generate tokens so we know
        // if we have a default or incoming client name to use
        buildIncomingScope();
        buildOutgoingScope();

        try {
            Map<String, Object> payload = new LinkedHashMap<String, Object>();
            payload.put("iss", this.accountSid);
            payload.put("exp", String.valueOf(((new Date()).getTime() / 1000) + ttl));
            payload.put("scope", Joiner.on(" ").join(this.scopes));

            return jwtEncode(payload, this.authToken);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DomainException(e);
        }
    }

    private void buildOutgoingScope() {
        if (this.buildOutgoingScope) {
            return;
        }

        Map<String, String> values = new HashMap<>();
        values.put("appSid", appSid);

        // Outgoing takes precedence over any incoming name which
        // takes precedence over the default client name. however,
        // we do accept a null clientName
        if (this.outgoingClientName != null) {
            values.put("clientName", this.outgoingClientName);
        } else if (this.incomingClientName != null) {
            values.put("clientName", this.incomingClientName);
        }

        // Build outgoing scopes
        if (this.outgoingParams != null) {
            String paramsJoined = generateParamString(this.outgoingParams);
            values.put("appParams", paramsJoined);
        }

        this.scopes.add(this.buildScopeString("client", "outgoing", values));
    }

    private void buildIncomingScope() {
        if (this.buildIncomingScope) {
            return;
        }

        Map<String, String> value = new LinkedHashMap<String, String>();

        // incoming name which takes precedence over the default client
        // name. however, we do NOT accept a null clientName here
        if (this.incomingClientName != null) {
            value.put("clientName", this.incomingClientName);
        } else {
            throw new IllegalStateException("No client name set");
        }

        this.scopes.add(this.buildScopeString("client", "incoming", value));
    }
}
