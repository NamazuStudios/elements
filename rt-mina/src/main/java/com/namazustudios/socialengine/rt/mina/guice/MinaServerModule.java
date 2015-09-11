package com.namazustudios.socialengine.rt.mina.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.mina.Constants;
import com.namazustudios.socialengine.rt.mina.ServerBSONProtocolDecoder;
import com.namazustudios.socialengine.rt.mina.ServerBSONProtocolEncoder;
import com.namazustudios.socialengine.rt.mina.ServerIOHandler;
import de.undercouch.bson4jackson.BsonFactory;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.guice.MinaModule;
import org.apache.mina.guice.filter.InjectProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import javax.inject.Inject;


/**
 * Created by patricktwohig on 9/3/15.
 */
public class MinaServerModule extends MinaModule {

    @Override
    protected void configureMINA() {

        bind(IoAcceptor.class).toProvider(new Provider<NioSocketAcceptor>() {

            @Inject
            private Provider<IoHandler> ioHandlerProvider;

            @Inject
            private Provider<IoFilterChainBuilder> ioFilterChainBuilderProvider;

            @Override
            public NioSocketAcceptor get() {
                final NioSocketAcceptor nioSocketAcceptor = new NioSocketAcceptor();
                nioSocketAcceptor.getSessionConfig().setTcpNoDelay(true);
                nioSocketAcceptor.setHandler(ioHandlerProvider.get());
                nioSocketAcceptor.setFilterChainBuilder(ioFilterChainBuilderProvider.get());
                return nioSocketAcceptor;
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
