package com.example.demonozzle;

import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudFoundryConfig {
    private final CloudFoundryProps props;

    public CloudFoundryConfig(CloudFoundryProps props) {
        this.props = props;
    }

    @Bean
    public ConnectionContext connectionContext() {
        return DefaultConnectionContext.builder()
                .skipSslValidation(this.props.isSkipSslValidation())
                .apiHost(this.props.getApiHost())
                .build();
    }

    @Bean
    public TokenProvider tokenProvider() {
        return ClientCredentialsGrantTokenProvider.builder()
                .clientId(this.props.getClientId())
                .clientSecret(this.props.getClientSecret())
                .build();
    }

    @Bean
    public DopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();
    }
}
