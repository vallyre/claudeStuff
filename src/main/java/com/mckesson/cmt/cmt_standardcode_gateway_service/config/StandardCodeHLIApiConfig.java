package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class StandardCodeHLIApiConfig {

    @Value("${hli.api.url}")
    private String hliApiUrl;

    @Value("${hli.api.auth.token:}")
    private String authToken;

    @Value("${hli.api.batch-size:50}")
    private int batchSize;

    @Value("${hli.api.delay-ms:500}")
    private int delayMs;

    @Value("${hli.api.max-concurrent-requests:5}")
    private int maxConcurrentRequests;

    @Value("${hli.api.retry-max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${hli.api.retry-backoff-ms:1000}")
    private int retryBackoffMs;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                .responseTimeout(Duration.ofMillis(30000))
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        // Increase buffer size for larger payloads
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(hliApiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    @Bean
    public String authToken() {
        return authToken;
    }

    @Bean
    public int batchSize() {
        return batchSize;
    }

    @Bean
    public int delayMs() {
        return delayMs;
    }

    @Bean
    public int maxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    @Bean
    public int retryMaxAttempts() {
        return retryMaxAttempts;
    }

    @Bean
    public int retryBackoffMs() {
        return retryBackoffMs;
    }
}