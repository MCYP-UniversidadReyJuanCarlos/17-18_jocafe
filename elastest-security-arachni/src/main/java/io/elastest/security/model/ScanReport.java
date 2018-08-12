package io.elastest.security.model;

import java.util.ArrayList;
import java.util.List;

public class ScanReport {

	private String progress;
	
	private String status;
	
	private List<ScanAlert> alerts = new ArrayList<>();

	
	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<ScanAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<ScanAlert> alerts) {
		this.alerts = alerts;
	}
}
