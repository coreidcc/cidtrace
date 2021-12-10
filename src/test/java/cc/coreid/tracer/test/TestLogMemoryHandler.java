package cc.coreid.tracer.test;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TestLogMemoryHandler extends Handler {
    final ArrayList<LogRecord> logR;

    public TestLogMemoryHandler() {
        logR = new ArrayList<LogRecord>();
    }

    /**
     * Gives access to the previously logged messages
     */
    public String getMessage(int i) {
        return logR.get(i).getMessage();
    }

    public int getMessageCount() {
        return logR.size();
    }

    @Override
    public void publish(LogRecord record) {
        logR.add(record);
    }

    @Override
    public void flush() {
        logR.clear();
    }

    @Override
    public void close() throws SecurityException {
    }
}
