package se.sunet.mm.service.mmclient;

import org.testng.annotations.Test;
import se.gov.minameddelanden.schema.recipient.AccountTypes;
import se.gov.minameddelanden.schema.recipient.ReachabilityStatus;

import static org.testng.Assert.assertEquals;

public class RecipientServiceTest extends SetupCommon {

    @Test
    public void testIsReachable() throws Exception {
        RecipientService service = new RecipientService(WS_BASE_ENDPOINT, SENDER_ORG_NR);
        ReachabilityStatus status = service.isReachable(TEST_PERSON_NIN);

        assertEquals(status.getAccountStatus().getType(), AccountTypes.SECURE);
        assertEquals(status.getAccountStatus().getRecipientId(), TEST_PERSON_NIN);
    }

}
