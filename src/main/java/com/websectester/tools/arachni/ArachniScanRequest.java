package com.websectester.tools.arachni;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.websectester.tools.arachni.ArachniScanRequest.Autologin;
import com.websectester.tools.arachni.ArachniScanRequest.Plugins;

@JsonInclude(NON_NULL)
public class ArachniScanRequest {

	public class Autologin {

		private String url;
		
		private String parameters;
		
		private String check;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getParameters() {
			return parameters;
		}

		public void setParameters(String parameters) {
			this.parameters = parameters;
		}

		public String getCheck() {
			return check;
		}

		public void setCheck(String check) {
			this.check = check;
		}
		
	}

	public class Plugins {

		private Autologin autologin;

		public Autologin getAutologin() {
			return autologin;
		}

		public void setAutologin(Autologin autologin) {
			this.autologin = autologin;
		}
		
	}

	private String url;
	
	private List<String> checks = new ArrayList<>();
	
	private Plugins plugins;
	
	
	protected void checkPlugins() {
		if (plugins == null) {
			plugins = new Plugins();
		}
		if (plugins.autologin == null) {
			plugins.autologin = new Autologin();
		}
	}
	
	public void setAuthUrl(String authUrl) {
		checkPlugins();
		plugins.autologin.url = authUrl;
	}
	
	public void setAuthParameters(String userField, String passField, String user, String pass) {
		checkPlugins();
		plugins.autologin.parameters = userField + "=" + user + "&" + passField + "=" + pass;
		try {
			plugins.autologin.parameters = URLEncoder.encode(plugins.autologin.parameters, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void setAuthCheckLoggedInString(String check) {
		checkPlugins();
		plugins.autologin.check = check;
	}

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

	public Plugins getPlugins() {
		return plugins;
	}

	public void setPlugins(Plugins plugins) {
		this.plugins = plugins;
	}

}
