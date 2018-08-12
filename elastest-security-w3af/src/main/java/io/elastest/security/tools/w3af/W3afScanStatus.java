package io.elastest.security.tools.w3af;

import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScanStatus {
	
	public static final String SCAN_STATUS_DONE = "Stopped";
	
	private String status;
	
	@JsonProperty("is_running")
	private Boolean isRunning;
	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}	

	public Boolean getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}

	public String getProgress() {
		String progress = "";
		
		if ((isRunning != null) && !isRunning.booleanValue()) {
			if (SCAN_STATUS_DONE.equalsIgnoreCase(status)) {
				progress = "100";
			}
		}
		
		return progress;
	}

}
