package io.elastest.security.tools.arachni;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.elastest.security.model.Reference;
import io.elastest.security.model.ScanAlert;
import io.elastest.security.model.ScanAttack;
import io.elastest.security.model.ScanReport;
import io.elastest.security.model.ScanRequest;
import io.elastest.security.model.ScanResponse;
import io.elastest.security.model.ScanStatus;

@RestController
@RequestMapping("/tools/arachni")
public class ArachniController {

	public static final String ARACHNI_SERVICE_URL = "http://arachni:7331/scans";

	
	Logger logger = LoggerFactory.getLogger(this.getClass());

    RestTemplate restTemplate = new RestTemplate();
    
    
    @RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanResponse startScan(@RequestBody ScanRequest scanRequest) {
    	
    	logger.info("Arachni Scan: " + scanRequest.getUrl());

    	ArachniScanRequest arachniRequest = new ArachniScanRequest();
    	arachniRequest.setUrl(scanRequest.getUrl());
    	arachniRequest.getChecks().add("*");
    	
    	HttpEntity<ArachniScanRequest> request = new HttpEntity<>(arachniRequest);
    	ArachniScan arachniScan = restTemplate.postForObject(ARACHNI_SERVICE_URL, request, ArachniScan.class);
    	
    	logger.info("Arachni Scan ID: " + arachniScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId(arachniScan.getScanId());
    	
    	return scanResponse;
    }
    
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	
    	ArachniScanStatus arachniStatus = restTemplate.getForObject(ARACHNI_SERVICE_URL + "/" + scanId,
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
    	
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.put(ARACHNI_SERVICE_URL + "/" + scanId + "/pause", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.put(ARACHNI_SERVICE_URL + "/" + scanId + "/resume", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanStatus scanStatus = getScanStatus(scanId);
    	
    	ScanReport scanReport = new ScanReport();
    	scanReport.setProgress(scanStatus.getProgress());
    	scanReport.setStatus(scanStatus.getStatus());
    	
    	ArachniScanReport arachniReport = restTemplate.getForObject(ARACHNI_SERVICE_URL + "/" + scanId + "/report",
    			ArachniScanReport.class);
    
    	if (arachniReport.getIssues() != null) {
	    	for (ArachniScanAlert arachniAlert : arachniReport.getIssues()) {
	    		ScanAlert alert = new ScanAlert();
	    		alert.setName(arachniAlert.getName());
	    		alert.setDescription(arachniAlert.getDescription());
	    		alert.setUrl(arachniAlert.getUrl());
	    		alert.setSeverity(arachniAlert.getSeverity());
	    		alert.setSolution(arachniAlert.getRemedyGuidance());
	    		
	    		ScanAttack attack = new ScanAttack();
	    		attack.setParam(arachniAlert.getParam());
	    		attack.setEvidence(arachniAlert.getProof());
	    		alert.setAttack(attack);
	    		
	    		List<Reference> references = new ArrayList<>();
	    		
	    		Reference reference = new Reference();
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
			    		reference = new Reference();
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
    
    public boolean isToolAvailable() {
    	Object result = null;
    	try {
    		result = restTemplate.getForObject(ARACHNI_SERVICE_URL, Object.class);
    	}
    	catch (RestClientException e) {
    	}
    	
    	return result != null;
    }
    
}
