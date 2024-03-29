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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created by lundberg on 2014-05-22.
 */
public class UserReachableTest extends SetupCommon {

    private final Gson gson = new Gson();

    @Test
    public void testUserReachable() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/user/reachable";
        // Load Mina Meddelanden test response data
        //UserReachable.Response expectedResponse = new UserReachable.Response(Boolean.TRUE,
        //        "Secure", "https://twww.smartrefill.se/PostServer/ws/government/Service");
        // ServiceAddress changed for the test data unexpectedly
        UserReachable.Response expectedResponse = new UserReachable.Response(Boolean.TRUE, TEST_PERSON_NIN,
                        "Secure", "https://notarealhost.skatteverket.se/webservice/acc1accao/Service/v3");
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);
        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_NIN);
        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);
        UserReachable.Response jsonResponse = gson.fromJson(response.readEntity(String.class), UserReachable.Response.class);
        assertEquals(jsonResponse.getSenderAccepted(), expectedResponse.getSenderAccepted());
        assertEquals(jsonResponse.getAccountStatus().getRecipient(), expectedResponse.getAccountStatus().getRecipient());
        assertEquals(jsonResponse.getAccountStatus().getType(), expectedResponse.getAccountStatus().getType());
        assertEquals(jsonResponse.getAccountStatus().getServiceSupplier().getServiceAddress(),
                expectedResponse.getAccountStatus().getServiceSupplier().getServiceAddress());
    }

    @Test
    public void testUserNotReachable() throws Exception {
        Server server = embeddedServer.getServer();
        String servletPath = "/user/reachable";

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(server.getURI()).path(servletPath);
        HashMap<String, String> data = new HashMap<>();
        data.put("identity_number", TEST_PERSON_FAILING_NIN);
        Entity entity = Entity.entity(gson.toJson(data), MediaType.APPLICATION_JSON);
        Response response = target.request(MediaType.APPLICATION_JSON).post(entity);
        UserReachable.Response jsonResponse = gson.fromJson(response.readEntity(String.class), UserReachable.Response.class);
        assertNotNull(jsonResponse.getSenderAccepted());
        assertNotNull(jsonResponse.getAccountStatus().getRecipient());
        assertNotNull(jsonResponse.getAccountStatus().getType());
        assertNotNull(jsonResponse.getAccountStatus().getServiceSupplier().getServiceAddress());
    }


}
