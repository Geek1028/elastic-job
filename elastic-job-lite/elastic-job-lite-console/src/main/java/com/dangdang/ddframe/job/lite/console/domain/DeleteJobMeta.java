package com.dangdang.ddframe.job.lite.console.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DeleteJobMeta {

    @NotNull
    private String registryCenterName;

    @NotNull
    private String jobName;


}
