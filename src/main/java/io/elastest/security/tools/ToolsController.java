package io.elastest.security.tools;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.security.model.Tool;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/tools")
public class ToolsController {

    @RequestMapping(method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tool> getTools() {
    	List<Tool> tools = new ArrayList<>();
    	
    	//TODO get the real tools' status
    	
    	tools.add(new Tool("ZAP", "zap", true));
    	tools.add(new Tool("Arachni", "arachni", true));
    	tools.add(new Tool("W3af", "w3af", true));
    	return tools;
    }

}
