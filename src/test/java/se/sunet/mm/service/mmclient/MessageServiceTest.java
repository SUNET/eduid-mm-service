package se.sunet.mm.service.mmclient;

import org.testng.annotations.Test;
import se.gov.minameddelanden.schema.service.DeliveryResult;
import se.gov.minameddelanden.schema.service.DeliveryStatus;


import static org.testng.Assert.assertNotNull;
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
        SenderInformation senderInformation = new SenderInformation(SENDER_ORG_NR, SENDER_NAME, SENDER_TEXT,
                SENDER_MAIL, SENDER_PHONE, SENDER_URL, SENDER_PKCS8_KEY_PATH, SENDER_PEM_CERT_PATH);
        ServiceService messageService = new ServiceService(WS_BASE_ENDPOINT, senderInformation);

        DeliveryResult result = messageService.sendSecureMessage(TEST_PERSON_NIN, "Test-dela-ut", "Dummy text", "svSE", "text/plain");

        assertNotNull(result.getTransId());
        assertTrue(result.getStatus().get(0).isDelivered());

    }
}
