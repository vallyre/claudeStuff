package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class BlobStorageConfig {

    private final String filePath;

    public BlobStorageConfig(@Value("${standardcodes.localDirectory}") String filePath) {
        this.filePath = filePath;
    }
}
