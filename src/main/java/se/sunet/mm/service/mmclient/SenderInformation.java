package se.sunet.mm.service.mmclient;

/**
 * Created by lundberg on 2014-07-03.
 */
public class SenderInformation  {

    private String senderOrganisationNumber;
    private String senderName;
    private String senderSupportText;
    private String senderSupportEmailAddress;
    private String senderSupportPhoneNumber;
    private String senderSupportURL;
    private String senderPKCS8KeyPath;
    private String senderPEMCertPath;

    public SenderInformation(String senderOrganisationNumber, String senderName, String senderSupportText,
                             String senderSupportEmailAddress, String senderSupportPhoneNumber,
                             String senderSupportURL, String senderPKCS8KeyPath, String senderPEMCertPath) {
        this.senderOrganisationNumber = senderOrganisationNumber;
        this.senderName = senderName;
        this.senderSupportText = senderSupportText;
        this.senderSupportEmailAddress = senderSupportEmailAddress;
        this.senderSupportPhoneNumber = senderSupportPhoneNumber;
        this.senderSupportURL = senderSupportURL;
        this.senderPKCS8KeyPath = senderPKCS8KeyPath;
        this.senderPEMCertPath = senderPEMCertPath;
    }

    public String getSenderOrganisationNumber() {
        return senderOrganisationNumber;
    }

    public void setSenderOrganisationNumber(String senderOrganisationNumber) {
        this.senderOrganisationNumber = senderOrganisationNumber;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderSupportText() {
        return senderSupportText;
    }

    public void setSenderSupportText(String senderSupportText) {
        this.senderSupportText = senderSupportText;
    }

    public String getSenderSupportEmailAddress() {
        return senderSupportEmailAddress;
    }

    public void setSenderSupportEmailAddress(String senderSupportEmailAddress) {
        this.senderSupportEmailAddress = senderSupportEmailAddress;
    }

    public String getSenderSupportPhoneNumber() {
        return senderSupportPhoneNumber;
    }

    public void setSenderSupportPhoneNumber(String senderSupportPhoneNumber) {
        this.senderSupportPhoneNumber = senderSupportPhoneNumber;
    }

    public String getSenderSupportURL() {
        return senderSupportURL;
    }

    public void setSenderSupportURL(String senderSupportURL) {
        this.senderSupportURL = senderSupportURL;
    }

    public String getSenderPKCS8KeyPath() {
        return senderPKCS8KeyPath;
    }

    public void setSenderPKCS8KeyPath(String senderPKCS8KeyPath) {
        this.senderPKCS8KeyPath = senderPKCS8KeyPath;
    }

    public String getSenderPEMCertPath() {
        return senderPEMCertPath;
    }

    public void setSenderPEMCertPath(String senderPEMCertPath) {
        this.senderPEMCertPath = senderPEMCertPath;
    }
}
