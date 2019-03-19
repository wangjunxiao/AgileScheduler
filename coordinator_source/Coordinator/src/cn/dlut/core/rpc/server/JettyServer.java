package cn.dlut.core.rpc.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;

import cn.dlut.core.rpc.JSONRPCAPI;

/**
 * Run a JSON RPC web server that supports both http and https. Creates two
 * roles (admin and status) each with an exposed resource (/admin and /status).
 *
 */
public class JettyServer implements Runnable {

    private static Logger log = LogManager.getLogger(JettyServer.class
            .getName());

    private JSONRPCAPI service = null;
    private Server server = null;
    /**
     * Web server realm name.
     */
    public static final String REALM = "CoordinatorREALM";

    /**
     * Constructs and initializes a web server.
     *
     * @param port
     *            the port on which to run the web server
     */
    public JettyServer(final int port) {
        this.service = new JSONRPCAPI();
        this.init(port);
    }

    /**
     * Initializes API web server.
     *
     * @param port
     *            the port on which to run the web server
     */
    private void init(final int port) {
        log.info("Initializing API WebServer on port {}", port);
        this.server = new Server(port);

        /*final String sslKeyStore = System.getProperty("javax.net.ssl.keyStore");
        if (sslKeyStore == null) {
            throw new RuntimeException(
                    "Property javax.net.ssl.keyStore not defined; missing keystore file:"
                            + "Use startup script to start Coordinator");
        }
        if (!new File(sslKeyStore).exists()) {
            throw new RuntimeException(
                    "SSL Key Store file not found: '"
                            + sslKeyStore
                            + " make sure you installed Coordinator correctly : see Installation manual");
        }*/

        // HTTP Configuration
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(32768);
        //httpConfig.setSecureScheme("https");
        //httpConfig.setSecurePort(8443);
       
        // HTTP connector
        final ServerConnector http = new ServerConnector(this.server,
                new HttpConnectionFactory(httpConfig));
        http.setPort(port);
        http.setIdleTimeout(30000);

        // SSL Context Factory for HTTPS and SPDY
		// final SslContextFactory sslContextFactory = new SslContextFactory();
        /*sslContextFactory.setKeyStorePath(sslKeyStore);
        sslContextFactory
                .setKeyStorePassword("OBF:1lbw1wg41sox1kfx1vub1w8t1idn1zer1zej1igj1w8x1vuz1kch1sot1wfu1lfm");
        sslContextFactory
                .setKeyManagerPassword("OBF:1ym71u2g1uh61l8h1l4t1ugk1u2u1ym7");*/

        // HTTPS Configuration
		// final HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        // httpsConfig.addCustomizer(new SecureRequestCustomizer());

        // HTTPS connector 
        /* final ServerConnector https = new ServerConnector(this.server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpsConfig));
        https.setPort(8443);
        https.setIdleTimeout(500000);*/

        // Set the connectors
        //this.server.setConnectors(new Connector[] {http, https});
        this.server.setConnectors(new Connector[] {http});
        
        //final Constraint adminConstraint = new Constraint();
        //adminConstraint.setName(Constraint.NONE);
        //adminConstraint.setName(Constraint.__BASIC_AUTH);
        //adminConstraint.setRoles(new String[] {"admin"});
        //adminConstraint.setAuthenticate(true);

        //final Constraint statusConstraint = new Constraint();
        //statusConstraint.setName(Constraint.NONE);
        //statusConstraint.setName(Constraint.NONE.__BASIC_AUTH);
        //statusConstraint.setRoles(new String[] {"status"});
        //statusConstraint.setAuthenticate(true);

        //final ConstraintMapping adminmapping = new ConstraintMapping();
        //adminmapping.setConstraint(adminConstraint);
        //adminmapping.setPathSpec("/admin");

        //final ConstraintMapping statusmapping = new ConstraintMapping();
        //statusmapping.setConstraint(statusConstraint);
        //statusmapping.setPathSpec("/status");

        final ResourceHandler sh = new ResourceHandler();
    
        //final ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
        //final DefaultHandler sh =new org.eclipse.jetty.server.handler.DefaultHandler();
        //sh.setRealmName(JettyServer.REALM);
        //sh.setConstraintMappings(new ConstraintMapping[] {adminmapping,statusmapping});
        //sh.setAuthenticator(new BasicAuthenticator());       
        
        sh.setHandler(this.service);
        
        //final LoginService loginSrv = new CoordinatorLoginService();
        //sh.setLoginService(loginSrv);

        this.server.setHandler(sh);
    }

    @Override
    public void run() {
        try {
            this.server.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            this.server.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

}
