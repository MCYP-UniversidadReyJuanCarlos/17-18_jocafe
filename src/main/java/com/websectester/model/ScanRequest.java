package com.websectester.model;

public class ScanRequest {

	private String url;


	public ScanRequest() {
	}
	
	public ScanRequest(String url) {
		super();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
