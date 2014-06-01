package se.sunet.mm.service.mmclient;

import org.testng.annotations.Test;
import se.gov.minameddelanden.schema.service.DeliveryResult;

import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: ratler
 * Date: 27/05/14
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class MessageServiceTest extends SetupCommon {
    @Test
    public void testSendSecureMessage() throws Exception {
        MessageService messageService = new MessageService();

        DeliveryResult result = messageService.sendSecureMessage(TEST_PERSON_NIN, "Test-dela-ut", "Dummy text", "svSE", "text/plain");

        assertTrue(result.getStatus().get(0).isDelivered());

    }
}
