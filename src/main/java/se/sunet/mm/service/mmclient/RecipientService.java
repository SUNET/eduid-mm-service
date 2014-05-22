package se.sunet.mm.service.mmclient;

import se.gov.minameddelanden.recipient.Recipient;
import se.gov.minameddelanden.recipient.RecipientPort;
import se.gov.minameddelanden.schema.recipient.ReachabilityStatus;

import java.util.ArrayList;
import java.util.List;

public class RecipientService extends ClientBase {
    private String organizationNumber;
    private static String serviceEndpoint = null;

    public RecipientService() {}

    public RecipientService(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    public RecipientService(String organizationNumber, String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
        this.organizationNumber = organizationNumber;
    }

    public ReachabilityStatus isReachable(String nationalIdentityNumber) throws Exception {
        RecipientPort port = getPort(RecipientPort.class, Recipient.class, serviceEndpoint);
        List<String> recipients = new ArrayList<>();
        recipients.add(nationalIdentityNumber);

        List<ReachabilityStatus> status = port.isReachable(organizationNumber, recipients);

        return status.get(0);
    }

    public void setOrganizationNumber(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }
}
