package com.twilio.jwt.validation;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.twilio.http.HttpMethod;
import com.twilio.jwt.Jwt;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ValidationToken extends Jwt {

    private static final HashFunction HASH_FUNCTION = Hashing.sha256();
    private static final String CTY = "twilio-pkrv;v=1";
    private static final String NEW_LINE = "\n";

    private final String accountSid;
    private final String credentialSid;
    private final String signingKeySid;
    private final String method;
    private final String uri;
    private final String queryString;
    private final Header[] headers;
    private final List<String> signedHeaders;
    private final String requestBody;

    private ValidationToken(Builder b) {
        super(
            SignatureAlgorithm.RS256,
            b.privateKey,
            b.credentialSid,
            new Date(new Date().getTime() + b.ttl * 1000)
        );
        this.accountSid = b.accountSid;
        this.credentialSid = b.credentialSid;
        this.signingKeySid = b.signingKeySid;
        this.method = b.method;
        this.uri = b.uri;
        this.queryString = b.queryString;
        this.headers = b.headers;
        this.signedHeaders = b.signedHeaders;
        this.requestBody = b.requestBody;
    }

    @Override
    public Map<String, Object> getHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("cty", CTY);
        headers.put("kid", this.credentialSid);
        return headers;
    }

    @Override
    public Map<String, Object> getClaims() {
        Map<String, Object> payload = new HashMap<>();

        payload.put("iss", this.signingKeySid);
        payload.put("sub", this.accountSid);

        // Sort the signed headers
        Collections.sort(signedHeaders);
        List<String> lowercaseSignedHeaders = Lists.transform(signedHeaders, LOWERCASE_STRING);
        String includedHeaders = Joiner.on(";").join(lowercaseSignedHeaders);
        payload.put("hrh", includedHeaders);

        // Add the method and uri
        StringBuilder signature = new StringBuilder();
        signature.append(method).append(NEW_LINE);
        signature.append(uri).append(NEW_LINE);

        // Get the query args, sort and rejoin
        String[] queryArgs = queryString.split("&");
        Arrays.sort(queryArgs);
        String sortedQueryString = Joiner.on("&").join(queryArgs);
        signature.append(sortedQueryString).append(NEW_LINE);

        // Normalize all the headers
        Header[] lowercaseHeaders = LOWERCASE_KEYS.apply(headers);
        Map<String, List<String>> combinedHeaders = COMBINE_HEADERS.apply(lowercaseHeaders);

        // Add the headers that we care about
        for (String header : lowercaseSignedHeaders) {
            String lowercase = header.toLowerCase().trim();

            if (combinedHeaders.containsKey(lowercase)) {
                List<String> values = combinedHeaders.get(lowercase);
                Collections.sort(values);

                signature.append(lowercase)
                    .append(":")
                    .append(Joiner.on(',').join(values))
                    .append(NEW_LINE);
            }
        }
        signature.append(NEW_LINE);

        // Mark the headers that we care about
        signature.append(includedHeaders).append(NEW_LINE);

        // Hash and hex the request payload
        if (!Strings.isNullOrEmpty(requestBody)) {
            String hashedPayload = HASH_FUNCTION.hashString(requestBody, Charsets.UTF_8).toString();
            signature.append(hashedPayload);
        }

        // Hash and hex the canonical request
        String hashedSignature = HASH_FUNCTION.hashString(signature.toString(), Charsets.UTF_8).toString();
        payload.put("rqh", hashedSignature);

        return payload;
    }

    public static ValidationToken fromHttpRequest(
        String accountSid,
        String credentialSid,
        String signingKeySid,
        PrivateKey privateKey,
        HttpRequest request,
        List<String> signedHeaders
    ) throws IOException {
        Builder builder = new Builder(accountSid, credentialSid, signingKeySid, privateKey);

        String method = request.getRequestLine().getMethod();
        builder.method(method);

        String uri = request.getRequestLine().getUri();
        if (uri.contains("?")) {
            String[] uriParts = uri.split("\\?");
            builder.uri(uriParts[0]);
            builder.queryString(uriParts[1]);
        } else {
            builder.uri(uri);
        }

        builder.headers(request.getAllHeaders());
        builder.signedHeaders(signedHeaders);

        if (HttpMethod.POST.toString().equals(method.toUpperCase())) {
            HttpEntity entity = ((HttpEntityEnclosingRequest)request).getEntity();
            builder.requestBody(CharStreams.toString(new InputStreamReader(entity.getContent(), Charsets.UTF_8)));
        }

        return builder.build();
    }

    private static Function<Header[], Map<String, List<String>>> COMBINE_HEADERS = new Function<Header[], Map<String, List<String>>>() {
        @Override
        public Map<String, List<String>> apply(Header[] headers) {
            Map<String, List<String>> combinedHeaders = new HashMap<>();

            for (Header header : headers)  {
                if (combinedHeaders.containsKey(header.getName())) {
                    combinedHeaders.get(header.getName()).add(header.getValue());
                } else {
                    combinedHeaders.put(header.getName(), Lists.newArrayList(header.getValue()));
                }
            }

            return combinedHeaders;
        }
    };

    private static Function<Header[], Header[]> LOWERCASE_KEYS = new Function<Header[], Header[]>() {
        @Override
        public Header[] apply(Header[] headers) {
            Header[] lowercaseHeaders = new Header[headers.length];
            for (int i = 0; i < headers.length; i++) {
                lowercaseHeaders[i] = new BasicHeader(headers[i].getName().toLowerCase(), headers[i].getValue());
            }

            return lowercaseHeaders;
        }
    };

    private static Function<String, String> LOWERCASE_STRING = new Function<String, String>() {
        @Override
        public String apply(String s) {
            return s.toLowerCase();
        }
    };

    public static class Builder {

        private String accountSid;
        private String credentialSid;
        private String signingKeySid;
        private PrivateKey privateKey;
        private String method;
        private String uri;
        private String queryString = "";
        private Header[] headers;
        private List<String> signedHeaders = Collections.emptyList();
        private String requestBody = "";
        private int ttl = 300;

        public Builder(String accountSid, String credentialSid, String signingKeySid, PrivateKey privateKey) {
            this.accountSid = accountSid;
            this.credentialSid = credentialSid;
            this.signingKeySid = signingKeySid;
            this.privateKey = privateKey;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder headers(Header[] headers) {
            this.headers = headers;
            return this;
        }

        public Builder signedHeaders(List<String> signedHeaders) {
            this.signedHeaders = signedHeaders;
            return this;
        }

        public Builder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder ttl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public ValidationToken build() {
            return new ValidationToken(this);
        }
    }

}
