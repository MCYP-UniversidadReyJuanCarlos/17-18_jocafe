package com.websectester.tools.w3af;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.websectester.model.ScanRequest;
import com.websectester.tools.w3af.W3afController;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
public class W3afControllerTests {
	
	private static final String TOOL_PATH = "/tools/w3af/scans/";
	
	private static final String VULNERABLE_APP_URL = "http://testphp.vulnweb.com";
	

	@Value("${tools.w3af.host}")
	String serviceHost;

	@Value("${tools.w3af.port}")
	String servicePort;

	@Value("${tools.w3af.context}")
	String serviceContext;


	@Test
    public void launchAndControlScan() throws Exception {

		W3afController w3afController = new W3afController();
		w3afController.setServiceHost(serviceHost);
		w3afController.setServicePort(servicePort);
		w3afController.setServiceContext(serviceContext);
		
    	RestAssuredMockMvc.standaloneSetup(w3afController);
    	
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
    	

    	// W3af can't pause scans
/*    	
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
*/    	

    	// Get scan results
    	when().
    		get(TOOL_PATH + scanId + "/report").
    	then().
    		statusCode(200).
    		body("status", isA(String.class), "progress", isA(String.class), "alerts", is(notNullValue()));
	
    }

}
