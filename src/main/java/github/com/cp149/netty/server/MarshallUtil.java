package github.com.cp149.netty.server;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;



public class MarshallUtil {
	public static MarshallerFactory createMarshallerFactory() {
		return Marshalling.getProvidedMarshallerFactory("serial");
	}

	public static MarshallingConfiguration createMarshallingConfig() {
		// Create a configuration
		final MarshallingConfiguration configuration = new MarshallingConfiguration();
		configuration.setVersion(5);
		return configuration;
	}

	public static UnmarshallerProvider createUnProvider() {
		
		return new DefaultUnmarshallerProvider(createMarshallerFactory(), createMarshallingConfig());

	}

	public static MarshallerProvider createProvider() {
		return new DefaultMarshallerProvider(createMarshallerFactory(), createMarshallingConfig());
	}

}
