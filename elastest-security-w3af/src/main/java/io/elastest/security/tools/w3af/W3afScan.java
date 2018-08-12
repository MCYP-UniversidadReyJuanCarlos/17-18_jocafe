package io.elastest.security.tools.w3af;

import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScan {

	@JsonProperty("id")
	private Long scanId;

	
	public Long getScanId() {
		return scanId;
	}

	public void setScanId(Long scanId) {
		this.scanId = scanId;
	}

}
