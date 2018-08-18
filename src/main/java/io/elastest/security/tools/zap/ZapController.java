package io.elastest.security.tools.zap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/tools/zap")
public class ZapController {
	
    private static final Long NOT_STARTED_ACTIVE_SCAN_ID = -1L;
	
	private static final int POLLING_SPIDER_STATUS_INTERVAL = 1000;
	
	private static final String SPIDER_STATUS_PREFIX = "SPIDER_";
	
    Logger logger = LoggerFactory.getLogger(this.getClass());
	

	private class SpiderThread implements Runnable {
    	
    	private Long spiderScanId;
    	
    	private ScanRequest scanRequest;
    	
    	public SpiderThread(Long spiderScanId, ScanRequest scanRequest) {
    		super();
    		this.scanRequest = scanRequest;
    		this.spiderScanId = spiderScanId;
    	}

    	public void run() {
    		logger.info("SpiderThread running");
    		
    		startScanAfterSpider(spiderScanId, scanRequest);
    	}
    	
    }

    
	RestTemplate restTemplate;
	
	/**
	 * Maps spider scan IDs with their corresponding active scan IDs
	 */
	Map<Long, Long> spiderScans;
    
    
    public ZapController() {
		super();
		this.restTemplate = new RestTemplate();
		this.spiderScans = new HashMap<>();
	}
    
	@RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanResponse startScan(@RequestBody ScanRequest scanRequest) {
    	logger.info("ZAP Spider Scan: " + scanRequest.getUrl());

    	ZapScan spiderScan = restTemplate.getForObject(
    			"http://localhost:8081/JSON/spider/action/scan/?url=" + scanRequest.getUrl(), ZapScan.class);

    	Long spiderScanId = spiderScan.getScanId();
    	logger.info("ZAP Spider Scan ID: " + spiderScanId);
    	
    	spiderScans.put(spiderScanId, NOT_STARTED_ACTIVE_SCAN_ID);

    	Thread spiderThread = new Thread(new SpiderThread(spiderScanId, scanRequest));
    	spiderThread.start();
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId("" + spiderScan.getScanId());
    	
    	return scanResponse;
    }
    
	private void startScanAfterSpider(Long spiderScanId, ScanRequest scanRequest) {
		ScanStatus status = new ScanStatus();
		
    	while (!"100".equals(status.getProgress())) {
    		try {
				Thread.sleep(POLLING_SPIDER_STATUS_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		logger.info("SpiderThread requesting spider scan status. ID: " + spiderScanId);
    		status = getSpiderStatus(spiderScanId);
    		
    		logger.info("SpiderThread. spiderScanId: " + spiderScanId + " - Status: " + status.getProgress() +
    				" " + status.getStatus());
    	}
    	
    	// Spider finished, starting active scan
    	logger.info("ZAP Active Scan: " + scanRequest.getUrl());

    	ZapScan zapScan = restTemplate.getForObject(
    			"http://localhost:8081/JSON/ascan/action/scan/?url=" + scanRequest.getUrl(), ZapScan.class);
    	
    	spiderScans.put(spiderScanId, zapScan.getScanId());
    	
    	logger.info("ZAP Active Scan ID: " + zapScan.getScanId());

	}

    private ScanStatus getSpiderStatus(Long spiderScanId) {
    	ScanStatus spiderStatus = null;

    	ZapScansList spiderScans = restTemplate.getForObject("http://localhost:8081/JSON/spider/view/scans/",
    			ZapScansList.class);
    	
    	for (ZapScanStatus scanStatus : spiderScans.getScans()) {
    		if (spiderScanId == Long.valueOf(scanStatus.getId())) {
    			spiderStatus = new ScanStatus();
    			spiderStatus.setStatus(SPIDER_STATUS_PREFIX + scanStatus.getState());
				spiderStatus.setProgress(scanStatus.getProgress());
    			break;
    		}
    	}
    	
    	return spiderStatus;
    }

    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus getScanStatus(@PathVariable (required = true) String scanId) {
    	ScanStatus status = null;
    	
    	Long spiderScanId = Long.valueOf(scanId);
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		logger.info("ZAP Scan status: " + scanId + " - NULL");
    		status = new ScanStatus();
    		return status;
    	}

    	Long activeScanId = spiderScans.get(spiderScanId); 
		
    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		return getSpiderStatus(spiderScanId);
    	}
    	
    	// else: Spider finished, status from related active scan...
    	ZapScansList scans = restTemplate.getForObject("http://localhost:8081/JSON/ascan/view/scans/",
    			ZapScansList.class);
    	
    	for (ZapScanStatus scanStatus : scans.getScans()) {
    		if (activeScanId.toString().equals(scanStatus.getId())) {
    			status = new ScanStatus();
    			status.setStatus(scanStatus.getState());
				status.setProgress(scanStatus.getProgress());
    			break;
    		}
    	}

    	if (status == null) {
    		logger.info("ZAP Scan status: " + scanId + " - NULL");
    		status = new ScanStatus();
    	}
    	else {
    		logger.info("ZAP Scan status: " + scanId + " - " + status.getStatus() + ", " + status.getProgress() +
    				"%");
    	}
    	
    	return status;    	
    }
    	
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus pauseScan(@PathVariable (required = true) String scanId) {
    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		logger.info("ZAP Scan status: " + scanId + " - NULL");
    		return new ScanStatus();
    	}
    	
    	Long activeScanId = spiderScans.get(spiderScanId); 

    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		// Pause spider scan
    		restTemplate.getForObject("http://localhost:8081/JSON/spider/action/pause/?scanId=" + spiderScanId, Object.class);
    	}
    	else {
    		// Pause active scan
    		restTemplate.getForObject("http://localhost:8081/JSON/ascan/action/pause/?scanId=" + activeScanId, Object.class);
    	}
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus resumeScan(@PathVariable (required = true) String scanId) {
    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		logger.info("ZAP Scan status: " + scanId + " - NULL");
    		return new ScanStatus();
    	}
    	
    	Long activeScanId = spiderScans.get(Long.valueOf(scanId)); 

    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		// Resume spider scan
    		restTemplate.getForObject("http://localhost:8081/JSON/spider/action/resume/?scanId=" + spiderScanId, Object.class);
    	}
    	else {
    		// Resume active scan
    		restTemplate.getForObject("http://localhost:8081/JSON/ascan/action/resume/?scanId=" + activeScanId, Object.class);
    	}
		
		return getScanStatus(scanId);
    }
    	
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanReport getScanReport(@PathVariable (required = true) String scanId) {
    	ScanReport scanReport = new ScanReport();

    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		logger.info("ZAP Scan status: " + scanId + " - NULL");
    		return scanReport;
    	}
    	
    	Long activeScanId = spiderScans.get(spiderScanId); 

    	ScanStatus scanStatus = getScanStatus(scanId);
    	scanReport.setProgress(scanStatus.getProgress());
    	scanReport.setStatus(scanStatus.getStatus());

    	// Spider not finished yet, no alerts
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		return scanReport;
    	}
    	
    	// Active scan running or finished, get alerts
    	ZapScanAlertList alertList = restTemplate.getForObject(
    			"http://localhost:8081/JSON/ascan/view/alertsIds/?scanId=" + activeScanId, ZapScanAlertList.class);
    
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
