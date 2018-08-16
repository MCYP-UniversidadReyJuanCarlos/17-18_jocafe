package io.elastest.security.tools.arachni;

import java.util.List;

public class ArachniScanReport {

	private List<ArachniScanAlert> issues;

	
	public List<ArachniScanAlert> getIssues() {
		return issues;
	}

	public void setIssues(List<ArachniScanAlert> issues) {
		this.issues = issues;
	}

}
