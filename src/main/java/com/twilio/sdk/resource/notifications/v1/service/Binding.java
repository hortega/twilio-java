/**
 * This code was generated by
 * \ / _    _  _|   _  _
 *  | (_)\/(_)(_|\/| |(/_  v1.0.0
 *       /       /       
 */

package com.twilio.sdk.resource.notifications.v1.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.twilio.sdk.client.TwilioRestClient;
import com.twilio.sdk.converter.DateConverter;
import com.twilio.sdk.creator.notifications.v1.service.BindingCreator;
import com.twilio.sdk.deleter.notifications.v1.service.BindingDeleter;
import com.twilio.sdk.exception.ApiConnectionException;
import com.twilio.sdk.exception.ApiException;
import com.twilio.sdk.fetcher.notifications.v1.service.BindingFetcher;
import com.twilio.sdk.http.HttpMethod;
import com.twilio.sdk.http.Request;
import com.twilio.sdk.http.Response;
import com.twilio.sdk.reader.notifications.v1.service.BindingReader;
import com.twilio.sdk.resource.RestException;
import com.twilio.sdk.resource.SidResource;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Binding extends SidResource {
    private static final long serialVersionUID = 72408629014564L;

    public enum BindingType {
        APN("apn"),
        GCM("gcm");
    
        private final String value;
        
        private BindingType(final String value) {
            this.value = value;
        }
        
        public String toString() {
            return value;
        }
        
        /**
         * Generate a BindingType from a string.
         * @param value string value
         * @return generated BindingType
         */
        @JsonCreator
        public static BindingType forValue(final String value) {
            String normalized = value.replace("-", "_").toUpperCase();
            try {
                return BindingType.valueOf(normalized);
            } catch (RuntimeException e) {
        
                // Don't blow up of value does not exist
                return null;
            }
        }
    }

    /**
     * Create a BindingFetcher to execute fetch.
     * 
     * @param serviceSid The service_sid
     * @param sid The sid
     * @return BindingFetcher capable of executing the fetch
     */
    public static BindingFetcher fetch(final String serviceSid, 
                                       final String sid) {
        return new BindingFetcher(serviceSid, sid);
    }

    /**
     * Create a BindingDeleter to execute delete.
     * 
     * @param serviceSid The service_sid
     * @param sid The sid
     * @return BindingDeleter capable of executing the delete
     */
    public static BindingDeleter delete(final String serviceSid, 
                                        final String sid) {
        return new BindingDeleter(serviceSid, sid);
    }

    /**
     * Create a BindingCreator to execute create.
     * 
     * @param serviceSid The service_sid
     * @param endpoint The endpoint
     * @param identity The identity
     * @param bindingType The binding_type
     * @param address The address
     * @return BindingCreator capable of executing the create
     */
    public static BindingCreator create(final String serviceSid, 
                                        final String endpoint, 
                                        final String identity, 
                                        final Binding.BindingType bindingType, 
                                        final String address) {
        return new BindingCreator(serviceSid, endpoint, identity, bindingType, address);
    }

    /**
     * Create a BindingReader to execute read.
     * 
     * @param serviceSid The service_sid
     * @return BindingReader capable of executing the read
     */
    public static BindingReader read(final String serviceSid) {
        return new BindingReader(serviceSid);
    }

    /**
     * Converts a JSON String into a Binding object using the provided ObjectMapper.
     * 
     * @param json Raw JSON String
     * @param objectMapper Jackson ObjectMapper
     * @return Binding object represented by the provided JSON
     */
    public static Binding fromJson(final String json, final ObjectMapper objectMapper) {
        // Convert all checked exceptions to Runtime
        try {
            return objectMapper.readValue(json, Binding.class);
        } catch (final JsonMappingException | JsonParseException e) {
            throw new ApiException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new ApiConnectionException(e.getMessage(), e);
        }
    }

    /**
     * Converts a JSON InputStream into a Binding object using the provided
     * ObjectMapper.
     * 
     * @param json Raw JSON InputStream
     * @param objectMapper Jackson ObjectMapper
     * @return Binding object represented by the provided JSON
     */
    public static Binding fromJson(final InputStream json, final ObjectMapper objectMapper) {
        // Convert all checked exceptions to Runtime
        try {
            return objectMapper.readValue(json, Binding.class);
        } catch (final JsonMappingException | JsonParseException e) {
            throw new ApiException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new ApiConnectionException(e.getMessage(), e);
        }
    }

    private final String sid;
    private final String accountSid;
    private final String serviceSid;
    private final DateTime dateCreated;
    private final DateTime dateUpdated;
    private final String notificationProtocolVersion;
    private final String endpoint;
    private final String identity;
    private final String bindingType;
    private final String address;
    private final List<String> tags;
    private final URI url;

    @JsonCreator
    private Binding(@JsonProperty("sid")
                    final String sid, 
                    @JsonProperty("account_sid")
                    final String accountSid, 
                    @JsonProperty("service_sid")
                    final String serviceSid, 
                    @JsonProperty("date_created")
                    final String dateCreated, 
                    @JsonProperty("date_updated")
                    final String dateUpdated, 
                    @JsonProperty("notification_protocol_version")
                    final String notificationProtocolVersion, 
                    @JsonProperty("endpoint")
                    final String endpoint, 
                    @JsonProperty("identity")
                    final String identity, 
                    @JsonProperty("binding_type")
                    final String bindingType, 
                    @JsonProperty("address")
                    final String address, 
                    @JsonProperty("tags")
                    final List<String> tags, 
                    @JsonProperty("url")
                    final URI url) {
        this.sid = sid;
        this.accountSid = accountSid;
        this.serviceSid = serviceSid;
        this.dateCreated = DateConverter.iso8601DateTimeFromString(dateCreated);
        this.dateUpdated = DateConverter.iso8601DateTimeFromString(dateUpdated);
        this.notificationProtocolVersion = notificationProtocolVersion;
        this.endpoint = endpoint;
        this.identity = identity;
        this.bindingType = bindingType;
        this.address = address;
        this.tags = tags;
        this.url = url;
    }

    /**
     * Returns The The sid.
     * 
     * @return The sid
     */
    public final String getSid() {
        return this.sid;
    }

    /**
     * Returns The The account_sid.
     * 
     * @return The account_sid
     */
    public final String getAccountSid() {
        return this.accountSid;
    }

    /**
     * Returns The The service_sid.
     * 
     * @return The service_sid
     */
    public final String getServiceSid() {
        return this.serviceSid;
    }

    /**
     * Returns The The date_created.
     * 
     * @return The date_created
     */
    public final DateTime getDateCreated() {
        return this.dateCreated;
    }

    /**
     * Returns The The date_updated.
     * 
     * @return The date_updated
     */
    public final DateTime getDateUpdated() {
        return this.dateUpdated;
    }

    /**
     * Returns The The notification_protocol_version.
     * 
     * @return The notification_protocol_version
     */
    public final String getNotificationProtocolVersion() {
        return this.notificationProtocolVersion;
    }

    /**
     * Returns The The endpoint.
     * 
     * @return The endpoint
     */
    public final String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Returns The The identity.
     * 
     * @return The identity
     */
    public final String getIdentity() {
        return this.identity;
    }

    /**
     * Returns The The binding_type.
     * 
     * @return The binding_type
     */
    public final String getBindingType() {
        return this.bindingType;
    }

    /**
     * Returns The The address.
     * 
     * @return The address
     */
    public final String getAddress() {
        return this.address;
    }

    /**
     * Returns The The tags.
     * 
     * @return The tags
     */
    public final List<String> getTags() {
        return this.tags;
    }

    /**
     * Returns The The url.
     * 
     * @return The url
     */
    public final URI getUrl() {
        return this.url;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        Binding other = (Binding) o;
        
        return Objects.equals(sid, other.sid) && 
               Objects.equals(accountSid, other.accountSid) && 
               Objects.equals(serviceSid, other.serviceSid) && 
               Objects.equals(dateCreated, other.dateCreated) && 
               Objects.equals(dateUpdated, other.dateUpdated) && 
               Objects.equals(notificationProtocolVersion, other.notificationProtocolVersion) && 
               Objects.equals(endpoint, other.endpoint) && 
               Objects.equals(identity, other.identity) && 
               Objects.equals(bindingType, other.bindingType) && 
               Objects.equals(address, other.address) && 
               Objects.equals(tags, other.tags) && 
               Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid,
                            accountSid,
                            serviceSid,
                            dateCreated,
                            dateUpdated,
                            notificationProtocolVersion,
                            endpoint,
                            identity,
                            bindingType,
                            address,
                            tags,
                            url);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("sid", sid)
                          .add("accountSid", accountSid)
                          .add("serviceSid", serviceSid)
                          .add("dateCreated", dateCreated)
                          .add("dateUpdated", dateUpdated)
                          .add("notificationProtocolVersion", notificationProtocolVersion)
                          .add("endpoint", endpoint)
                          .add("identity", identity)
                          .add("bindingType", bindingType)
                          .add("address", address)
                          .add("tags", tags)
                          .add("url", url)
                          .toString();
    }
}