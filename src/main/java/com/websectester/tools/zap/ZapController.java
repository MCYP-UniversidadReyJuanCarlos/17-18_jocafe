package com.websectester.tools.zap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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

@SuppressWarnings("unused")
@RestController
@RequestMapping("/tools/zap")
public class ZapController implements ToolController {
	
    private static final Long NOT_STARTED_ACTIVE_SCAN_ID = -1L;
	
	private static final int POLLING_SPIDER_STATUS_INTERVAL = 1000;
	
	private static final String SPIDER_STATUS_PREFIX = "SPIDER_";
	private static final String AUTH_CONTEXT_PREFIX = "Context";
	private static final String AUTH_USER_PREFIX = "User";
	
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
	
	/**
	 * Maps authorized spider scan IDs with their corresponding contextId and userId (needed to start the active scan)
	 */
	Map<Long, String> scanContextIds;
	Map<Long, String> scanUserIds;
    
	String serviceHost;
	
	String servicePort;
	
	String serviceContext;
	
	
	@Value("${tools.zap.host}")
    public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}

	@Value("${tools.zap.port}")
	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	@Value("${tools.zap.context}")
	public void setServiceContext(String serviceContext) {
		this.serviceContext = serviceContext;
	}

	public ZapController() {
		super();
		this.restTemplate = new RestTemplate();
		this.spiderScans = new HashMap<>();
		this.scanContextIds = new HashMap<>();
		this.scanUserIds = new HashMap<>();
	}
    
    protected String getServiceUrl() {
    	return serviceHost + ":" + servicePort + "/" + serviceContext;
    }
    
    private void sendError(HttpServletResponse response, HttpStatus status, String message) {
    	logger.error(message);
    	try {
			response.sendError(status.value(), message);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	private boolean authParamsValid(ScanAuth scanAuth) {
		if ((scanAuth.getAuthUrl() == null) ||
			(scanAuth.getUsernameField() == null) ||	
			(scanAuth.getPasswordField() == null) ||	
			(scanAuth.getUsername() == null) ||	
			(scanAuth.getPassword() == null) ||	
			(scanAuth.getCheckLoggedInString() == null) ||
			(scanAuth.getCheckLoggedOutString() == null)) {	
			return false;
		}
		return true;
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

    	// Check if it's an authorized scan
    	String contextId = scanContextIds.get(spiderScanId);
    	String userId = scanUserIds.get(spiderScanId);
    	boolean authorization = (contextId != null) && (userId != null);
    	
    	// Start active scan or authorized active scan
    	ZapScan zapScan; 
    	
    	if (authorization) {
    		zapScan = restTemplate.getForObject(getServiceUrl() + "ascan/action/scanAsUser/" +
    				"?url=" + scanRequest.getUrl() +
    				"&contextId=" + contextId +
    				"&userId=" + userId, ZapScan.class);
    	}
    	else {
    		zapScan = restTemplate.getForObject(getServiceUrl() + "ascan/action/scan/?url=" + scanRequest.getUrl(),
    				ZapScan.class);
    	}
    	
    	spiderScans.put(spiderScanId, zapScan.getScanId());
    	
    	logger.info("ZAP Active Scan ID: " + zapScan.getScanId());
	}

    private ScanStatus getSpiderStatus(Long spiderScanId) {
    	ScanStatus spiderStatus = null;

    	// Request spider scans status list because it gives more information than a particular scan status request
    	ZapScansList spiderScans = restTemplate.getForObject(getServiceUrl() + "spider/view/scans/",
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

    @Override
	@RequestMapping(value = "/scans", method = RequestMethod.POST,
    		consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanResponse startScan(@RequestBody ScanRequest scanRequest, HttpServletResponse response) {
    	logger.info("ZAP Spider Scan: " + scanRequest.getUrl());

    	if ((scanRequest.getUrl() == null) || scanRequest.getUrl().isEmpty()) {
			sendError(response, HttpStatus.BAD_REQUEST, "Missing required parameter: url");
			return new ScanResponse();
    	}
    	
    	// Authentication
    	String contextId = null;
    	String userId = null;
    	if (scanRequest.getAuth() != null) {
    		ScanAuth scanAuth = scanRequest.getAuth();
    		if (authParamsValid(scanAuth)) {
    			long randomNumber = (long) (Math.random() * 1000000);
    			String contextName = AUTH_CONTEXT_PREFIX + randomNumber;
    			String userName = AUTH_USER_PREFIX + randomNumber;
    			
    			// Create new authentication context
    			ZapContext zapContext = restTemplate.getForObject(
    	    			getServiceUrl() + "context/action/newContext/?contextName=" + contextName, ZapContext.class);
    			if ((zapContext == null) || (zapContext.getContextId() == null)) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error creating authorization context");
					return new ScanResponse();
    			}
    			contextId = zapContext.getContextId();

    			// Include scan URL in context (with regex:  url.*)
    			Map<String, String> uriVariables = new HashMap<>();
    			uriVariables.put("contextName", contextName);
    			uriVariables.put("regex", scanRequest.getUrl() + ".*");
    			ZapResult zapResult = restTemplate.getForObject(getServiceUrl() + "context/action/includeInContext/" +
    					"?contextName={contextName}" +
    					"&regex={regex}", ZapResult.class, uriVariables);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error including URL in authorization context");
					return new ScanResponse();
    			}
    			
    			// Set authentication method and authentication params (form fields pattern)
    			String authMethod = "formBasedAuthentication";
    			String configParams = "loginUrl=" + scanAuth.getAuthUrl() + "&loginRequestData=" + 
    					scanAuth.getUsernameField() + "%3D%7B%25username%25%7D%26" +
    					scanAuth.getPasswordField() + "%3D%7B%25password%25%7D";
    			
    			zapResult = restTemplate.getForObject(getServiceUrl() + "authentication/action/setAuthenticationMethod/" +
    					"?contextId=" + contextId +
    					"&authMethodName=" + authMethod +
    					"&authMethodConfigParams=" + configParams, ZapResult.class);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error setting auth method and config params");
					return new ScanResponse();
    			}

    			// Set logged in indicator (regex)
    			zapResult = restTemplate.getForObject(getServiceUrl() + "authentication/action/setLoggedInIndicator/" +
    					"?contextId=" + contextId +
    					"&loggedInIndicatorRegex=" + scanAuth.getCheckLoggedInString(), ZapResult.class);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error setting loggedIn indicator");
					return new ScanResponse();
    			}
    			
    			// Set logged out indicator (regex)
    			zapResult = restTemplate.getForObject(getServiceUrl() + "authentication/action/setLoggedOutIndicator/" +
    					"?contextId=" + contextId +
    					"&loggedOutIndicatorRegex=" + scanAuth.getCheckLoggedOutString(), ZapResult.class);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error setting loggedOut indicator");
					return new ScanResponse();
    			}

    			// Create authentication user
    			ZapUser zapUser = restTemplate.getForObject(getServiceUrl() + "users/action/newUser/" + 
    					"?contextId=" + contextId + "&name=" + userName, ZapUser.class);
    			if ((zapUser == null) || (zapUser.getUserId() == null)) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error creating authorization user");
					return new ScanResponse();
    			}
    			userId = zapUser.getUserId();
    			
    			// Set user authentication credentials
    			uriVariables = new HashMap<>();
    			uriVariables.put("contextId", contextId);
    			uriVariables.put("userId", userId);
    			uriVariables.put("authCredentialsConfigParams", "username=" + scanAuth.getUsername() + "&password=" + scanAuth.getPassword());
    			zapResult = restTemplate.getForObject(getServiceUrl() + "users/action/setAuthenticationCredentials/" +
    					"?contextId={contextId}&userId={userId}&authCredentialsConfigParams={authCredentialsConfigParams}",
    					ZapResult.class, uriVariables);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error setting user credentials");
					return new ScanResponse();
    			}

    			// Enable user
    			zapResult = restTemplate.getForObject(getServiceUrl() + "users/action/setUserEnabled/" +
    					"?contextId=" + contextId +
    					"&userId=" + userId +
    					"&enabled=true", ZapResult.class);
    			if ((zapResult == null) || !zapResult.isOK()) {
					sendError(response, HttpStatus.BAD_REQUEST, "Error enabling user");
					return new ScanResponse();
    			}
    		}
    		else {
				sendError(response, HttpStatus.BAD_REQUEST, "Missing required authentication parameters "
						+ "(authUrl, usernameField, passwordField, username, password,"
						+ " checkLoggedInString, checkLoggedOutString)");
				return new ScanResponse();
    		}
    	}
    	
    	boolean authorization = (contextId != null) && (userId != null);
    	
    	// Spider scan
    	ZapScan spiderScan;
    	if (authorization) {
    		spiderScan = restTemplate.getForObject(getServiceUrl() + "spider/action/scanAsUser/" +
    				"?contextId=" + contextId +
    				"&userId=" + userId +
    				"&url=" + scanRequest.getUrl(), ZapScan.class);
    	}
    	else {
    		spiderScan = restTemplate.getForObject(
    			getServiceUrl() + "spider/action/scan/?url=" + scanRequest.getUrl(), ZapScan.class);
    	}

    	Long spiderScanId = spiderScan.getScanId();
    	logger.info("ZAP Spider Scan ID: " + spiderScanId);
    	
    	spiderScans.put(spiderScanId, NOT_STARTED_ACTIVE_SCAN_ID);
    	
    	if (authorization) {
    		// Authorization scan
    		scanContextIds.put(spiderScanId, contextId);
    		scanUserIds.put(spiderScanId, userId);
    	}

    	Thread spiderThread = new Thread(new SpiderThread(spiderScanId, scanRequest));
    	spiderThread.start();
    	
    	ScanResponse scanResponse = new ScanResponse();
    	scanResponse.setScanId("" + spiderScan.getScanId());
    	
    	return scanResponse;
    }
    
    @Override
    @RequestMapping(value = "/scans/{scanId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus getScanStatus(@PathVariable (required = true) String scanId, HttpServletResponse response) {
    	ScanStatus status = null;
    	
    	Long spiderScanId = Long.valueOf(scanId);
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "No ZAP scan with ID: " + scanId);
    		return new ScanStatus();
    	}

    	Long activeScanId = spiderScans.get(spiderScanId); 
		
    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		return getSpiderStatus(spiderScanId);
    	}
    	
    	// else: Spider finished, status from related active scan...
    	ZapScansList scans = restTemplate.getForObject(getServiceUrl() + "ascan/view/scans/",
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
    		sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "ZAP Scan with ID: " + scanId + " - Status NULL");
    		return new ScanStatus();
    	}
    	else {
    		logger.info("ZAP Scan status: " + scanId + " - " + status.getStatus() + ", " + status.getProgress() + "%");
    	}
    	
    	return status;    	
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/pause", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus pauseScan(@PathVariable (required = true) String scanId, HttpServletResponse response) {
    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "No ZAP scan with ID: " + scanId);
    		return new ScanStatus();
    	}
    	
    	Long activeScanId = spiderScans.get(spiderScanId); 

    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		// Pause spider scan
    		restTemplate.getForObject(getServiceUrl() + "spider/action/pause/?scanId=" + spiderScanId, Object.class);
    	}
    	else {
    		// Pause active scan
    		restTemplate.getForObject(getServiceUrl() + "ascan/action/pause/?scanId=" + activeScanId, Object.class);
    	}
		
		return getScanStatus(scanId, response);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/resume", method = RequestMethod.PUT,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanStatus resumeScan(@PathVariable (required = true) String scanId, HttpServletResponse response) {
    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "No ZAP scan with ID: " + scanId);
    		return new ScanStatus();
    	}
    	
    	Long activeScanId = spiderScans.get(Long.valueOf(scanId)); 

    	// Spider not finished yet
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		// Resume spider scan
    		restTemplate.getForObject(getServiceUrl() + "spider/action/resume/?scanId=" + spiderScanId, Object.class);
    	}
    	else {
    		// Resume active scan
    		restTemplate.getForObject(getServiceUrl() + "ascan/action/resume/?scanId=" + activeScanId, Object.class);
    	}
		
		return getScanStatus(scanId, response);
    }
    	
    @Override
    @RequestMapping(value = "/scans/{scanId}/report", method = RequestMethod.GET,
    		produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanReport getScanReport(@PathVariable (required = true) String scanId, HttpServletResponse response) {
    	ScanReport scanReport = new ScanReport();

    	Long spiderScanId = Long.valueOf(scanId);
    	
    	// Public Scan ID is Spider Scan ID
    	if (!spiderScans.containsKey(spiderScanId)) {
    		// Error... no spider scan with that ID
    		sendError(response, HttpStatus.INTERNAL_SERVER_ERROR, "No ZAP scan with ID: " + scanId);
    		return new ScanReport();
    	}
    	
    	Long activeScanId = spiderScans.get(spiderScanId); 

    	scanReport.setStatus(getScanStatus(scanId, response));

    	// Spider not finished yet, no alerts
    	if (activeScanId == NOT_STARTED_ACTIVE_SCAN_ID) {
    		return scanReport;
    	}
    	
    	// Active scan running or finished, get alerts
    	ZapScanAlertList alertList = restTemplate.getForObject(
    			getServiceUrl() + "ascan/view/alertsIds/?scanId=" + activeScanId, ZapScanAlertList.class);
    
    	for (String alertId : alertList.getAlertsIds()) {
    		ZapScanAlertResponse alertResponse = restTemplate.getForObject(
    				getServiceUrl() + "core/view/alert/?id=" + alertId, ZapScanAlertResponse.class);
    		
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
    		
    		AlertAttack attack = new AlertAttack();
    		attack.setParam(zapAlert.getParam());
    		attack.setEvidence(zapAlert.getEvidence());
    		alert.setAttack(attack);
    		
    		List<AlertReference> references = new ArrayList<>();
    		
    		// CWE
    		AlertReference reference = new AlertReference();
    		if ((zapAlert.getCweid() != null) && !zapAlert.getCweid().isEmpty()) {
	    		reference.setSource("CWE");
	    		reference.setId(zapAlert.getCweid());
	    		references.add(reference);
    		}
    		
    		// WASC
    		if ((zapAlert.getWascid() != null) && !zapAlert.getWascid().isEmpty()) {
	    		reference = new AlertReference();
	    		reference.setSource("WASC");
	    		reference.setId(zapAlert.getWascid());
	    		references.add(reference);
    		}
    		
    		// URL references
    		String zapReferences = zapAlert.getReference();
    		if ((zapReferences != null) && !zapReferences.isEmpty()) {
    			String[] refsArray = zapReferences.split(" |\n");
    			for (String ref : refsArray) {
    				reference = new AlertReference();
    				reference.setUrl(ref);
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
    		result = restTemplate.getForObject(getServiceUrl() + "spider/view/scans/", Object.class);
    	}
    	catch (RestClientException e) {
    	}
    	
    	return result != null;
    }
    
}
