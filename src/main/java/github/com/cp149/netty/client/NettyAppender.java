package github.com.cp149.netty.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.util.HashedWheelTimer;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class NettyAppender extends NetAppenderBase<ILoggingEvent> {
	protected PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();
	protected ClientBootstrap bootstrap = null;
	protected int channelSize = 5;

	protected org.jboss.netty.channel.Channel[] channelList;
	private org.jboss.netty.channel.Channel channel;

	int channelid = 0;

	protected Channel getChannel() {
		if (channelid >= channelSize)
			channelid = 0;
		return channelList[channelid++];
		// return channel;
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
				Channel channel = getChannel();
				if (channel.isConnected())
					channel.write(serEvent);
				else
					// else write to local
					aai.appendLoopOnAppenders(eventObject);

			}

		} catch (Exception e) {
			addError(e.getMessage());
		}

	}

	@Override
	public void cleanUp() {
		try {
			if (bootstrap != null) {
				for (int i = 0; i < channelList.length; i++) {
					channelList[i].disconnect().awaitUninterruptibly();
					channelList[i].close();
				}

				// channel.disconnect().awaitUninterruptibly();
				// channel.close();
				bootstrap.releaseExternalResources();
				bootstrap.shutdown();

				bootstrap = null;
			}
		} catch (Exception e) {
			addError(e.getMessage());
		}

	}

	@Override
	public synchronized void connect(InetAddress address, int port) {
		if (bootstrap == null) {

			final ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(4, 1024 * 1024 * 100,
					1024 * 1024 * 100 * 2));
			bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newFixedThreadPool(4),
					Executors.newFixedThreadPool(4)));
			bootstrap.setOption("tcpNoDelay", true);
			bootstrap.setOption("keepAlive", true);
			bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));

			bootstrap.setOption("sendBufferSize", 1048576 );

			// Set up the pipeline factory.
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

				public ChannelPipeline getPipeline() throws Exception {
					// new
					// org.jboss.netty.handler.timeout.ReadTimeoutHandler(new
					// HashedWheelTimer(),30),
					return Channels.pipeline(executionHandler, new org.jboss.netty.handler.codec.marshalling.MarshallingEncoder(
							new org.jboss.netty.handler.codec.marshalling.DefaultMarshallerProvider(createMarshallerFactory(),
									createMarshallingConfig())));
				}
			});
			channelList = new Channel[channelSize];
			for (int i = 0; i < channelSize; i++) {
				org.jboss.netty.channel.ChannelFuture 				
					future = bootstrap.connect();
					channel = future.getChannel();
					channelList[i] = channel;
				

			}
		}

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
