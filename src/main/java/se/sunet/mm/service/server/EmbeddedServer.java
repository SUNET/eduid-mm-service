package se.sunet.mm.service.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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

    private ServletContextHandler getContext(String packagesLocation, String rootPath) {
        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter(ServerProperties.PROVIDER_PACKAGES, packagesLocation);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet(sh, rootPath);
        return context;
    }

    public void start(String configFile) throws Exception {
        Properties prop = getProperties(configFile);
        String host = prop.getProperty("host");
        Integer port = Integer.parseInt(prop.getProperty("port"));
        InetSocketAddress address = new InetSocketAddress(host, port);
        server = new Server(address);
        ServletContextHandler context = getContext("se.sunet.mm.service.api", "/*");
        server.setHandler(context);
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
