package io.elastest.security.tools.zap;

import java.util.ArrayList;
import java.util.List;

public class ZapScanAlertList {

	private List<String> alertsIds = new ArrayList<>();

	
	public List<String> getAlertsIds() {
		return alertsIds;
	}

	public void setAlertsIds(List<String> alertsIds) {
		this.alertsIds = alertsIds;
	}

}
