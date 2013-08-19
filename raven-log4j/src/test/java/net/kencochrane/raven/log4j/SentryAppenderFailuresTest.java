package net.kencochrane.raven.log4j;

import mockit.*;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.dsn.Dsn;
import net.kencochrane.raven.event.Event;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SentryAppenderFailuresTest {
    private SentryAppender sentryAppender;
    private MockUpErrorHandler mockUpErrorHandler;
    @Injectable
    private Raven mockRaven = null;
    @Injectable
    private Logger mockLogger = null;

    @BeforeMethod
    public void setUp() throws Exception{
        sentryAppender = new SentryAppender(mockRaven);
        mockUpErrorHandler = new MockUpErrorHandler();
        sentryAppender.setErrorHandler(mockUpErrorHandler.getMockInstance());
    }

    @Test
    public void testRavenFailureDoesNotPropagate() throws Exception {
        new NonStrictExpectations() {{
            mockRaven.sendEvent((Event) any);
            result = new UnsupportedOperationException();
        }};

        sentryAppender.append(new LoggingEvent(null, mockLogger, 0, Level.INFO, null, null));

        new Verifications() {{
            mockRaven.sendEvent((Event) any);
            assertThat(mockUpErrorHandler.getErrorCount(), is(1));
        }};
    }

    @Test
    public void testRavenFactoryFailureDoesNotPropagate() throws Exception {
        new Expectations() {
            @Mocked("ravenInstance")
            private RavenFactory ravenFactory;

            {
                RavenFactory.ravenInstance((Dsn) any, anyString);
                result = new UnsupportedOperationException();
            }
        };
        SentryAppender sentryAppender = new SentryAppender();
        sentryAppender.setErrorHandler(mockUpErrorHandler.getMockInstance());
        sentryAppender.setDsn("protocol://public:private@host/1");

        sentryAppender.activateOptions();

        assertThat(mockUpErrorHandler.getErrorCount(), is(1));
    }
}