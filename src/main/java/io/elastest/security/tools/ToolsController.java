package io.elastest.security.tools;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.security.model.Tool;
import io.elastest.security.tools.arachni.ArachniController;
import io.elastest.security.tools.w3af.W3afController;
import io.elastest.security.tools.zap.ZapController;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/tools")
public class ToolsController {
	
	@Autowired
	ZapController zapController;
	
	@Autowired
	ArachniController arachniController;
	
	@Autowired
	W3afController w3afController;

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tool> getTools() {
    	List<Tool> tools = new ArrayList<>();
    	tools.add(new Tool("ZAP", "zap", zapController.isToolAvailable()));
    	tools.add(new Tool("Arachni", "arachni", arachniController.isToolAvailable()));
    	tools.add(new Tool("W3af", "w3af", w3afController.isToolAvailable()));
    	return tools;
    }

}
