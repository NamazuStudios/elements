package com.namazustudios.socialengine.rt.mina.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.mina.BSONObjectMapperProvider;
import com.namazustudios.socialengine.rt.mina.BSONProtocolDecoder;
import com.namazustudios.socialengine.rt.mina.BSONProtocolEncoder;
import com.namazustudios.socialengine.rt.mina.ServerIOHandler;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.guice.MinaModule;
import org.apache.mina.guice.filter.InjectProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import javax.inject.Inject;
import java.util.zip.Adler32;
import java.util.zip.Checksum;


/**
 * Created by patricktwohig on 9/3/15.
 */
public class MinaServerModule extends MinaModule {

    @Override
    protected void configureMINA() {

        bind(IoAcceptor.class).annotatedWith(Names.named(Constants.TRANSPORT_RELIABLE))
            .toProvider(new Provider<NioSocketAcceptor>() {

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

        bind(IoAcceptor.class).annotatedWith(Names.named(Constants.TRANSPORT_BEST_EFFORT))
            .toProvider(new Provider<NioDatagramAcceptor>() {

                @Inject
                private Provider<IoHandler> ioHandlerProvider;

                @Inject
                private Provider<IoFilterChainBuilder> ioFilterChainBuilderProvider;

                @Override
                public NioDatagramAcceptor get() {
                    final NioDatagramAcceptor nioDatagramAcceptor = new NioDatagramAcceptor();
                    nioDatagramAcceptor.setHandler(ioHandlerProvider.get());
                    nioDatagramAcceptor.setFilterChainBuilder(ioFilterChainBuilderProvider.get());
                    return nioDatagramAcceptor;
                }

            });

        bindIoSession();

        bindFilter().named("logging").atAndOfFilterChain().to(LoggingFilter.class);
        bindFilter().named("codec").atAndOfFilterChain().to(InjectProtocolCodecFilter.class);

        bindFilterChainBuilder();
        bindProtocolCodecFactory();
        bind(ProtocolEncoder.class).to(BSONProtocolEncoder.class);
        bind(ProtocolDecoder.class).to(BSONProtocolDecoder.class);

        bind(IoHandler.class).to(ServerIOHandler.class);

        bind(ObjectMapper.class)
            .annotatedWith(Names.named(Constants.BSON_OBJECT_MAPPER))
            .toProvider(Providers.guicify(BSONObjectMapperProvider.getInstance()));

        binder().bindConstant()
                .annotatedWith(Names.named(Constants.MAX_ENVELOPE_SIZE))
                .to(maxEnvelopeSize());

        binder().bind(Checksum.class).to(Adler32.class);

    }

    /**
     * Override to adjust the max envelope size.  For maximum performance and reliability
     * applications should target for maximum envelope sizes around the size of the
     * MTU.
     *
     * @return the max envelope size
     */
    public int maxEnvelopeSize() {
        return 4096;
    }

}
