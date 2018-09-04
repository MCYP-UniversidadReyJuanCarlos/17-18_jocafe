package com.websectester.tools;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.websectester.model.ScanReport;
import com.websectester.model.ScanRequest;
import com.websectester.model.ScanResponse;
import com.websectester.model.ScanStatus;

public interface ToolController {

    ScanResponse startScan(@RequestBody ScanRequest scanRequest, HttpServletResponse response);
    
    ScanStatus getScanStatus(@PathVariable (required = true) String scanId);

    ScanStatus pauseScan(@PathVariable (required = true) String scanId);
    
    ScanStatus resumeScan(@PathVariable (required = true) String scanId);
    
    ScanReport getScanReport(@PathVariable (required = true) String scanId);
    
    boolean isToolAvailable();

}
