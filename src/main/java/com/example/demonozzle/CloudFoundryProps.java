package com.example.demonozzle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cf")
public class CloudFoundryProps {
    private String apiHost;
    private String clientId;
    private String clientSecret;
    private boolean skipSslValidation;
    private Firehose firehose = new Firehose();

    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isSkipSslValidation() {
        return skipSslValidation;
    }

    public void setSkipSslValidation(boolean skipSslValidation) {
        this.skipSslValidation = skipSslValidation;
    }

    public Firehose getFirehose() {
        return firehose;
    }

    public void setFirehose(Firehose firehose) {
        this.firehose = firehose;
    }

    class Firehose {
        private String subscriptionId;

        public String getSubscriptionId() {
            return subscriptionId;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }
    }
}
