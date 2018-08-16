package io.elastest.security.model;

public class Tool {

	private String name;
	
	private String identifier;
	
	private Boolean available;

	
	public Tool() {
		
	}
	
	public Tool(String name, String identifier, Boolean available) {
		super();
		this.name = name;
		this.identifier = identifier;
		this.available = available;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

}
