package io.elastest.security.tools.arachni;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArachniScan {

	@JsonProperty("id")
	private String scanId;

	
	public String getScanId() {
		return scanId;
	}

	public void setScanId(String scanId) {
		this.scanId = scanId;
	}

}
