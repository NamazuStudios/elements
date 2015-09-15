package com.namazustudios.socialengine.rt.mina.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.mina.ServerBSONProtocolDecoder;
import com.namazustudios.socialengine.rt.mina.ServerBSONProtocolEncoder;
import com.namazustudios.socialengine.rt.mina.ServerIOHandler;
import de.undercouch.bson4jackson.BsonFactory;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.guice.MinaModule;
import org.apache.mina.guice.filter.InjectProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 9/11/15.
 */
public class MinaClientModule extends MinaModule {

    @Override
    protected void configureMINA() {

        bind(IoConnector.class).annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
           .toProvider(new Provider<IoConnector>() {

               @Inject
               private Provider<IoHandler> ioHandlerProvider;

               @Inject
               private Provider<IoFilterChainBuilder> ioFilterChainBuilderProvider;

               @Override
               public IoConnector get() {
                   final NioSocketConnector nioSocketConnector = new NioSocketConnector();
                   nioSocketConnector.getSessionConfig().setTcpNoDelay(true);
                   nioSocketConnector.setHandler(ioHandlerProvider.get());
                   nioSocketConnector.setFilterChainBuilder(ioFilterChainBuilderProvider.get());
                   return nioSocketConnector;
               }

           });

        bind(IoConnector.class).annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
            .toProvider(new Provider<IoConnector>() {

                @Inject
                private Provider<IoHandler> ioHandlerProvider;

                @Inject
                private Provider<IoFilterChainBuilder> ioFilterChainBuilderProvider;

                @Override
                public IoConnector get() {
                    final NioDatagramConnector nioSocketConnector = new NioDatagramConnector();
                    nioSocketConnector.setHandler(ioHandlerProvider.get());
                    nioSocketConnector.setFilterChainBuilder(ioFilterChainBuilderProvider.get());
                    return nioSocketConnector;
                }

            });

        bindFilter().named("logging").atAndOfFilterChain().to(LoggingFilter.class);
        bindFilter().named("codec").atAndOfFilterChain().to(InjectProtocolCodecFilter.class);

        bindFilterChainBuilder();
        bindProtocolCodecFactory();
        bind(ProtocolEncoder.class).to(ServerBSONProtocolEncoder.class);
        bind(ProtocolDecoder.class).to(ServerBSONProtocolDecoder.class);

        bind(IoHandler.class).to(ServerIOHandler.class);

        bind(ObjectMapper.class)
            .annotatedWith(Names.named(Constants.BSON_OBJECT_MAPPER))
            .toProvider(new Provider<ObjectMapper>() {
                @Override
                public ObjectMapper get() {
                    final ObjectMapper objectMapper = new ObjectMapper(new BsonFactory());
                    return objectMapper;
                }
            });

    }

}
