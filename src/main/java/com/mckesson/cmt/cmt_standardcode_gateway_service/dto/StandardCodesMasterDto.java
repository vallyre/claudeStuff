package com.mckesson.cmt.cmt_standardcode_gateway_service.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class StandardCodesMasterDto {
    private String id;
    private String resourceType;
    private String url;
    private String version;
    private String name;
    private String title;
    private String status;
    private Boolean experimental;
    private String date;
    private String publisher;
    private String description;
    private String purpose;
    private String approvalDate;
    private String lastReviewDate;
    private String lastUpdated;

}
