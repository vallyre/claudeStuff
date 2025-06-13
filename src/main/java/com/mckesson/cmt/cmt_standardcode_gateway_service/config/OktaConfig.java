package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OktaConfig {

    @Value("${okta.config.baseurl}")
    private String baseUrl;

    @Value("${okta.config.authserverid}")
    private String authServerId;
    
    @Value("${okta.config.audience}")
    private String audience;

    @Bean
    public WebClient webClient(ClientRegistrationRepository clientRegistrationRepository,
                              OAuth2AuthorizedClientRepository authorizedClientRepository) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrationRepository, authorizedClientRepository);
        
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build();
    }
}