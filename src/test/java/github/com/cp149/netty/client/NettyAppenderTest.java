package github.com.cp149.netty.client;

import org.testng.annotations.Test;

/**
 * @author cp149 nettyappender test ,call by nettytest
 */
public class NettyAppenderTest extends AppenderBaseTest {

	@Test(invocationCount = 100, threadPoolSize = 30)
	public void testLog() throws Exception {
		for (int i = 0; i < loglines; i++)
			logback.debug("logback test jasocket {} at thread {}", i, Thread.currentThread().getId());

	}

	public NettyAppenderTest() {
		super();
		isNettyappender = true;
		Logfile = "logback-server-";
	}

}
