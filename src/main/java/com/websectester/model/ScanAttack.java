package com.websectester.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_NULL)
public class ScanAttack {

	public static class Attributes {
		
		private Map<String, String> properties = new HashMap<>();

		@JsonAnyGetter
		public Map<String, String> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

	}
	

	private String param;
	
	private String evidence;
	
	private Attributes attributes = new Attributes();

	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getEvidence() {
		return evidence;
	}

	public void setEvidence(String evidence) {
		this.evidence = evidence;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}
	
}