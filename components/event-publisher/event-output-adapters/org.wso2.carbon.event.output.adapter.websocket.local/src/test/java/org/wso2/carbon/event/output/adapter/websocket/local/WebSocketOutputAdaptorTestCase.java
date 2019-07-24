package org.wso2.carbon.event.output.adapter.websocket.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * .
 */
public class WebSocketOutputAdaptorTestCase {
    private static final Log logger = LogFactory.getLog(WebSocketOutputAdaptorTestCase.class);
    private static final Path testDir = Paths.get("src", "test", "resources");

    private void setupCarbonConfig() {
        System.setProperty("carbon.home",
                Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
    }


}
