package io.elastest.security.tools;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.security.model.Tool;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tool> getTools() {
    	List<Tool> tools = new ArrayList<>();
    	
    	//TODO get the real tools' status
    	
    	tools.add(new Tool("ZAP", "zap", true));
    	tools.add(new Tool("Arachni", "arachni", true));
    	tools.add(new Tool("W3af", "w3af", false));
    	return tools;
    }

}
