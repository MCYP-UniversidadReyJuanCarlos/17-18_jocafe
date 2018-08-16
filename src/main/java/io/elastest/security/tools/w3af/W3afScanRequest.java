package io.elastest.security.tools.w3af;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScanRequest {

	@JsonProperty("target_urls")
	private List<String> urls = new ArrayList<>();
	
	@JsonProperty("scan_profile")
	private String scanProfile;
	

	@JsonIgnore
	public String getUrl() {
		if (!this.urls.isEmpty()) {
			return this.urls.get(0);
		}
		return "";
	}

	public void setUrl(String url) {
		this.urls.clear();
		this.urls.add(url);
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public String getScanProfile() {
		return scanProfile;
	}

	public void setScanProfile(String scanProfile) {
		this.scanProfile = scanProfile;
	}

}
