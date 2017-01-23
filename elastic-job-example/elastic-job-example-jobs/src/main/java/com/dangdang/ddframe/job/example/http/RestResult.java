package com.dangdang.ddframe.job.example.http;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RestResult<T> {
	private int status;
	private boolean sendSuccess;
	private Map<String,String> headers = new HashMap<String,String>();
	private T content;
}
