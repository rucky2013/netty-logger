package github.com.cp149.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.io.Serializable;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEventVO;

public class NettyappenderServerHandler extends SimpleChannelInboundHandler<Serializable> {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(NettyappenderServerHandler.class);
	private static final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		// Close the connection when an exception is raised.
		if (!(e instanceof IOException))
			logger.warn("Unexpected exception from downstream.", e);

		ctx.close();
	}

	

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, Serializable eve) throws Exception {
				
				try {
					
					if(eve instanceof LoggingEventVO){
						LoggingEventVO	event=(LoggingEventVO)eve;
					Logger remoteLogger = lc.getLogger(event.getLoggerName());
					// apply the logger-level filter
					if (remoteLogger.isEnabledFor(event.getLevel())) {
						//					event.getCallerData();
						// finally log the event as if was generated locally
						remoteLogger.callAppenders(event);
					}
					}
				} finally{
					
				}
				

		
	}



	
	
	
}