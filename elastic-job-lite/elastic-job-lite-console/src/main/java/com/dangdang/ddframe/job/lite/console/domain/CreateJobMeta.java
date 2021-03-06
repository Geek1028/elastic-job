package com.dangdang.ddframe.job.lite.console.domain;

import lombok.Data;
import org.springframework.http.HttpMethod;

import javax.validation.constraints.NotNull;

@Data
public class CreateJobMeta {

    @NotNull
    private String registryCenterName;

    private String jobClass;

    private Integer shardingTotalCount;

    @NotNull
    private Integer type;

    @NotNull
    private String cron;

    @NotNull
    private String data;

    @NotNull
    private String callback_url;

    @NotNull
    private HttpMethod callback_method;

    @NotNull
    private String callback_data;

}
