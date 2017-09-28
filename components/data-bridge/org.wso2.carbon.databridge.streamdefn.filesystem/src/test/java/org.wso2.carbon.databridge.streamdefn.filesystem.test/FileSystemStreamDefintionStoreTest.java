package org.wso2.carbon.databridge.streamdefn.filesystem.test;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.filesystem.FileSystemStreamDefinitionStore;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.FileSystemStreamDefnStoreDS;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.ServiceHolder;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.core.internal.EventStreamRuntime;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class FileSystemStreamDefintionStoreTest extends FileSystemStreamDefnStoreDS {
    private static Logger log = Logger.getLogger(FileSystemStreamDefintionStoreTest.class);
    public static final Path testDir = Paths.get("src", "test", "resources");
    private static FileSystemStreamDefinitionStore streamDefinitionStore;
    private static StreamDefinition streamDefinition;

    private static final String STREAM_NAME = "org.wso2.esb.MediatorStatistics";
    private static final String VERSION = "1.0.0";

    private static final String STREAM_DEFN = "{" +
            "  'name':'" + STREAM_NAME + "'," +
            "  'version':'" + VERSION + "'," +
            "  'nickName': 'Stock Quote Information'," +
            "  'description': 'Some Desc'," +
            "  'tags':['foo', 'bar']," +
            "  'metaData':[" +
            "          {'name':'ipAdd','type':'STRING'}" +
            "  ]," +
            "  'payloadData':[" +
            "          {'name':'symbol','type':'STRING'}," +
            "          {'name':'price','type':'DOUBLE'}," +
            "          {'name':'volume','type':'INT'}," +
            "          {'name':'max','type':'DOUBLE'}," +
            "          {'name':'min','type':'Double'}" +
            "  ]" +
            "}";

    private static FileSystemStreamDefinitionStore getStreamDefinitionStore() {
        if (streamDefinitionStore == null) {
            streamDefinitionStore = new FileSystemStreamDefinitionStore();
        }
        return streamDefinitionStore;
    }

    private static void setupCarbonConfig() {
        System.setProperty("carbon.home", Paths.get(testDir.toString(), "carbon-context").toString());
        System.setProperty("tenant.name", "tenant.name");
        System.setProperty("portOffset", "0");
    }

    @BeforeClass
    public static void init() throws MalformedURLException {
        setupCarbonConfig();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(-1234);
        AxisConfiguration axisConfig = new AxisConfiguration();

        URL url = new URL(testDir.toString());
        axisConfig.setRepository(url);
        try {
            streamDefinition = EventDefinitionConverterUtils.convertFromJson(STREAM_DEFN);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
        }
        EventStreamServiceValueHolder.registerEventStreamRuntime(new EventStreamRuntime());
        CarbonEventStreamService carbonEventStreamService = new CarbonEventStreamService();
        EventStreamServiceValueHolder.setCarbonEventStreamService(carbonEventStreamService);

        ServiceHolder.setEventStreamService(carbonEventStreamService);
        ServiceHolder.setStreamDefinitionStore(getStreamDefinitionStore());
        if (log.isDebugEnabled()) {
            log.debug("Successfully deployed EventStreamService");
        }
    }

    @Test
    public void testSaveStreamDefinitionToStore() {
        try {
            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(STREAM_DEFN);
            getStreamDefinitionStore().saveStreamDefinitionToStore(streamDefinition, -1234);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            log.error("Error while saving the stream definition", e);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();
            log.error("Malformated Stream Definition : " + e.getMessage(), e);
        }
    }

    /* @Test*/
    public void testSaveStreamDefinitionToStoreWithInvalidTenantID() {
        try {
            getStreamDefinitionStore().saveStreamDefinitionToStore(streamDefinition, -1);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
            log.error("Error while saving the stream definition", e);
        }
    }

    @Test
    public void testGetStreamDefinitionFromStore() {
        testSaveStreamDefinitionToStore();
        StreamDefinition streamDef = null;
        Boolean expected = false;
        try {
            streamDef = getStreamDefinitionStore().getStreamDefinitionFromStore(STREAM_NAME, VERSION, -1234);
        } catch (StreamDefinitionStoreException e) {
            e.printStackTrace();
        }
        if (streamDefinition.equals(streamDef)) {
            expected = true;
        }
        Assert.assertTrue("StreamDefinition is retrieved successfully ", expected);
    }

    @Test
    public void testGetAllStreamDefinitionsFromStore() throws StreamDefinitionStoreException {
        testSaveStreamDefinitionToStore();
        Collection<StreamDefinition> streamDefs = getStreamDefinitionStore().getAllStreamDefinitionsFromStore(-1234);
    }
}
