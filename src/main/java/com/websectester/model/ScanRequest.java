package com.websectester.model;

public class ScanRequest {

	private String url;
	
	private ScanAuth auth;


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

	public ScanAuth getAuth() {
		return auth;
	}

	public void setAuth(ScanAuth auth) {
		this.auth = auth;
	}
	
}
