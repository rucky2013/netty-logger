package github.com.cp149.netty.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.Test;

public class DisruptorNettyTest extends NettyTest {
	public DisruptorNettyTest() {
		super();
		this.testclass = "-Dtest=github.com.cp149.netty.client.DisruptorAppenderTest";
		configFile = this.getClass().getResource("").getFile() + File.separator + "logbackserver-disruptor.xml";
		logfilename = "logs/logback-server-disruptor" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
	}

	@Test(timeOut = 40000, groups = "nettytest")
	public void testNettyclientandserver() throws Exception {
		super.testNettyclientandserver();
	}

}
