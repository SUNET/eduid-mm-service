package se.sunet.mm.service.mmclient;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

import javax.net.ssl.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.io.FileInputStream;
import java.security.*;


/***
 * Base class for all svc services, supposed to be sub classed
 */
public class ClientBase {
    private String wsBaseEndpoint;
    private KeyStore keyStore;

    public ClientBase(String wsBaseEndpoint) {
        this.wsBaseEndpoint = wsBaseEndpoint;
    }

    public String getWsBaseEndpoint() {
        return wsBaseEndpoint;
    }

    /***
     * Get svc port for provided port type and service class
     *
     * @param portType
     * @param serviceInterface
     * @param urlEndpoint
     * @param <T>
     * @return
     * @throws Exception
     */
    protected <T> T getPort(Class<T> portType, Class<? extends Service> serviceInterface, String urlEndpoint) throws Exception {
        Service svc;
        String serviceName = serviceInterface.getSimpleName();
        String endpoint = urlEndpoint;

        if (endpoint == null) {
            endpoint = String.format("%s/%s", wsBaseEndpoint, serviceName);
        }

        svc = instantiate(serviceInterface);
        T port = svc.getPort(portType);

        // This binding needs to be first or the conduit modifications are lost
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);


        keyStore = KeyStore.getInstance(System.getProperty("javax.net.ssl.keyStoreType"));
        keyStore.load(new FileInputStream(
                        System.getProperty("javax.net.ssl.keyStore")),
                        System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
        KeyManager[] keyManagers = getKeyManagers();

        // Setup tls and keyStore stuff
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setKeyManagers(keyManagers);
        HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
        conduit.setTlsClientParameters(tlsClientParameters);

        return port;
    }

    private <T> T instantiate(Class<T> cls) throws Exception {
        try {
            return cls.getConstructor().newInstance();
        } catch (InstantiationException e) {
            throw new Exception(e);
        } catch (IllegalAccessException e) {
            throw new Exception(e);
        }
    }

    private KeyManager[] getKeyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory factory = KeyManagerFactory.getInstance(algorithm);
        factory.init(keyStore, System.getProperty("javax.net.ssl.keyStorePassword").toCharArray());
        return factory.getKeyManagers();
    }
}
