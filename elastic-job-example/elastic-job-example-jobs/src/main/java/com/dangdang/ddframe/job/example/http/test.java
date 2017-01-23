package com.dangdang.ddframe.job.example.http;

import org.springframework.http.HttpMethod;

/**
 * Created by liuheng3 on 2017/1/23.
 */
public class test {

    public static void main(String[] args){

        RestResult restResult=RestClient.getInstance().execute(HttpMethod.GET,"http://www.baidu.com",null,String.class,null);
        System.out.println(restResult);
    }
}
