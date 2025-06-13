package com.mckesson.cmt.cmt_standardcode_gateway_service.component;

//import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class BatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(BatchProcessor.class);

    public <T, R> void processBatches(
            List<T> items,
            int batchSize,
            int delayMs,
            Function<T, Mono<R>> processor) {

        if (items.isEmpty()) {
            log.info("No items to process");
            return;
        }

        log.info("Processing {} items in batches of {}", items.size(), batchSize);

        // Split the list into batches
        List<List<T>> batches = getBatches(items, batchSize);
        log.info("Split into {} batches", batches.size());

        // Process each batch with delay between batches
        Flux.fromIterable(batches)
                .concatMap(batch -> {
                    log.info("Processing batch of {} items", batch.size());

                    // Process items in the batch concurrently
                    return Flux.fromIterable(batch)
                            .flatMap(processor, 10) // Limit concurrency
                            .collectList()
                            .doOnSuccess(results -> log.info("Completed batch of {} items", batch.size()))
                            .then(Mono.delay(Duration.ofMillis(delayMs)))
                            .then();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .blockLast();

        log.info("Completed processing all batches");
    }

    private <T> List<List<T>> getBatches(List<T> items, int batchSize) {
        return Flux.fromIterable(items)
                .buffer(batchSize)
                .collect(Collectors.toList())
                .block();
    }
}
