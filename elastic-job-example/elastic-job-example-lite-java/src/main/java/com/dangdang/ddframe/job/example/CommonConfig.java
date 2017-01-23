package com.dangdang.ddframe.job.example;

/**
 * Created by liuheng3 on 2017/1/22.
 */
public class CommonConfig {

    public static final String ZOOKEEPER_CONNECTION_STRING = "10.64.7.106:2181";

    public static final String JOB_NAMESPACE = "elastic-job-scheduler-test";

    // switch to MySQL by yourself
//    public static final String EVENT_RDB_STORAGE_DRIVER = "com.mysql.jdbc.Driver";
//    public static final String EVENT_RDB_STORAGE_URL = "jdbc:mysql://localhost:3306/elastic_job_log";

    public static final String EVENT_RDB_STORAGE_DRIVER = "org.h2.Driver";

    public static final String EVENT_RDB_STORAGE_URL = "jdbc:h2:mem:job_event_storage";

    public static final String EVENT_RDB_STORAGE_USERNAME = "sa";

    public static final String EVENT_RDB_STORAGE_PASSWORD = "";

    public static final String digest = "";

    public static final String jobCreateConfigNode="jobCreateConfigNode";

}
