package io.elastest.security.tools.w3af;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import io.elastest.security.tools.ToolController;
import io.elastest.security.tools.w3af.W3afScanAlert.OwaspTop10;

@RestController
@RequestMapping("/tools/w3af")
public class W3afController implements ToolController {
	
	public static final String W3AF_SERVICE_URL = "http://0.0.0.0:5000/scans/";

	public static final String W3AF_PROFILE_DIR = "/w3af/profiles/";
	
	// OWASP_TOP10.pw3af
	public static final String W3AF_PROFILE_OWASP_TOP10 = "OWASP_TOP10.pw3af";
	
	// full_audit.pw3af
	public static final String W3AF_PROFILE_FULL_AUDIT = "full_audit.pw3af";

	// fast_scan.pw3af
	public static final String W3AF_PROFILE_FAST_SCAN = "fast_scan.pw3af";
	
    
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RestTemplate restTemplate = new RestTemplate();
    
    
    @Override
    @RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanResponse startScan(@RequestBody ScanRequest scanRequest) {
    	
    	logger.info("W3af Scan: " + scanRequest.getUrl());
    	
    	// The current W3af REST API implementation does not allow users to run more than one concurrent scan.
    	// Get previous scans list to delete them
    	W3afScanList scanList = restTemplate.getForObject(W3AF_SERVICE_URL, W3afScanList.class);
    	for (String scanId : scanList.getScanIds()) {
        	logger.info("Delete previous W3af Scan: " + scanId);
        	// Delete old scan
    		restTemplate.delete(W3AF_SERVICE_URL + scanId);
    	}

    	// Start new scan
    	W3afScanRequest w3afRequest = new W3afScanRequest();
    	w3afRequest.setUrl(scanRequest.getUrl());
    	
    	// Load scan profile from resource file
    	String profile = W3AF_PROFILE_FULL_AUDIT;
    	w3afRequest.setScanProfile(getFile(W3AF_PROFILE_DIR + profile));

    	logger.info("W3af Scan Profile: " + profile);

    	HttpEntity<W3afScanRequest> request = new HttpEntity<>(w3afRequest);
    	W3afScan w3afScan = restTemplate.postForObject(W3AF_SERVICE_URL, request, W3afScan.class);
    	
    	logger.info("W3af Scan ID: " + w3afScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId(w3afScan.getScanId().toString());
    	
    	return scanResponse;
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
    	
    	W3afScanStatus w3afStatus = restTemplate.getForObject(W3AF_SERVICE_URL + scanId + "/status",
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
    	
    	W3afScanAlertList alertList = restTemplate.getForObject(W3AF_SERVICE_URL + scanId + "/kb",
    			W3afScanAlertList.class);
    
    	for (String alertId : alertList.getAlertIds()) {
    		W3afScanAlert w3afAlert = restTemplate.getForObject(W3AF_SERVICE_URL + scanId + "/kb/" + alertId,
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
    		
    		ScanAttack attack = new ScanAttack();
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
    		
    		List<Reference> references = new ArrayList<>();
    		
    		// CWE
    		Reference reference = new Reference();
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
    				reference = new Reference();
    				reference.setSource(ref.getTitle());
    				reference.setUrl(ref.getUrl());
    				references.add(reference);
    			}
    		}
    		
    		// OWASP Top 10
    		if (w3afAlert.getOwaspTop10Refs() != null) {
    			for (OwaspTop10 ref : w3afAlert.getOwaspTop10Refs()) {
    				reference = new Reference();
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
    		result = restTemplate.getForObject(W3AF_SERVICE_URL, Object.class);
    	}
    	catch (RestClientException e) {
    	}
    	
    	return result != null;
    }
    
}
