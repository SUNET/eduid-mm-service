package se.sunet.mm.service.api;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import se.sunet.mm.service.server.EmbeddedServer;

/**
 * Created by lundberg on 2014-05-22.
 */
public class SetupCommon {

    public EmbeddedServer embeddedServer = new EmbeddedServer();
    public String configFile = "./src/test/resources/mm-service.properties";
    public static final String TEST_PERSON_NIN = "192705178354";
    public static final String TEST_PERSON_FAILING_NIN = "191212121212";

    @BeforeClass
    public void setUp() throws Exception {
        setupServer();
    }

    @AfterClass
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
