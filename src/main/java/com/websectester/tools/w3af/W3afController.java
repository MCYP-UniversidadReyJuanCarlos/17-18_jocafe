package com.websectester.tools.w3af;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.websectester.model.AlertReference;
import com.websectester.model.ScanAlert;
import com.websectester.model.ScanAuth;
import com.websectester.model.AlertAttack;
import com.websectester.model.ScanReport;
import com.websectester.model.ScanRequest;
import com.websectester.model.ScanResponse;
import com.websectester.model.ScanStatus;
import com.websectester.tools.ToolController;
import com.websectester.tools.w3af.W3afScanAlert.OwaspTop10;

@RestController
@RequestMapping("/tools/w3af")
public class W3afController implements ToolController {
	
	public static final String W3AF_PROFILE_DIR = "/w3af/profiles/";
	
	// OWASP_TOP10.pw3af
	public static final String W3AF_PROFILE_OWASP_TOP10 = "OWASP_TOP10.pw3af";
	
	// full_audit.pw3af
	public static final String W3AF_PROFILE_FULL_AUDIT = "full_audit.pw3af";

	// fast_scan.pw3af
	public static final String W3AF_PROFILE_FAST_SCAN = "fast_scan.pw3af";
	
    
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RestTemplate restTemplate = new RestTemplate();
    
    
	String serviceHost;
	
	String servicePort;
	
	String serviceContext;
	

