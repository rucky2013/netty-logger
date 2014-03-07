/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package github.com.cp149.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.marshalling.CompatibleMarshallingDecoder;
import io.netty.handler.codec.marshalling.CompatibleMarshallingEncoder;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.CompatibleObjectEncoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Echoes back any received data from a client.
 */
public class NettyappenderServer {

	private final int port;
	private ServerBootstrap bootstrap;
	EventLoopGroup bossGroup = new NioEventLoopGroup(8);
	EventLoopGroup workerGroup = new NioEventLoopGroup(8);

	public ServerBootstrap getBootstrap() {
		return bootstrap;
	}

	public NettyappenderServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {

		try {			
			bootstrap = new ServerBootstrap();
			final EventExecutorGroup executor = new DefaultEventExecutorGroup(16);

			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_RCVBUF, 65535)
					.childOption(ChannelOption.SO_SNDBUF, 2048).childOption(ChannelOption.SO_REUSEADDR,true) //reuse address
					.childOption(ChannelOption.ALLOCATOR,new PooledByteBufAllocator(false))// heap buf 's better					
					.childHandler(new ChannelInitializer<SocketChannel>() {
						
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
//							ch.pipeline().addLast( new MarshallingEncoder(MarshallUtil.createProvider()));
//							ch.pipeline().addLast(new CompatibleObjectDecoder());
//							ch.pipeline().addLast(new ObjectEncoder(),
//		                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
							ch.pipeline().addLast(new MarshallingDecoder(MarshallUtil.createUnProvider()));
							
							ch.pipeline().addLast(executor, new NettyappenderServerHandler());
							//
						}
					});
			
			bootstrap.bind(port).sync();
			
		} finally {

		}
		// bootstrap = new ServerBootstrap(new
		// NioServerSocketChannelFactory(Executors.newFixedThreadPool(4),
		// Executors.newFixedThreadPool(4)));
		// final ExecutionHandler executionHandler = new ExecutionHandler(new
		// OrderedMemoryAwareThreadPoolExecutor(4, 1024 * 1024 * 300, 1024 *
		// 1024 * 300 * 2));
		// bootstrap.setOption("tcpNoDelay", true);
		// bootstrap.setOption("keepAlive", true);
		// // bootstrap.setOption("writeBufferHighWaterMark", 100 * 64 * 1024);
		// // bootstrap.setOption("sendBufferSize", 1048576);
		// bootstrap.setOption("receiveBufferSize", 1048576*10 );
		//
		// // Set up the pipeline factory.
		// bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
		// public ChannelPipeline getPipeline() throws Exception {
		// return Channels.pipeline(executionHandler, new
		// MarshallingDecoder(createProvider(createMarshallerFactory(),
		// createMarshallingConfig())),
		// new NettyappenderServerHandler());
		// }
		// });
		
		// // Bind and start to accept incoming connections.
		// bootstrap.bind(new InetSocketAddress(port));
//		 LoggerFactory.getLogger(this.getClass()).info("start server at" +
//				 port);
	}

	public void shutdown() {
		if (bootstrap != null) {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();

		}
	}

	public static void main(String[] args) throws Exception {
		int port = 4560;
		int timeout = 0;
		if (args.length > 0) {
			timeout = Integer.parseInt(args[0]);
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		configureLC(lc, NettyappenderServer.class.getResource("").getFile() + File.separator + "logbackserver.xml");

		NettyappenderServer nettyappenderServer = new NettyappenderServer(port);
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