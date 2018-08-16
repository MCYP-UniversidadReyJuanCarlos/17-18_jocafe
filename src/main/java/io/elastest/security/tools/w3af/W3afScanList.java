package io.elastest.security.tools.w3af;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScanList {

	public static class Scan {
		
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}


	@JsonProperty("items")
	private List<Scan> scans = new ArrayList<>();


	public List<Scan> getScans() {
		return scans;
	}

	public void setScans(List<Scan> scans) {
		this.scans = scans;
	}

	public List<String> getScanIds() {
		List<String> scanIds = new ArrayList<>();
		
		if (scans != null) {
			for (Scan scan : scans) {
				scanIds.add(scan.getId());
			}
		}
		
		return scanIds;
	}

}
