package io.elastest.security.tools.w3af;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.elastest.security.model.Reference;
import io.elastest.security.model.ScanAlert;
import io.elastest.security.model.ScanAttack;
import io.elastest.security.model.ScanReport;
import io.elastest.security.model.ScanRequest;
import io.elastest.security.model.ScanResponse;
import io.elastest.security.model.ScanStatus;
import io.elastest.security.tools.w3af.W3afScanAlert.OwaspTop10;

@RestController
public class W3afController {
	
	public static final String W3AF_PROFILE_DIR = "profiles/";
	
	// OWASP_TOP10.pw3af
	public static final String W3AF_PROFILE_OWASP_TOP10 = "OWASP_TOP10.pw3af";
	
	// full_audit.pw3af
	public static final String W3AF_PROFILE_FULL_AUDIT = "full_audit.pw3af";

	// fast_scan.pw3af
	public static final String W3AF_PROFILE_FAST_SCAN = "fast_scan.pw3af";
	
	
	RestTemplate restTemplate = new RestTemplate();
    
    
    @RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanResponse startScan(@RequestBody ScanRequest scanRequest) {
    	
    	System.out.println("W3af Scan: " + scanRequest.getUrl());
    	
    	// W3af no soporta análisis concurrentes, así que hay que eliminar el análisis anterior (si existe)
    	// Obtenemos la lista de análisis existentes
    	W3afScanList scanList = restTemplate.getForObject("http://localhost:5000/scans/", W3afScanList.class);
    	for (String scanId : scanList.getScanIds()) {
        	System.out.println("Delete previous W3af Scan: " + scanId);
    		restTemplate.delete("http://localhost:5000/scans/" + scanId);
    	}

    	// Creamos el nuevo análisis
    	W3afScanRequest w3afRequest = new W3afScanRequest();
    	w3afRequest.setUrl(scanRequest.getUrl());
    	
    	// Cargamos el perfil de fichero
    	String profile = W3AF_PROFILE_FAST_SCAN;
    	w3afRequest.setScanProfile(getFile(W3AF_PROFILE_DIR + profile));

    	System.out.println("W3af Scan Profile: " + profile);

    	HttpEntity<W3afScanRequest> request = new HttpEntity<>(w3afRequest);
    	W3afScan w3afScan = restTemplate.postForObject("http://localhost:5000/scans/", request, W3afScan.class);
    	
    	System.out.println("W3af Scan ID: " + w3afScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId(w3afScan.getScanId().toString());
    	
    	return scanResponse;
    }
    
    private String getFile(String fileName) {
    	StringBuilder result = new StringBuilder("");

    	//Get file from resources folder
    	ClassLoader classLoader = getClass().getClassLoader();
    	File file = new File(classLoader.getResource(fileName).getFile());

    	try (Scanner scanner = new Scanner(file)) {
    		while (scanner.hasNextLine()) {
    			String line = scanner.nextLine();
    			result.append(line).append("\n");
    		}

    		scanner.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    		
    	return result.toString();
    }
    
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	
    	W3afScanStatus w3afStatus = restTemplate.getForObject("http://localhost:5000/scans/" + scanId + "/status",
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
    	
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	
    	// No funciona el pausado en W3af
//		restTemplate.put("http://localhost:7331/scans/" + scanId + "/pause", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	
    	// No funciona el pausado en W3af
//		restTemplate.put("http://localhost:7331/scans/" + scanId + "/resume", scanId);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanStatus scanStatus = getScanStatus(scanId);
    	
    	ScanReport scanReport = new ScanReport();
    	scanReport.setProgress(scanStatus.getProgress());
    	scanReport.setStatus(scanStatus.getStatus());
    	
    	W3afScanAlertList alertList = restTemplate.getForObject("http://localhost:5000/scans/" + scanId + "/kb",
    			W3afScanAlertList.class);
    
    	for (String alertId : alertList.getAlertIds()) {
    		W3afScanAlert w3afAlert = restTemplate.getForObject(
    				"http://localhost:5000/scans/" + scanId + "/kb/" + alertId, W3afScanAlert.class);
    		
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
    
}
