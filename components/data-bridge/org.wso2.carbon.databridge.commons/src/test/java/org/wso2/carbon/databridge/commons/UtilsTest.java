package org.wso2.carbon.databridge.commons;

import junit.framework.TestCase;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.DataBridgeThreadFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sajith on 9/26/17.
 */
public class UtilsTest extends TestCase {

    @Test
    public void testSystemPropertyReplacement() {
        System.setProperty("someProperty", "someValue");
        String value = DataBridgeCommonsUtils.replaceSystemProperty("${someProperty}");
        assertTrue(value.equals("someValue"));

        //Replacing carbon.home
        System.setProperty("carbon.home", ".");
        value = DataBridgeCommonsUtils.replaceSystemProperty("${carbon.home}");
        assertTrue(value.equals(new File(".").getAbsolutePath() + File.separator + "."));
    }

    @Test
    public void testGetStringSize() throws UnsupportedEncodingException {
        int textSize = DataBridgeCommonsUtils.getSize("someText");
        assertTrue(8 == textSize);
    }

    @Test
    public void testGetEventSize() {
        Map<String, String> arbitaryMap = new HashMap<>();
        arbitaryMap.put("arbitraryField", "arbitraryValue");
        Event event = new Event();
        event.setStreamId("sample.stream.id");
        event.setPayloadData(new Object[] {new Integer(1), new Long(1)});
        event.setMetaData( new Object[] { new Boolean(true), new Double(1.0)});
        event.setCorrelationData(new Object[] {new Float(1.0), new String("text")});
        event.setArbitraryDataMap(arbitaryMap);

        int eventSize = DataBridgeCommonsUtils.getSize(event);

        assertTrue(145 == eventSize);
    }

    @Test
    public void testStreamIDDecodeing() {
        String streamId = "sample.stream.id:1.0.0";
        String streamName = DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId);
        String streamVersion = DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId);

        assertEquals(streamName, "sample.stream.id");
        assertEquals(streamVersion, "1.0.0");
    }

    @Test
    public void testReferenceSize() {
        int referenceSize = DataBridgeCommonsUtils.getReferenceSize();
        int wordSize = Integer.parseInt(System.getProperty("sun.arch.data.model"));

        assertEquals(referenceSize, wordSize/8);
    }


    @Test
    public void testCreatingNewThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });

        Thread dataBridgeThread = new DataBridgeThreadFactory("DataBridgeThread").newThread(t);
        assertTrue(dataBridgeThread != null);
        assertTrue(dataBridgeThread.getName().startsWith("DataBridge"));
        assertTrue(dataBridgeThread.getPriority() == Thread.NORM_PRIORITY);
        assertTrue(dataBridgeThread.isDaemon() == false);
    }
}