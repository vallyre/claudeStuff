package com.mckesson.cmt.cmt_standardcode_gateway_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mckesson.cmt.cmt_standardcode_gateway_service.HLIApiException;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMemberHLIRes;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersHLIReq;
import com.mckesson.cmt.cmt_standardcode_gateway_service.contract.groups.GroupMembersReqDTO;
import com.mckesson.cmt.cmt_standardcode_gateway_service.utils.GroupMembersMapper;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class HLIRestAPIClient {

    private static final Logger log = LoggerFactory.getLogger(HLIRestAPIClient.class);
    private final WebClient webClient;
    private final int retryMaxAttempts;
    private final int retryBackoffMs;
    private final String authToken;

    public HLIRestAPIClient(
            WebClient webClient,
            @Value("${hli.api.retry-max-attempts:3}") int retryMaxAttempts,
            @Value("${hli.api.retry-backoff-ms:1000}") int retryBackoffMs,
            @Value("${hli.api.auth.token}") String authToken) {
        this.webClient = webClient;
        this.retryMaxAttempts = retryMaxAttempts;
        this.retryBackoffMs = retryBackoffMs;
        this.authToken = authToken;
    }

    public Mono<GroupMemberHLIRes> sendHliApiRequest(GroupMembersReqDTO groupMembersReq) {
        GroupMembersHLIReq payload = buildGroupMemberPayload(groupMembersReq);
        log.debug("payload::",payload.toString());

        return webClient.post()
                .uri("/v1/groups/members")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(GroupMemberHLIRes.class)
                .doOnSuccess(response -> log.info("Successfully received HLI response for requestId: {}",
                        groupMembersReq.getId()))
                .doOnError(error -> log.error("Error receiving HLI response for requestId: {}, Error: {}",
                        groupMembersReq.getId(), error.getMessage()))
                .retryWhen(Retry.backoff(retryMaxAttempts, Duration.ofMillis(retryBackoffMs))
                        .filter(this::isRetryableException)
                        .doBeforeRetry(
                                retrySignal -> log.warn("Retrying HLI Request for requestId: {}, attempt: {}",
                                        groupMembersReq.getId(), retrySignal.totalRetries() + 1)))
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("API Error creating ticket: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
                    return Mono.error(new HLIApiException(
                            "API Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error creating ticket: {}", e.getMessage());
                    return Mono.error(new HLIApiException("Unexpected error: " + e.getMessage()));
                });
    }

    private GroupMembersHLIReq buildGroupMemberPayload(GroupMembersReqDTO groupMemberReqest) {
        // Build the JSON payload for HLI API
        // This structure should be adapted based on the actual HLI API requirements
        return GroupMembersMapper.toHLIRequest(groupMemberReqest);
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            // Retry on server errors (5xx) and some client errors (429 - Too Many Requests)
            return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
        }
        // Also retry on connection issues
        return true;
    }
}