package io.elastest.security.tools.arachni;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import io.elastest.security.model.ScanRequest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

public class ArachniControllerTests {
	
	private static final String TOOL_PATH = "/tools/arachni/scans/";

	private static final String VULNERABLE_APP_URL = "http://testphp.vulnweb.com";

	
	@Test
    public void launchAndControlScan() throws Exception {

    	RestAssuredMockMvc.standaloneSetup(new ArachniController());
    	
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
			body("status", containsString("PAUSING"));
	
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
