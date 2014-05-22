package se.sunet.mm.service.mmclient;

import org.testng.annotations.BeforeTest;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


public class SetupCommon  {
    private static final String KEY_STORE_PATH = "./src/test/resources/Kommun_B.p12";
    private static final String KEY_STORE_PASSWORD = "7374645261023221";
    private static final String TRUST_KEYSTORE_PATH = "./src/test/resources/trust.jks";
    private static final String TRUST_KEYSTORE_PASSWORD = "trustedcerts";
    public static final String TEST_PERSON_NIN = "192705178354";
    public static final String SENDER_ORG_NR = "162021003898";


    @BeforeTest
    public void setUp() throws Exception {
        setupKeystore();
    }

    private void setupKeystore() {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.keyStoreType");

        System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");

        System.setProperty("javax.net.ssl.trustStore", TRUST_KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUST_KEYSTORE_PASSWORD);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        //System.setProperty("javax.net.debug", "ssl, handshake, failure");
    }
}
