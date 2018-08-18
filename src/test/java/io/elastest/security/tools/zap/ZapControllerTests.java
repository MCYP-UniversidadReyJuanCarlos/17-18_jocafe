package io.elastest.security.tools.zap;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import io.elastest.security.model.ScanRequest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

public class ZapControllerTests {

    @Test
    public void launchAndControlScan() throws Exception {

    	RestAssuredMockMvc.standaloneSetup(new ZapController());
    	
    	// Start a new scan
    	String scanId =
    	given().
    		accept(ContentType.JSON).
    		contentType(ContentType.JSON).
    		body(new ScanRequest("http://testphp.vulnweb.com")).
    	when().
        	post("/tools/zap/scans").
        then().
	        statusCode(200).
	        body("scanId", isA(String.class)).  
    	extract().
        	path("scanId");
    	
    	// Get scan status
    	when().
    		get("tools/zap/scans/" + scanId).
    	then().
    		statusCode(200).
    		body("status", isA(String.class), "progress", isA(String.class));
    	
    	// Pause scan
    	when().
			put("tools/zap/scans/" + scanId + "/pause").
		then().
			statusCode(200).
			body("status", isA(String.class), "progress", isA(String.class)).and().
			body("status", containsString("PAUSED"));
	
    	// Resume scan
    	when().
		put("tools/zap/scans/" + scanId + "/resume").
		then().
			statusCode(200).
			body("status", isA(String.class), "progress", isA(String.class));
	
    }

}
