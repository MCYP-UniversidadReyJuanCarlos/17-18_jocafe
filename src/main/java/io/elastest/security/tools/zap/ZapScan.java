package io.elastest.security.tools.zap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZapScan {

	@JsonProperty("scan")
	private Long scanId;

	
	public Long getScanId() {
		return scanId;
	}

	public void setScanId(Long scanId) {
		this.scanId = scanId;
	}

}
