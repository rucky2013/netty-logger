package github.com.cp149.netty.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.DatagramChannel;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class NettyUdpAppender extends NetAppenderBase<ILoggingEvent> {
	protected PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();
	protected ConnectionlessBootstrap bootstrap = null;
	private DatagramChannel channel;
	private InetSocketAddress remoteAddress ;

	@Override
	public void cleanUp() {
		try {
			if (bootstrap != null) {
				channel.disconnect().awaitUninterruptibly();
				channel.close();
				bootstrap.releaseExternalResources();
				bootstrap.shutdown();
				bootstrap = null;
			}
		} catch (Exception e) {
			addError(e.getMessage());
		}

	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		try {
			if (isStarted()) {
				// if not start then start bootstrap
				if (connectatstart == false && bootstrap == null)
					connect(address, port);
				// eventObject.prepareForDeferredProcessing();
				eventObject.getCallerData();
				Serializable serEvent = getPST().transform(eventObject);
				// if connect write to server

				if (channel.isOpen()) {
					channel.write(serEvent,remoteAddress );
				} else
					// else write to local
					aai.appendLoopOnAppenders(eventObject);

			}

		} catch (Exception e) {
			addError(e.getMessage());
		}

	}

	@Override
	public void connect(InetAddress address, int port) {
		bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(Executors.newCachedThreadPool()));
		final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 1024 * 1024 * 300,
				1024 * 1024 * 300 * 2));
		ChannelPipeline p = bootstrap.getPipeline();
		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {

				return Channels.pipeline(executionHandler, new org.jboss.netty.handler.codec.marshalling.MarshallingEncoder(
						new org.jboss.netty.handler.codec.marshalling.DefaultMarshallerProvider(createMarshallerFactory(),
								createMarshallingConfig())));
			}
		});
		remoteAddress= new InetSocketAddress(address, port);
		bootstrap.setOption("broadcast", "false");
		bootstrap.setOption("sendBufferSize", 65536);
		bootstrap.setOption("receiveBufferSize", 65536);

		channel = (DatagramChannel) bootstrap.bind(new InetSocketAddress(0));

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

	@Override
	protected PreSerializationTransformer<ILoggingEvent> getPST() {
		return pst;
	}

}
