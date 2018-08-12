package io.elastest.security.tools.arachni;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ArachniScanAlert {

	public static class References {
		
		private Map<String, String> properties = new HashMap<>();

		public Map<String, String> getProperties() {
			return properties;
		}

		@JsonAnySetter
		public void add(String key, String value) {
			properties.put(key, value);
		}
	}
	
	public static class Vector {
		
		private String url;
		
		@JsonProperty("affected_input_name")
		private String affectedInputName;

		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getAffectedInputName() {
			return affectedInputName;
		}

		public void setAffectedInputName(String affectedInputName) {
			this.affectedInputName = affectedInputName;
		}
		
	}

	private String name;
	
	private String description;
	
	private References references;

	private String severity;
	
	private String proof;
	
	private Vector vector;
	
	@JsonProperty("remedy_guidance")
	private String remedyGuidance;
	
	private String cwe;
	
	@JsonProperty("cwe_url")
	private String cweUrl;
	

	public String getUrl() {
		if (vector != null) {
			return vector.getUrl();
		}
		return "";
	}
	
	public String getParam() {
		if (vector != null) {
			return vector.getAffectedInputName();
		}
		return "";
	}

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

	public References getReferences() {
		return references;
	}

	public void setReferences(References references) {
		this.references = references;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getProof() {
		return proof;
	}

	public void setProof(String proof) {
		this.proof = proof;
	}

	public Vector getVector() {
		return vector;
	}

	public void setVector(Vector vector) {
		this.vector = vector;
	}

	public String getRemedyGuidance() {
		return remedyGuidance;
	}

	public void setRemedyGuidance(String remedyGuidance) {
		this.remedyGuidance = remedyGuidance;
	}

	public String getCwe() {
		return cwe;
	}

	public void setCwe(String cwe) {
		this.cwe = cwe;
	}

	public String getCweUrl() {
		return cweUrl;
	}

	public void setCweUrl(String cweUrl) {
		this.cweUrl = cweUrl;
	}
	
}
