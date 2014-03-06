package github.com.cp149.netty.server;

import java.io.IOException;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEventVO;

public class NettyudpServerHandler extends SimpleChannelHandler {
	private static final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(NettyudpServerHandler.class);
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		System.out.print(e.toString());
		 LoggingEventVO event = ((LoggingEventVO) e.getMessage());
				Logger remoteLogger = lc.getLogger(event.getLoggerName());
				// apply the logger-level filter
				if (remoteLogger.isEnabledFor(event.getLevel())) {
					event.getCallerData();
					// finally log the event as if was generated locally
					remoteLogger.callAppenders(event);
				}
	}
	

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {	
		// Close the connection when an exception is raised.
		if (!(e instanceof IOException))
			logger.warn("Unexpected exception from downstream.", e.getCause());

		
	}

}
