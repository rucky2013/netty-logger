package github.com.cp149.netty.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.Test;

public class JactorNettyTest extends NettyTest {

	public JactorNettyTest() {
		super();
		this.testclass = "-Dtest=github.com.cp149.netty.client.JactorNettyAppenderTest";
		configFile = this.getClass().getResource("").getFile() + File.separator + "logbackserver-jactor.xml";
		logfilename = "logs/logback-server-netty" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
	}

	@Test(timeOut = 30000, groups = "nettytest")
	public void testNettyclientandserver() throws Exception {
		super.testNettyclientandserver();
	}

}
