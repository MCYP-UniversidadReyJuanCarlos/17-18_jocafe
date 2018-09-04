package com.websectester.tools.w3af;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.websectester.model.ScanRequest;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
public class W3afControllerTests {
	
	private static final String TOOL_PATH = "/tools/w3af/scans/";
	

	@Value("${tools.w3af.host}")
	String serviceHost;

	@Value("${tools.w3af.port}")
	String servicePort;

	@Value("${tools.w3af.context}")
	String serviceContext;

	@Value("${test.vulnerable_app.url}")
	String vulnerableAppURL;

	@Autowired
	W3afController w3afController;


	@Test
    public void launchAndControlScan() throws Exception {

		if (w3afController == null) {
			w3afController = new W3afController();
		}
		w3afController.setServiceHost(serviceHost);
		w3afController.setServicePort(servicePort);
		w3afController.setServiceContext(serviceContext);
		
    	RestAssuredMockMvc.standaloneSetup(w3afController);
    	
    	// Start a new scan
    	String scanId =
    	given().
    		accept(ContentType.JSON).
    		contentType(ContentType.JSON).
    		body(new ScanRequest(vulnerableAppURL)).
    	when().
        	post(TOOL_PATH).
        then().
			statusCode(equalTo(200)).
	        body("scanId", isA(String.class)).  
    	extract().
        	path("scanId");
    	
    	// Wait 1 sec
    	Thread.sleep(1000);
    	
    	// Get scan status
    	when().
    		get(TOOL_PATH + scanId).
    	then().
			statusCode(equalTo(200)).
    		body("status", isA(String.class), "progress", isA(String.class));
    	

    	// W3af can't pause scans
/*    	
		// Pause scan
		when().
			put(TOOL_PATH + scanId + "/pause").
		then().
    		statusCode(equalTo(200)).
			body("status", isA(String.class), "progress", isA(String.class)).and().
			body("status", containsString("PAUSED"));
	
    	// Resume scan
    	when().
			put(TOOL_PATH + scanId + "/resume").
		then().
    		statusCode(equalTo(200)).
			body("status", isA(String.class), "progress", isA(String.class));
*/    	

    	// Wait 10 secs
    	Thread.sleep(10000);
    	
    	// Get scan results
    	when().
    		get(TOOL_PATH + scanId + "/report").
    	then().
			statusCode(equalTo(200)).
    		body(matchesJsonSchemaInClasspath("report-schema.json"));
	
    }

}
