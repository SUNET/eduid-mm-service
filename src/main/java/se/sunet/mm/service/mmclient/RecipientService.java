package se.sunet.mm.service.mmclient;

import se.gov.minameddelanden.recipient.Recipient;
import se.gov.minameddelanden.recipient.RecipientPort;
import se.gov.minameddelanden.schema.recipient.AccountTypes;
import se.gov.minameddelanden.schema.recipient.ReachabilityStatus;
import se.gov.minameddelanden.schema.recipient.ServiceSupplier;
import se.sunet.mm.service.api.UserReachable;

import java.util.ArrayList;
import java.util.List;

/** This class is used to verify if recipient exists in FAR (Mina meddelanden) */
public class RecipientService extends ClientBase {
    private String organizationNumber;
    private static String serviceEndpoint = null;

    /***
     * Constructor
     *
     * @param organizationNumber a Swedish organization number
     */
    public RecipientService(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    /***
     * Constructor
     *
     * @param organizationNumber a Swedish organization number
     * @param serviceEndpoint the webservice URL
     */
    public RecipientService(String organizationNumber, String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
        this.organizationNumber = organizationNumber;
    }

    /***
     * Check if the recipient (NIN) is registered.
     *
     * @param nationalIdentityNumber the recipients Swedish national identity number
     * @return {@link se.gov.minameddelanden.schema.recipient.ReachabilityStatus}
     * @throws Exception
     */
    public ReachabilityStatus isReachable(String nationalIdentityNumber) throws Exception {
        RecipientPort port = getPort(RecipientPort.class, Recipient.class, serviceEndpoint);
        List<String> recipients = new ArrayList<>();
        recipients.add(nationalIdentityNumber);

        List<ReachabilityStatus> status = port.isReachable(organizationNumber, recipients);

        return status.get(0);
    }

    public String getServiceAddress(String nationalIdentityNumber) throws Exception {
        ReachabilityStatus status = isReachable(nationalIdentityNumber);
        if (status.getAccountStatus().getType().equals(AccountTypes.SECURE)) {
            return status.getAccountStatus().getServiceSupplier().getServiceAdress();
        } else {
            return null;
        }

    }
    /***
     * Setter for organization number
     *
     * @param organizationNumber
     */
    public void setOrganizationNumber(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }
}
