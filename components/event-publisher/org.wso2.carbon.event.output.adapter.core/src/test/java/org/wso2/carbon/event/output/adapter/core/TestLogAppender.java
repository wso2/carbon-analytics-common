package org.wso2.carbon.event.output.adapter.core;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestLogAppender extends AbstractAppender {

    private final List<LogEvent> log = new ArrayList<>();

    public TestLogAppender(String name, Filter filter, Layout<? extends Serializable> layout) {

        super(name, filter, layout);
    }

    List<LogEvent> getLog() {

        return new ArrayList<LogEvent>(log);
    }

    @Override
    public void append(LogEvent logEvent) {

        log.add(logEvent);
    }
}
