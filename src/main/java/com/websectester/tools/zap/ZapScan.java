package com.websectester.tools.zap;

import com.fasterxml.jackson.annotation.JsonSetter;

public class ZapScan {

	private Long scanId;


	@JsonSetter("scan")
	public void setScanId(Long scanId) {
		if (this.scanId == null) {
			this.scanId = scanId;
		}
	}
	
	@JsonSetter("scanAsUser")
	public void setAuthScanId(Long authScanId) {
		if (this.scanId == null) {
			this.scanId = authScanId;
		}
	}
	
	public Long getScanId() {
		return scanId;
	}

}
