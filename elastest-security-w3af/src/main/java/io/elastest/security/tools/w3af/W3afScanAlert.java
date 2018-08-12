package io.elastest.security.tools.w3af;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class W3afScanAlert {

	public static class Attributes {
		
		private Map<String, String> properties = new HashMap<>();

		public Map<String, String> getProperties() {
			return properties;
		}

		@JsonAnySetter
		public void add(String key, String value) {
			properties.put(key, value);
		}
	}
	
	public static class OwaspTop10 {
		
		@JsonProperty("link")
		private String url;
		
		@JsonProperty("owasp_version")
		private String owaspVersion;

		@JsonProperty("risk_id")
		private Integer id;
		
		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getOwaspVersion() {
			return owaspVersion;
		}

		public void setOwaspVersion(String owaspVersion) {
			this.owaspVersion = owaspVersion;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

	}
	
	public static class Reference {
		
		private String title;
		
		private String url;
		

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		
	}
	
	
	private String url;

	private String name;
	
	@JsonProperty("desc")
	private String description;
	
	@JsonProperty("long_description")
	private String longDescription;
	
	private Attributes attributes;

	private String severity;
	
	private String var;
	
	private List<String> highlight;
	
	@JsonProperty("fix_guidance")
	private String fixGuidance;
	
	private List<Reference> references;
	
	@JsonProperty("owasp_top_10_references")
	private List<OwaspTop10> owaspTop10Refs;
	
	@JsonProperty("cwe_ids")
	private List<String> cweIds;
	
	@JsonProperty("cwe_urls")
	private List<String> cweUrls;
	
	@JsonProperty("wasc_ids")
	private List<String> wascIds;
	
	@JsonProperty("wasc_urls")
	private List<String> wascUrls;
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public List<OwaspTop10> getOwaspTop10Refs() {
		return owaspTop10Refs;
	}

	public void setOwaspTop10Refs(List<OwaspTop10> owaspTop10Refs) {
		this.owaspTop10Refs = owaspTop10Refs;
	}

	public String getFixGuidance() {
		return fixGuidance;
	}

	public void setFixGuidance(String fixGuidance) {
		this.fixGuidance = fixGuidance;
	}

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}

	public List<String> getCweIds() {
		return cweIds;
	}

	public void setCweIds(List<String> cweIds) {
		this.cweIds = cweIds;
	}

	public List<String> getCweUrls() {
		return cweUrls;
	}

	public void setCweUrls(List<String> cweUrls) {
		this.cweUrls = cweUrls;
	}

	public List<String> getWascIds() {
		return wascIds;
	}

	public void setWascIds(List<String> wascIds) {
		this.wascIds = wascIds;
	}

	public List<String> getWascUrls() {
		return wascUrls;
	}

	public void setWascUrls(List<String> wascUrls) {
		this.wascUrls = wascUrls;
	}

	public List<String> getHighlight() {
		return highlight;
	}

	public void setHighlight(List<String> highlight) {
		this.highlight = highlight;
	}

}
