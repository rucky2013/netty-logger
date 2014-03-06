package github.com.cp149.netty.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class NettyUdpServer {
	private final int port;
	private ConnectionlessBootstrap bootstrap;

	public ConnectionlessBootstrap getBootstrap() {
		return bootstrap;
	}

	public NettyUdpServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {

		try {
			
			DatagramChannelFactory udpChannelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
			bootstrap = new ConnectionlessBootstrap(udpChannelFactory);
			final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 1024 * 1024 * 300,
					1024 * 1024 * 300 * 2));
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(executionHandler,new org.jboss.netty.handler.codec.marshalling.MarshallingDecoder(
							new org.jboss.netty.handler.codec.marshalling.DefaultUnmarshallerProvider(createMarshallerFactory(),
									createMarshallingConfig())), new NettyudpServerHandler());
				}
			});
			bootstrap.setOption("broadcast", "false");
			bootstrap.setOption("sendBufferSize", 65536);
			bootstrap.setOption("receiveBufferSize", 65536);
			SocketAddress serverAddress = new InetSocketAddress(port);
			bootstrap.bind(serverAddress);

		} finally {

		}

		LoggerFactory.getLogger(this.getClass()).info("start server at" + port);
	}

	private MarshallerFactory createMarshallerFactory() {
		return Marshalling.getProvidedMarshallerFactory("serial");
	}

	private MarshallingConfiguration createMarshallingConfig() {
		// Create a configuration
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		// configuration.setVersion(5);
		return configuration;
	}

	public void shutdown() {
		if (bootstrap != null) {
			bootstrap.releaseExternalResources();
			bootstrap.shutdown();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 4570;
		int timeout = 0;
		if (args.length > 0) {
			timeout = Integer.parseInt(args[0]);
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		configureLC(lc, NettyappenderServer.class.getResource("").getFile() + File.separator + "logbackserver.xml");

		NettyUdpServer nettyappenderServer = new NettyUdpServer(port);
		// success lines
		int successlines = 0;
		if (args.length > 1) {
			successlines = Integer.parseInt(args[0]);
		}

		nettyappenderServer.run();
		// if timeout >0 then autoshutdown after timeout,just for unit test
		if (timeout > 0) {
			TimeUnit.SECONDS.sleep(timeout);
			lc.getLogger(NettyappenderServer.class).debug("shut down");
			nettyappenderServer.shutdown();

			System.exit(0);
		}
	}

	static public void configureLC(LoggerContext lc, String configFile) throws JoranException {
		JoranConfigurator configurator = new JoranConfigurator();
		lc.reset();
		configurator.setContext(lc);
		configurator.doConfigure(configFile);
	}

}
