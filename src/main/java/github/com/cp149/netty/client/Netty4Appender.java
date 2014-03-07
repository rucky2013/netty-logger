package github.com.cp149.netty.client;

import github.com.cp149.netty.server.MarshallUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.marshalling.MarshallingEncoder;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.PreSerializationTransformer;

public class Netty4Appender extends NetAppenderBase<ILoggingEvent> {
	protected PreSerializationTransformer<ILoggingEvent> pst = new LoggingEventPreSerializationTransformer();
	protected Bootstrap bootstrap = null;
	protected EventLoopGroup group;

	
	protected int channelSize = 1;
	protected Channel[] channelList;
	ChannelFuture lastWriteFuture = null;
	int channelid = 0;

	protected Channel getChannel() {
		channelid=(channelid+1)%channelSize;
		return channelList[channelid];		
	}
	@Override
	protected void append(ILoggingEvent eventObject) {
		try {
			if (isStarted()) {
				// if not start then start bootstrap
				if (bootstrap == null)
					connect(address, port);
				// eventObject.prepareForDeferredProcessing();
				eventObject.getCallerData();
				final Serializable serEvent = getPST().transform(eventObject);
				// if connect write to server
				try {
					Channel channel=getChannel();
					if (channel != null && channel.isOpen()) {

						lastWriteFuture = channel.writeAndFlush(serEvent);

					} else {
						System.out.println(eventObject);
						// else write to local
						aai.appendLoopOnAppenders(eventObject);
					}
				} catch (Exception e) {
					aai.appendLoopOnAppenders(eventObject);
					System.out.println(e);
					if (e != null)
						addError(e.getMessage());
				}

			} else {
				System.out.println(eventObject);
				// else write to local
				aai.appendLoopOnAppenders(eventObject);
			}

		} catch (Exception e) {
			System.out.println(e);
			addError(e.getMessage());
		}

	}

	@Override
	public void cleanUp() {
		try {

			if (bootstrap != null) {
				// Wait until all messages are flushed before closing the
				// channel.

				if (lastWriteFuture != null) {
					lastWriteFuture.awaitUninterruptibly();
				}

				group.shutdownGracefully().sync();

				group = null;
				bootstrap = null;
			}

		} catch (Exception e) {
			addError(e.getMessage());
		}

	}

	@Override
	public synchronized void connect(InetAddress address, int port) {
		if (bootstrap == null) {
			// final EventExecutorGroup executor = new
			// DefaultEventExecutorGroup(10);
			bootstrap = new Bootstrap();
			group = new NioEventLoopGroup();

			bootstrap
					.group(group)
					.channel(NioSocketChannel.class)
					// .option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					// .option(ChannelOption.SO_RCVBUF, 20)
//					.option(ChannelOption.SO_SNDBUF, 0)
					.remoteAddress(new InetSocketAddress(address, port))
					.handler(new ChannelInitializer<NioSocketChannel>() {
						@Override
						public void initChannel(NioSocketChannel ch)
								throws Exception {
							// ch.pipeline().addLast(
							// new IdleStateHandler(0, 0, 60,
							// TimeUnit.SECONDS));
							// ch.pipeline().addLast(new
							// ReadTimeoutHandler(30));
							// ch.pipeline().addLast(new ObjectEncoder(),
							// new
							// ObjectDecoder(ClassResolvers.cacheDisabled(null)));
							ch.pipeline().addLast(
									new MarshallingEncoder(MarshallUtil
											.createProvider()));
							// ch.pipeline().addLast(new
							// MarshallingDecoder(MarshallUtil.createUnProvider()));
							// ch.pipeline().addLast(executor, new
							// AppenderClientHandler());
						}
					});

			
			channelList = new Channel[channelSize];
			for (int i = 0; i < channelSize; i++) {
				ChannelFuture future;
				try {
					future = bootstrap.connect().sync();
					Channel channel = future.channel();
					channelList[i] = channel;
				} catch (InterruptedException e) {
					
					addError(e.getMessage());
				}
				
			}		

		}

	}

	@Override
	protected PreSerializationTransformer<ILoggingEvent> getPST() {

		return pst;
	}

	public void setChannelSize(int channelSize) {
		this.channelSize = channelSize;
	}

}
