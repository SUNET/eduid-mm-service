package se.sunet.mm.service.server;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

public class EmbeddedServer {

    private Server server;

    public EmbeddedServer() {}

    private Properties getProperties(String configFile) throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = new FileInputStream(configFile);
        prop.load(inputStream);
        return prop;
    }

    private ServletContextHandler getServletContext(String packagesLocation, String rootPath) {
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES, packagesLocation);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(sh, rootPath);
        return context;
    }

    private SslContextFactory getSslContextFactory(String keyStorePath, String keyStorePassword, String keyManagerPassword) {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        return sslContextFactory;
    }

    private Server getSslServer(String host, Integer port, SslContextFactory sslContextFactory) {
        Server server = new Server();
        HttpConfiguration https_config = new HttpConfiguration();
        https_config.setSecureScheme("https");
        https_config.setSecurePort(port);
        https_config.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(https_config);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,"http/1.1");
        ServerConnector https = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        https.setHost(host);
        https.setPort(port);
        https.setIdleTimeout(500000);
        server.addConnector(https);
        return server;
    }

    public void start(String configFile) throws Exception {
        Properties prop = getProperties(configFile);
        String host = prop.getProperty("host");
        Integer port = Integer.parseInt(prop.getProperty("port"));
        String packagesLocation = prop.getProperty("apiPackagesPath");
        String rootPath = prop.getProperty("apiRootPath");
        Boolean https = Boolean.parseBoolean(prop.getProperty("https"));

        if (!https) {
            InetSocketAddress address = new InetSocketAddress(host, port);
            server = new Server(address);
        } else {
            String keyStorePath = prop.getProperty("keyStorePath");
            String keyStorePassword = prop.getProperty("keyStorePassword");
            String keyManagerPassword = prop.getProperty("keyManagerPassword");
            SslContextFactory sslContextFactory = getSslContextFactory(keyStorePath, keyStorePassword, keyManagerPassword);
            server = getSslServer(host, port, sslContextFactory);
        }

        ServletContextHandler servletContext = getServletContext(packagesLocation, rootPath);
        server.setHandler(servletContext);
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
