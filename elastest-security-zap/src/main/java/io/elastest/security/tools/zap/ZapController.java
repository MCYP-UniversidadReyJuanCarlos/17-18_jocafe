package io.elastest.security.tools.zap;

import java.util.ArrayList;
import java.util.List;

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

@RestController
public class ZapController {

	RestTemplate restTemplate = new RestTemplate();
    
    
    @RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanResponse startScan(@RequestBody ScanRequest scanRequest) {
    	
    	System.out.println("ZAP Spider Scan: " + scanRequest.getUrl());

    	ZapScan spiderScan = restTemplate.getForObject(
    			"http://localhost:8081/JSON/spider/action/scan/?url=" + scanRequest.getUrl(),
    			ZapScan.class);

    	System.out.println("ZAP Spider Scan ID: " + spiderScan.getScanId());
    	
    	// Hay que dar tiempo a que comience el spider en la URL indicada, para poder iniciar un escaneo activo
    	// TODO Asegurarse de que es suficiente con que haya comenzado; igual es necesario que haya terminado para que
    	//      analice correcta y completamente toda la web.
    	try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("ZAP Active Scan: " + scanRequest.getUrl());

    	ZapScan zapScan = restTemplate.getForObject(
    			"http://localhost:8081/JSON/ascan/action/scan/?url=" + scanRequest.getUrl(),
    			ZapScan.class);
    	
    	System.out.println("ZAP Active Scan ID: " + zapScan.getScanId());
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId("" + zapScan.getScanId());
    	
    	return scanResponse;
    }
    
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	
    	ZapScansList scans = restTemplate.getForObject("http://localhost:8081/JSON/ascan/view/scans/",
    			ZapScansList.class);
    	
    	ScanStatus status = null;
    	for (ZapScanStatus scanStatus : scans.getScans()) {
    		if (scanId.equals(scanStatus.getId())) {
    			status = new ScanStatus();
    			status.setStatus(scanStatus.getState());
				status.setProgress(scanStatus.getProgress());
    			break;
    		}
    	}

    	if (status == null) {
    		System.out.println("ZAP Scan status: " + scanId + " - NULL");
    		status = new ScanStatus();
    	}
    	else {
    		System.out.println("ZAP Scan status: " + scanId + " - " + status.getStatus() + ", " + status.getProgress() +
    				"%");
    	}
    	
    	return status;    	
    }
    	
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.getForObject("http://localhost:8081/JSON/ascan/action/pause/?scanId=" + scanId, Object.class);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	
		restTemplate.getForObject("http://localhost:8081/JSON/ascan/action/resume/?scanId=" + scanId, Object.class);
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanStatus scanStatus = getScanStatus(scanId);
    	
    	ScanReport scanReport = new ScanReport();
    	scanReport.setProgress(scanStatus.getProgress());
    	scanReport.setStatus(scanStatus.getStatus());
    	
    	ZapScanAlertList alertList = restTemplate.getForObject(
    			"http://localhost:8081/JSON/ascan/view/alertsIds/?scanId=" + scanId,
    			ZapScanAlertList.class);
    
    	for (String alertId : alertList.getAlertsIds()) {
    		ZapScanAlertResponse alertResponse = restTemplate.getForObject(
    				"http://localhost:8081/JSON/core/view/alert/?id=" + alertId, ZapScanAlertResponse.class);
    		
    		if (alertResponse == null) {
    			continue;
    		}
    		ZapScanAlert zapAlert = alertResponse.getAlert();
    		
    		ScanAlert alert = new ScanAlert();
    		alert.setName(zapAlert.getName());
    		alert.setDescription(zapAlert.getDescription());
    		alert.setUrl(zapAlert.getUrl());
    		alert.setSeverity(zapAlert.getRisk());
    		alert.setSolution(zapAlert.getSolution());
    		
    		ScanAttack attack = new ScanAttack();
    		attack.setParam(zapAlert.getParam());
    		attack.setEvidence(zapAlert.getEvidence());
    		alert.setAttack(attack);
    		
    		List<Reference> references = new ArrayList<>();
    		
    		// CWE
    		Reference reference = new Reference();
    		if ((zapAlert.getCweid() != null) && !zapAlert.getCweid().isEmpty()) {
	    		reference.setSource("CWE");
	    		reference.setId(zapAlert.getCweid());
	    		references.add(reference);
    		}
    		
    		// WASC
    		if ((zapAlert.getWascid() != null) && !zapAlert.getWascid().isEmpty()) {
	    		reference = new Reference();
	    		reference.setSource("WASC");
	    		reference.setId(zapAlert.getWascid());
	    		references.add(reference);
    		}
    		
    		// URL references
    		String zapReferences = zapAlert.getReference();
    		if ((zapReferences != null) && !zapReferences.isEmpty()) {
    			String[] refsArray = zapReferences.split(" |\n");
    			for (String ref : refsArray) {
    				reference = new Reference();
    				reference.setUrl(ref);
    				references.add(reference);
    			}
    		}
    		
    		alert.setReferences(references);
    		
    		scanReport.getAlerts().add(alert);
    	}
    	
    	return scanReport;
    }
    
}
