package io.elastest.security.tools.w3af;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScanAlertList {

	public static class Alert {
		
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}


	@JsonProperty("items")
	private List<Alert> alerts = new ArrayList<>();


	public List<Alert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	public List<String> getAlertIds() {
		List<String> alertIds = new ArrayList<>();
		
		if (alerts != null) {
			for (Alert alert : alerts) {
				alertIds.add(alert.getId());
			}
		}
		
		return alertIds;
	}

}
