package io.elastest.security.tools;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.security.model.ScanReport;
import io.elastest.security.model.ScanRequest;
import io.elastest.security.model.ScanResponse;
import io.elastest.security.model.ScanStatus;

public interface ToolController {

    ScanResponse startScan(@RequestBody ScanRequest scanRequest);
    
    ScanStatus getScanStatus(@PathVariable (required = true) String scanId);

    ScanStatus pauseScan(@PathVariable (required = true) String scanId);
    
    ScanStatus resumeScan(@PathVariable (required = true) String scanId);
    
    ScanReport getScanReport(@PathVariable (required = true) String scanId);
    
    boolean isToolAvailable();

}
