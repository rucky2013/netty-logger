package github.com.cp149.netty.client;

import org.testng.annotations.Test;

public class DisruptorAppenderTest extends AppenderBaseTest {

	@Test(invocationCount = 100, threadPoolSize = 30)
	public void testLog() throws Exception {
		for (int i = 0; i < loglines; i++)
			logback.debug("logback test disruptro {} at thread {}", i, Thread.currentThread().getId());

	}

	public DisruptorAppenderTest() {
		super();
		LOGBACK_XML = "logback-disruptor.xml";
		Logfile = "logback-server-disruptor";
		isNettyappender = true;
	}

}
