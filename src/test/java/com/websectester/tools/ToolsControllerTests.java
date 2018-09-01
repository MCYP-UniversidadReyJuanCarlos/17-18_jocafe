package com.websectester.tools;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.websectester.tools.arachni.ArachniController;
import com.websectester.tools.w3af.W3afController;
import com.websectester.tools.zap.ZapController;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ToolsControllerTests {
	
	@Value("${tools.arachni.host}")
	String arachniServiceHost;

	@Value("${tools.arachni.port}")
	String arachniServicePort;

	@Value("${tools.arachni.context}")
	String arachniServiceContext;

	@Value("${tools.w3af.host}")
	String w3afServiceHost;

	@Value("${tools.w3af.port}")
	String w3afServicePort;

	@Value("${tools.w3af.context}")
	String w3afServiceContext;

	@Value("${tools.zap.host}")
	String zapServiceHost;

	@Value("${tools.zap.port}")
	String zapServicePort;

	@Value("${tools.zap.context}")
	String zapServiceContext;


    @Test
    public void allToolsAvailable() throws Exception {

    	ToolsController toolsController = new ToolsController();
    	
    	ArachniController arachniController = new ArachniController();
		arachniController.setServiceHost(arachniServiceHost);
		arachniController.setServicePort(arachniServicePort);
		arachniController.setServiceContext(arachniServiceContext);
		
		W3afController w3afController = new W3afController();
		w3afController.setServiceHost(w3afServiceHost);
		w3afController.setServicePort(w3afServicePort);
		w3afController.setServiceContext(w3afServiceContext);
		
		ZapController zapController = new ZapController();
		zapController.setServiceHost(zapServiceHost);
		zapController.setServicePort(zapServicePort);
		zapController.setServiceContext(zapServiceContext);
		
		toolsController.arachniController = arachniController;
		toolsController.w3afController = w3afController;
		toolsController.zapController = zapController;

    	RestAssuredMockMvc.standaloneSetup(toolsController);
    	
    	when().
        	get("/tools").
        then().
	        statusCode(200).
	        body("identifier", hasItems("zap", "arachni", "w3af")).
	        body("name", hasItems("OWASP ZAP", "Arachni", "W3af")).
	        body("available", equalTo(Arrays.asList(new Boolean[] {true, true, true})));  

    }

}
