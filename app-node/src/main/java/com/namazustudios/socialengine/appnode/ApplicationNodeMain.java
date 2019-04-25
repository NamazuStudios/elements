package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.rt.jeromq.RouteRepresentationUtil;
import com.namazustudios.socialengine.rt.remote.CommandPreamble;
import com.namazustudios.socialengine.rt.jeromq.Connection;
import com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder;
import com.namazustudios.socialengine.rt.remote.StatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import java.util.Properties;

import static com.namazustudios.socialengine.appnode.Constants.CONTROL_REQUEST_TIMEOUT;
import static com.namazustudios.socialengine.remote.jeromq.JeroMQDemultiplexedConnectionService.CONTROL_BIND_PORT;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.STATUS_REQUEST;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static java.lang.String.format;
import static org.zeromq.ZMQ.REQ;

/**
 * Hello world!
 *
 */
public class ApplicationNodeMain {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMain.class);

    public static void main(final String[] args) {

        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        // quick and dirty arg check - may want to leverage command processing from Setup module

        for (String arg : args) {
            if(arg.equalsIgnoreCase("--status-check")) {
                final Properties properties = defaultConfigurationSupplier.get();

                final Integer port = Integer.parseInt(properties.getProperty(CONTROL_BIND_PORT));
                final String statusCheckAddress = RouteRepresentationUtil.buildTcpAddress("*", port);

                logger.info(format("Performing status check on %s...", statusCheckAddress));

                boolean result = false;

                try (ZContext context = new ZContext()) {
                    try (final Connection connection = from(context, c -> c.createSocket(REQ))) {
                        connection.socket().connect(properties.getProperty(statusCheckAddress));

                        try {
                            connection.socket().setReceiveTimeOut(Integer.parseInt(properties.getProperty(CONTROL_REQUEST_TIMEOUT)));
                        }
                        catch (NumberFormatException e) {
                            // use default timeout
                        }

                        ControlMessageBuilder.send(connection.socket(), STATUS_REQUEST,  new StatusRequest().getByteBuffer());

                        final ZMsg resp = ZMsg.recvMsg(connection.socket());

                        if(null != resp) {
                            final CommandPreamble respPreamble = new CommandPreamble();

                            respPreamble.getByteBuffer().put(resp.pop().getData());

                            if(respPreamble.commandType.get() == CommandPreamble.CommandType.STATUS_RESPONSE) {
                                // actual content of the StatusResponse is not important right now

                                result = true;
                            }

                        }

                    }

                }

                logger.info("Status check {}", result ? "OK" : "FAIL");
                logger.info("Shutting down.");

                System.exit(result ? 0 : -1);
            }
        }

        final ApplicationNode applicationNode = new ApplicationNode(defaultConfigurationSupplier);
        applicationNode.start();
    }

}
