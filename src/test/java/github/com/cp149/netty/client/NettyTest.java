package github.com.cp149.netty.client;

import github.com.cp149.netty.server.NettyappenderServer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.qos.logback.classic.LoggerContext;

/**
 * @author netty test create a netty server then run nettyappend class by mvn
 *         ,count how many lines the netty server received
 */
public class NettyTest {

	protected String testclass = "-Dtest=github.com.cp149.netty.client.NettyAppenderTest";
	protected String configFile = this.getClass().getResource("").getFile() + File.separator + "logbackserver.xml";
	protected String logfilename = "logs/logback-server-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
	protected  int expecttotal = AppenderBaseTest.loglines * 100 + AppenderBaseTest.WARMLOGSIZE+1;
	private NettyappenderServer nettyappenderServer;
	

	/**
	 * @throws Exception
	 *             do config
	 */
	@BeforeMethod(alwaysRun = true)
	public void befortest() throws Exception {
		// CountAppender.count.set(0);
		File file = new File(logfilename);
		if (file.exists())
			file.delete();
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		NettyappenderServer.configureLC(lc, configFile);
		// start netty server
		nettyappenderServer = new NettyappenderServer(4560);
		nettyappenderServer.run();
		
	}

	/**
	 * @throws Exception
	 *             close nettyserver
	 */
	@AfterMethod(alwaysRun = true)
	public void aftertest() throws Exception {
		nettyappenderServer.shutdown();
	}

	@Test( groups = "nettytest")
	public void testNettyclientandserver() throws Exception {
		// run client
		new MvnCommandexe().executeCommands("mvn.bat", testclass, "test");
		// check log lines
		int totallogs = Testutils.countlines(logfilename);

		while (totallogs < expecttotal) {
			totallogs = Testutils.countlines(logfilename);
			TimeUnit.SECONDS.sleep(2);
			System.out.println(this.getClass().getSimpleName() + " current lines =" + totallogs + " expert " + expecttotal);
		}
		System.out.println(Thread.currentThread().getStackTrace()[1] + "last total=" + totallogs);
		// CountAppender's count should equal expect total
//		Assert.assertEquals(CountAppender.count.intValue(), expecttotal);
		//
		Assert.assertEquals(Testutils.countlines(logfilename), expecttotal);

	}

}
