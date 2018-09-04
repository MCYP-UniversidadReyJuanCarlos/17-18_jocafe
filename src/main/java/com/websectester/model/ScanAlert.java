package com.websectester.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ScanAlert {

	private String name;
	
	private String description;
	
	private String url;
	
	private String severity;
	
	private String solution;
	
	private AlertAttack attack;
	
	private List<AlertReference> references = new ArrayList<>();

	
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

	public AlertAttack getAttack() {
		return attack;
	}

	public void setAttack(AlertAttack attack) {
		this.attack = attack;
	}

	public List<AlertReference> getReferences() {
		return references;
	}

	public void setReferences(List<AlertReference> references) {
		this.references = references;
	}
	
}
