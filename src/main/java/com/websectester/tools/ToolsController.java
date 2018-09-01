package com.websectester.tools;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.websectester.model.Tool;
import com.websectester.tools.arachni.ArachniController;
import com.websectester.tools.w3af.W3afController;
import com.websectester.tools.zap.ZapController;

@RestController
@RequestMapping("/tools")
public class ToolsController {
	
	@Autowired
	ArachniController arachniController;
	
	@Autowired
	W3afController w3afController;
	
	@Autowired
	ZapController zapController;
	
	@RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tool> getTools() {
    	List<Tool> tools = new ArrayList<>();
    	tools.add(new Tool("OWASP ZAP", "zap", zapController.isToolAvailable()));
    	tools.add(new Tool("Arachni", "arachni", arachniController.isToolAvailable()));
    	tools.add(new Tool("W3af", "w3af", w3afController.isToolAvailable()));
    	return tools;
    }

}
