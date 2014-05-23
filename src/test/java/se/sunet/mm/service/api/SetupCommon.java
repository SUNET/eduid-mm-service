package se.sunet.mm.service.api;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import se.sunet.mm.service.server.EmbeddedServer;

/**
 * Created by lundberg on 2014-05-22.
 */
public class SetupCommon {

    public EmbeddedServer embeddedServer = new EmbeddedServer();
    public String configFile = "./src/test/resources/mm-service.properties";
    public static final String TEST_PERSON_NIN = "192705178354";

    @BeforeTest
    public void setUp() throws Exception {
        setupServer();
    }

    @AfterTest
    public void tearDown() throws Exception {
        tearDownServer();
    }

    public void setupServer() throws Exception {
        this.embeddedServer.setup(configFile);
        this.embeddedServer.start();
    }

    public void tearDownServer() throws Exception {
        this.embeddedServer.stop();
    }
}
