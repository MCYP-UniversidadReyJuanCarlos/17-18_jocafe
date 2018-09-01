package com.websectester.tools.zap;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.websectester.model.ScanRequest;
import com.websectester.tools.zap.ZapController;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZapControllerTests {
	
	private static final String TOOL_PATH = "/tools/zap/scans/";
	
	private static final String VULNERABLE_APP_URL = "http://testphp.vulnweb.com";
	
	
	@Value("${tools.zap.host}")
	String serviceHost;

	@Value("${tools.zap.port}")
	String servicePort;

	@Value("${tools.zap.context}")
	String serviceContext;

	
    @Test
    public void launchAndControlScan() throws Exception {

    	ZapController zapController = new ZapController();
    	zapController.setServiceHost(serviceHost);
    	zapController.setServicePort(servicePort);
    	zapController.setServiceContext(serviceContext);
    	
    	RestAssuredMockMvc.standaloneSetup(zapController);
    	
    	// Start a new scan
    	String scanId =
    	given().
    		accept(ContentType.JSON).
    		contentType(ContentType.JSON).
    		body(new ScanRequest(VULNERABLE_APP_URL)).
    	when().
        	post(TOOL_PATH).
        then().
	        statusCode(200).
	        body("scanId", isA(String.class)).  
    	extract().
        	path("scanId");
    	
    	// Get scan status
    	when().
    		get(TOOL_PATH + scanId).
    	then().
    		statusCode(200).
    		body("status", isA(String.class), "progress", isA(String.class));
    	
    	// Pause scan
    	when().
			put(TOOL_PATH + scanId + "/pause").
		then().
			statusCode(200).
			body("status", isA(String.class), "progress", isA(String.class)).and().
			body("status", containsString("PAUSED"));
	
    	// Resume scan
    	when().
			put(TOOL_PATH + scanId + "/resume").
		then().
			statusCode(200).
			body("status", isA(String.class), "progress", isA(String.class));
    	
    	// Get scan results
    	when().
    		get(TOOL_PATH + scanId + "/report").
    	then().
    		statusCode(200).
    		body("status", isA(String.class), "progress", isA(String.class), "alerts", is(notNullValue()));
	
    }

}
