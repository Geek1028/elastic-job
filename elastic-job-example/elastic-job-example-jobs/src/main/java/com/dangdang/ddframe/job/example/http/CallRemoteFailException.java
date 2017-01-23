package com.dangdang.ddframe.job.example.http;

public class CallRemoteFailException extends RuntimeException{

	private static final long serialVersionUID = 3777575079333633346L;
	
	private String remote;
	
	public CallRemoteFailException(String remote, String message, Throwable t){
		super(message,t);
		this.remote = remote;
	}
	public String toString(){
		return "call "+remote+" failed";
	}
}
