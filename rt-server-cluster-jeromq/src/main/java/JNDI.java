import com.namazustudios.socialengine.rt.remote.JndiInstanceHostInfo;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

public class JNDI {

    public static void main(final String[] args) throws NamingException {

        final var env = new Hashtable<>();
        env.put(Context.AUTHORITATIVE, "true");
        env.put(Context.PROVIDER_URL, "dns://127.0.0.1:5353");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

        final var context = new InitialDirContext(env);

        for (int i = 0; i < 100; ++i) {
            final var result = context.getAttributes("_elements._tcp.internal", new String[]{"SRV"});
            final var attributes = result.get("srv");
            final var nfos = JndiInstanceHostInfo.parse("tcp", attributes);
            System.out.println("Host Info: " + nfos);
        }

        System.out.println("Done!");

    }

}
