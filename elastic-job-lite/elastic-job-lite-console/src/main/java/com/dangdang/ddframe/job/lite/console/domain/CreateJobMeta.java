package com.dangdang.ddframe.job.lite.console.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CreateJobMeta {

    @NotNull
    private String registryCenterName;

    @NotNull
    private String jobClass;

    @NotNull
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
    private String callback_method;

    @NotNull
    private String callback_data;

}
