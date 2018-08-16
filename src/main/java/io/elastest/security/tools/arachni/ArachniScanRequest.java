package io.elastest.security.tools.arachni;

import java.util.ArrayList;
import java.util.List;

public class ArachniScanRequest {

	private String url;
	
	private List<String> checks = new ArrayList<>();
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getChecks() {
		return checks;
	}

	public void setChecks(List<String> checks) {
		this.checks = checks;
	}

}
