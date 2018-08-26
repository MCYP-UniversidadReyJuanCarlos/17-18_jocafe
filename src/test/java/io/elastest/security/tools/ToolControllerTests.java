package io.elastest.security.tools;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import org.assertj.core.util.Arrays;
import org.junit.Test;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

public class ToolControllerTests {
	
    @Test
    public void allToolsAvailable() throws Exception {

    	RestAssuredMockMvc.standaloneSetup(new ToolController());
    	
    	when().
        	get("/tools").
        then().
	        statusCode(200).
	        body("identifier", hasItems("zap", "arachni", "w3af")).
	        body("name", hasItems("ZAP", "Arachni", "W3af")).
	        body("available", equalTo(Arrays.asList(new Boolean[] {true, true, true})));  

    }

}
