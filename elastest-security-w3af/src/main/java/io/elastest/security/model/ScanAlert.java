package io.elastest.security.model;

import java.util.ArrayList;
import java.util.List;

public class ScanAlert {

	private String name;
	
	private String description;
	
	private String url;
	
	private String severity;
	
	private String solution;
	
	private ScanAttack attack;
	
	private List<Reference> references = new ArrayList<>();

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}

	public ScanAttack getAttack() {
		return attack;
	}

	public void setAttack(ScanAttack attack) {
		this.attack = attack;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}
	
}