	@Value("${tools.w3af.host}")
	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}

	@Value("${tools.w3af.port}")
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	@Value("${tools.w3af.context}")
	public void setServiceContext(String serviceContext) {
		this.serviceContext = serviceContext;
	}

    protected String getServiceUrl() {
    	return serviceHost + ":" + servicePort + "/" + serviceContext;
    }
    
    @Override
    @RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanResponse startScan(@RequestBody ScanRequest scanRequest, HttpServletResponse response) {
    	logger.info("W3af Scan: " + scanRequest.getUrl());
    	
    	if ((scanRequest.getUrl() == null) || scanRequest.getUrl().isEmpty()) {
			try {
				response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing required parameter: url");
				return new ScanResponse();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	// The current W3af REST API implementation does not allow users to run more than one concurrent scan.
    	// Get previous scans list to delete them
    	W3afScanList scanList = restTemplate.getForObject(getServiceUrl(), W3afScanList.class);
    	for (String scanId : scanList.getScanIds()) {
        	logger.info("Delete previous W3af Scan: " + scanId);
        	// Delete old scan
    		restTemplate.delete(getServiceUrl() + scanId);
    	}

    	// Start new scan
    	W3afScanRequest w3afRequest = new W3afScanRequest();
    	w3afRequest.setUrl(scanRequest.getUrl());
    	
    	// Load scan profile from resource file
    	String profile = W3AF_PROFILE_FULL_AUDIT;
    	w3afRequest.setScanProfile(getFile(W3AF_PROFILE_DIR + profile));

    	// Authentication
    	if (scanRequest.getAuth() != null) {
    		ScanAuth scanAuth = scanRequest.getAuth();
    		if (authParamsValid(scanAuth)) {
    			String authProfile = "\n\n[auth.generic]\n";
    			authProfile += "\nauth_url = " + scanAuth.getAuthUrl();
    			authProfile += "\nusername_field = " + scanAuth.getUsernameField();
				authProfile += "\npassword_field = " + scanAuth.getPasswordField();
				authProfile += "\nusername = " + scanAuth.getUsername();
				authProfile += "\npassword = " + scanAuth.getPassword();
				authProfile += "\ncheck_url = " + scanAuth.getCheckLoggedInUrl();
				authProfile += "\ncheck_string = " + scanAuth.getCheckLoggedInString();
				authProfile += "\n\n";
				w3afRequest.setScanProfile(w3afRequest.getScanProfile() + authProfile);
    		}
    		else {
    			try {
					response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing required authentication parameters "
							+ "(authUrl, usernameField, passwordField, username, password,"
							+ " checkLoggedInUrl, checkLoggedInString)");
					return new ScanResponse();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    	logger.info("W3af Scan Profile: " + profile);

    	HttpEntity<W3afScanRequest> request = new HttpEntity<>(w3afRequest);
    	W3afScan w3afScan = restTemplate.postForObject(getServiceUrl(), request, W3afScan.class);
    	
    	logger.info("W3af Scan ID: " + w3afScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId(w3afScan.getScanId().toString());
    	
    	return scanResponse;
    }
    
    private boolean authParamsValid(ScanAuth scanAuth) {
		if ((scanAuth.getAuthUrl() == null) ||
			(scanAuth.getUsernameField() == null) ||	
			(scanAuth.getPasswordField() == null) ||	
			(scanAuth.getUsername() == null) ||	
			(scanAuth.getPassword() == null) ||	
			(scanAuth.getCheckLoggedInUrl() == null) ||	
			(scanAuth.getCheckLoggedInString() == null)) {	
			return false;
		}
		return true;
    }

    private String getFile(String fileName) {
    	InputStream inputStream = getClass().getResourceAsStream(fileName);
        BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	        StringBuilder stringBuilder = new StringBuilder();
	        String inputStr;
	        while ((inputStr = br.readLine()) != null) {
	            stringBuilder.append(inputStr).append("\n");
	        }
	        return stringBuilder.toString();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	// TODO Improve exception processing 
		logger.info("ERROR - File not found: " + fileName);
		return "";
    }
    
    @Override
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	
    	W3afScanStatus w3afStatus = restTemplate.getForObject(getServiceUrl() + scanId + "/status",
    			W3afScanStatus.class);
    	
    	ScanStatus status = new ScanStatus();
    	if (w3afStatus != null) {
    		if (w3afStatus.getStatus() != null) {
    			status.setStatus(w3afStatus.getStatus().toUpperCase());
    		}
			status.setProgress(w3afStatus.getProgress());
    	}

    	return status;    	
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	
    	// Pausing in W3af doesn't work
//		restTemplate.put("http://localhost:5000/scans/" + scanId + "/pause", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	
    	// Pausing in W3af doesn't work
//		restTemplate.put("http://localhost:5000/scans/" + scanId + "/resume", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanReport scanReport = new ScanReport();
    	scanReport.setStatus(getScanStatus(scanId));
    	
    	W3afScanAlertList alertList = restTemplate.getForObject(getServiceUrl() + scanId + "/kb",
    			W3afScanAlertList.class);
    
    	for (String alertId : alertList.getAlertIds()) {
    		W3afScanAlert w3afAlert = restTemplate.getForObject(getServiceUrl() + scanId + "/kb/" + alertId,
    				W3afScanAlert.class);
    		
    		if (w3afAlert == null) {
    			continue;
    		}
    		
    		ScanAlert alert = new ScanAlert();
    		alert.setName(w3afAlert.getName());
    		alert.setDescription(w3afAlert.getDescription() 
    				+ (w3afAlert.getLongDescription() != null ? "\n\n" + w3afAlert.getLongDescription() : ""));
    		alert.setUrl(w3afAlert.getUrl());
    		alert.setSeverity(w3afAlert.getSeverity());
    		alert.setSolution(w3afAlert.getFixGuidance());
    		
    		AlertAttack attack = new AlertAttack();
    		attack.setParam(w3afAlert.getVar());
			String evidence = "";
    		List<String> highlights = w3afAlert.getHighlight();
    		if (highlights != null) {
    			for (String highlight : highlights) {
    				if (!evidence.isEmpty()) {
    					evidence += "\n";
    				}
    				evidence += highlight;
    			}
    		}
			attack.setEvidence(evidence);
			if (w3afAlert.getAttributes() != null) {
				attack.getAttributes().setProperties(w3afAlert.getAttributes().getProperties());
			}
    		alert.setAttack(attack);
    		
    		List<AlertReference> references = new ArrayList<>();
    		
    		// CWE
    		AlertReference reference = new AlertReference();
    		if (w3afAlert.getCweIds() != null) {
    			int i = 0;
    			for (String id : w3afAlert.getCweIds()) {
    				String url = w3afAlert.getCweUrls().get(i);
    				if (url != null) {
			    		reference.setSource("CWE");
			    		reference.setId(id);
			    		reference.setUrl(url);
			    		references.add(reference);
    				}
    			}
    		}
    		
    		// WASC
    		if (w3afAlert.getWascIds() != null) {
    			int i = 0;
    			for (String id : w3afAlert.getWascIds()) {
    				String url = w3afAlert.getWascUrls().get(i);
    				if (url != null) {
			    		reference.setSource("WASC");
			    		reference.setId(id);
			    		reference.setUrl(url);
			    		references.add(reference);
    				}
    			}
    		}
    		
    		// URL references
    		if (w3afAlert.getReferences() != null) {
    			for (W3afScanAlert.Reference ref : w3afAlert.getReferences()) {
    				reference = new AlertReference();
    				reference.setSource(ref.getTitle());
    				reference.setUrl(ref.getUrl());
    				references.add(reference);
    			}
    		}
    		
    		// OWASP Top 10
    		if (w3afAlert.getOwaspTop10Refs() != null) {
    			for (OwaspTop10 ref : w3afAlert.getOwaspTop10Refs()) {
    				reference = new AlertReference();
    				reference.setSource("OWASP Top 10 " + ref.getOwaspVersion());
    				reference.setId("" + ref.getId());
    				reference.setUrl(ref.getUrl());
    				references.add(reference);
    			}
    		}
    		
    		alert.setReferences(references);
    		
    		scanReport.getAlerts().add(alert);
    	}
    	
    	return scanReport;
    }
    
    @Override
    public boolean isToolAvailable() {
    	Object result = null;
    	try {
    		result = restTemplate.getForObject(getServiceUrl(), Object.class);
    	}
    	catch (RestClientException e) {
    	}
    	
    	return result != null;
    }
    
}
