package se.sunet.mm.service.api;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Server;
import org.testng.annotations.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import java.util.HashMap;

import static org.testng.Assert.*;

/**
 * Created by lundberg on 2014-06-23.
 */
public class MessageTest extends SetupCommon {

    private final Gson gson = new Gson();

    @Test
    public void testSendMessage() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/message/send";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("recipient", TEST_PERSON_NIN);
        data.put("subject", "Test-dela-ut");
        data.put("message", "Dummy text");
        data.put("language", "svSE");
        data.put("content_type", "text/plain");

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        Message.SendResponse jsonResponse = gson.fromJson(response.readEntity(String.class), Message.SendResponse.class);

        assertTrue(jsonResponse.getDelivered());
        assertEquals(jsonResponse.getRecipient(), TEST_PERSON_NIN);
        assertNotNull(jsonResponse.getTransactionId());
    }

    @Test
    public void testFailSendMessage() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/message/send";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);

        HashMap<String, String> data = new HashMap<>();
        data.put("recipient", TEST_PERSON_FAILING_NIN);
        data.put("subject", "Test-dela-ut");
        data.put("message", "Dummy text");
        data.put("language", "svSE");
        data.put("content_type", "text/plain");

        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);

        Message.SendResponse jsonResponse = gson.fromJson(response.readEntity(String.class), Message.SendResponse.class);

        assertFalse(jsonResponse.getDelivered());
        assertEquals(jsonResponse.getRecipient(), TEST_PERSON_FAILING_NIN);
        assertNotNull(jsonResponse.getTransactionId());
    }

}
