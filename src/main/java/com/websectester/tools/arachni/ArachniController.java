package com.websectester.tools.arachni;

import java.io.IOException;
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
import com.websectester.model.AlertAttack;
import com.websectester.model.ScanReport;
import com.websectester.model.ScanRequest;
import com.websectester.model.ScanResponse;
import com.websectester.model.ScanStatus;
import com.websectester.tools.ToolController;

@RestController
@RequestMapping("/tools/arachni")
public class ArachniController implements ToolController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

    RestTemplate restTemplate = new RestTemplate();
    
	String serviceHost;
	
	String servicePort;
	
	String serviceContext;
	

	@Value("${tools.arachni.host}")
	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}

	@Value("${tools.arachni.port}")
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	@Value("${tools.arachni.context}")
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
    	logger.info("Arachni Scan: " + scanRequest.getUrl());

    	if ((scanRequest.getUrl() == null) || scanRequest.getUrl().isEmpty()) {
			try {
				response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing required parameter: url");
				return new ScanResponse();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	ArachniScanRequest arachniRequest = new ArachniScanRequest();
    	arachniRequest.setUrl(scanRequest.getUrl());
    	arachniRequest.getChecks().add("*");
    	
    	// Authentication
    	if (scanRequest.getAuth() != null) {
    		if (checkAuthParameters(scanRequest)) {
    			arachniRequest.setAuthUrl(scanRequest.getAuth().getAuthUrl());
    			arachniRequest.setAuthParameters(
    					scanRequest.getAuth().getUsernameField(),
    					scanRequest.getAuth().getPasswordField(),
    					scanRequest.getAuth().getUsername(),
    					scanRequest.getAuth().getPassword());
    			arachniRequest.setAuthCheckLoggedInString(scanRequest.getAuth().getCheckLoggedInString());
    		}
    		else {
    			try {
					response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing required authentication parameters "
							+ "(authUrl, usernameField, passwordField, username, password, checkLoggedInString)");
					return new ScanResponse();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    	
    	HttpEntity<ArachniScanRequest> request = new HttpEntity<>(arachniRequest);
    	ArachniScan arachniScan = restTemplate.postForObject(getServiceUrl(), request, ArachniScan.class);
    	
    	logger.info("Arachni Scan ID: " + arachniScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId(arachniScan.getScanId());
    	
    	return scanResponse;
    }
    
    private boolean checkAuthParameters(ScanRequest scanRequest) {
		if ((scanRequest.getAuth().getAuthUrl() == null) ||
			(scanRequest.getAuth().getUsernameField() == null) ||	
			(scanRequest.getAuth().getPasswordField() == null) ||	
			(scanRequest.getAuth().getUsername() == null) ||	
			(scanRequest.getAuth().getPassword() == null) ||	
			(scanRequest.getAuth().getCheckLoggedInString() == null)) {	
			return false;
		}
		return true;
    }
    
    @Override
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	
    	ArachniScanStatus arachniStatus = restTemplate.getForObject(getServiceUrl() + "/" + scanId + "/summary",
    			ArachniScanStatus.class);
    	
    	ScanStatus status = new ScanStatus();
    	if (arachniStatus != null) {
    		if (arachniStatus.getStatus() != null) {
    			status.setStatus(arachniStatus.getStatus().toUpperCase());
    		}
			status.setProgress(arachniStatus.getProgress());
    	}

    	return status;    	
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.put(getServiceUrl() + "/" + scanId + "/pause", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.put(getServiceUrl() + "/" + scanId + "/resume", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanReport scanReport = new ScanReport();
    	scanReport.setStatus(getScanStatus(scanId));
    	
    	ArachniScanReport arachniReport = restTemplate.getForObject(getServiceUrl() + "/" + scanId + "/report",
    			ArachniScanReport.class);
    
    	if (arachniReport.getIssues() != null) {
	    	for (ArachniScanAlert arachniAlert : arachniReport.getIssues()) {
	    		ScanAlert alert = new ScanAlert();
	    		alert.setName(arachniAlert.getName());
	    		alert.setDescription(arachniAlert.getDescription());
	    		alert.setUrl(arachniAlert.getUrl());
	    		alert.setSeverity(arachniAlert.getSeverity());
	    		alert.setSolution(arachniAlert.getRemedyGuidance());
	    		
	    		AlertAttack attack = new AlertAttack();
	    		attack.setParam(arachniAlert.getParam());
	    		attack.setEvidence(arachniAlert.getProof());
	    		alert.setAttack(attack);
	    		
	    		List<AlertReference> references = new ArrayList<>();
	    		
	    		AlertReference reference = new AlertReference();
	    		// CWE
	    		if ((arachniAlert.getCwe() != null) && !arachniAlert.getCwe().isEmpty()) {
		    		reference.setSource("CWE");
		    		reference.setId(arachniAlert.getCwe());
		    		reference.setUrl(arachniAlert.getCweUrl());
		    		references.add(reference);
	    		}
	    		
	    		// Other
	    		if ((arachniAlert.getReferences() != null) && (arachniAlert.getReferences().getProperties() != null)) {
	    			for (String source : arachniAlert.getReferences().getProperties().keySet()) {
			    		reference = new AlertReference();
			    		reference.setSource(source);
			    		reference.setUrl(arachniAlert.getReferences().getProperties().get(source));
			    		references.add(reference);
	    			}
	    		}
	    		
	    		alert.setReferences(references);
	    		
	    		scanReport.getAlerts().add(alert);
	    	}
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
