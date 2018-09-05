package com.websectester.tools.zap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZapResult {

	// {"Result":"OK"}
	
	public static final String RESULT_OK = "OK";
	
	@JsonProperty("Result")
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public boolean isOK() {
		return RESULT_OK.equalsIgnoreCase(result);
	}
}
