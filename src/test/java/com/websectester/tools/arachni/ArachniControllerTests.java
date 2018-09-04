package com.websectester.tools.arachni;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.CoreMatchers.containsString;
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
public class ArachniControllerTests {
	
	private static final String TOOL_PATH = "/tools/arachni/scans/";

	@Value("${tools.arachni.host}")
	String serviceHost;

	@Value("${tools.arachni.port}")
	String servicePort;

	@Value("${tools.arachni.context}")
	String serviceContext;
	
	@Value("${test.vulnerable_app.url}")
	String vulnerableAppURL;
	
	@Autowired
	ArachniController arachniController;

	
	@Test
    public void launchAndControlScan() throws Exception {

		if (arachniController == null) {
			arachniController = new ArachniController();
		}
		arachniController.setServiceHost(serviceHost);
		arachniController.setServicePort(servicePort);
		arachniController.setServiceContext(serviceContext);
		
    	RestAssuredMockMvc.standaloneSetup(arachniController);
    	
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
    	
    	// Wait 1 sec
    	Thread.sleep(1000);
    	
    	// Pause scan
    	when().
			put(TOOL_PATH + scanId + "/pause").
		then().
			statusCode(equalTo(200)).
			body("status", isA(String.class), "progress", isA(String.class)).and().
			body("status", containsString("PAUSING"));
	
    	// Wait 1 sec
    	Thread.sleep(1000);
    	
    	// Resume scan
    	when().
			put(TOOL_PATH + scanId + "/resume").
		then().
			statusCode(equalTo(200)).
			body("status", isA(String.class), "progress", isA(String.class));
    	
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
