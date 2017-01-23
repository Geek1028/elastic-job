package com.dangdang.ddframe.job.example.http;

import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class CreateJobMeta {

    private String registryCenterName;

    private String jobClass;

    private Integer shardingTotalCount;

    private Integer type;

    private String cron;

    private String data;

    private String callback_url;

    private HttpMethod callback_method;

    private String callback_data;

}
