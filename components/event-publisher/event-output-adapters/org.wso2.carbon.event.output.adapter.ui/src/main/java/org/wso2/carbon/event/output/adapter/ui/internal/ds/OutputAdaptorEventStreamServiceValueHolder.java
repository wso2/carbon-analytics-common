package org.wso2.carbon.event.output.adapter.ui.internal.ds;

import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class holds a reference to the current EventStream OSGI service component "eventStreamService.component" in the
 * OSGI runtime.
 */
public class OutputAdaptorEventStreamServiceValueHolder {
    private static EventStreamService eventStreamService;

    public static void registerEventStreamService(EventStreamService eventBuilderService) {
        OutputAdaptorEventStreamServiceValueHolder.eventStreamService = eventBuilderService;
    }

    public static EventStreamService getEventStreamService() {
        return OutputAdaptorEventStreamServiceValueHolder.eventStreamService;
    }

    public static void unregisterEventStreamService(EventStreamService eventStreamService) {
        OutputAdaptorEventStreamServiceValueHolder.eventStreamService = null;
    }
}
