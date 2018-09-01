package com.websectester.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class ScanReport {

	@JsonUnwrapped
	private ScanStatus status;
	
	private List<ScanAlert> alerts = new ArrayList<>();

	
	public ScanStatus getStatus() {
		return status;
	}

	public void setStatus(ScanStatus status) {
		this.status = status;
	}

	public List<ScanAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<ScanAlert> alerts) {
		this.alerts = alerts;
	}
}
