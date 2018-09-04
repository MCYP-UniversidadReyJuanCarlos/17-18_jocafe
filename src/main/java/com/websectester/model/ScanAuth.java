package com.websectester.model;

public class ScanAuth {
	
	private String authUrl;
	
	private String usernameField;
	
	private String passwordField;
	
	private String username;
	
	private String password;
	
	private String checkLoggedInUrl;
	
	private String checkLoggedInString;
	
	private String checkLoggedOutString;


	public String getAuthUrl() {
		return authUrl;
	}

	public void setAuthUrl(String authUrl) {
		this.authUrl = authUrl;
	}

	public String getUsernameField() {
		return usernameField;
	}

	public void setUsernameField(String usernameField) {
		this.usernameField = usernameField;
	}

	public String getPasswordField() {
		return passwordField;
	}

	public void setPasswordField(String passwordField) {
		this.passwordField = passwordField;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCheckLoggedInUrl() {
		return checkLoggedInUrl;
	}

	public void setCheckLoggedInUrl(String checkLoggedInUrl) {
		this.checkLoggedInUrl = checkLoggedInUrl;
	}

	public String getCheckLoggedInString() {
		return checkLoggedInString;
	}

	public void setCheckLoggedInString(String checkLoggedInString) {
		this.checkLoggedInString = checkLoggedInString;
	}

	public String getCheckLoggedOutString() {
		return checkLoggedOutString;
	}

	public void setCheckLoggedOutString(String checkLoggedOutString) {
		this.checkLoggedOutString = checkLoggedOutString;
	}
	
}
