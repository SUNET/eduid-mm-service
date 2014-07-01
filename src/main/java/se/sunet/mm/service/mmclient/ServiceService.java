package se.sunet.mm.service.mmclient;

import se.gov.minameddelanden.common.CertificateUtils;
import se.gov.minameddelanden.common.SignedXml;
import se.gov.minameddelanden.common.X509CertificateWithPrivateKey;
import se.gov.minameddelanden.common.Xml;
import se.gov.minameddelanden.schema.message.*;
import se.gov.minameddelanden.schema.sender.Sender;
import se.gov.minameddelanden.schema.service.DeliveryResult;
import se.gov.minameddelanden.service.Service;
import se.gov.minameddelanden.service.ServicePort;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class ServiceService extends ClientBase {
    private String senderName;
    private String senderOrganisationNumber;
    private String senderSupportText;
    private String senderSupportEmailAddress;
    private String senderSupportPhoneNumber;
    private String senderSupportURL;
    private String senderPKCS8KeyPath;
    private String senderPEMCertPath;

    private static String serviceEndpoint = null;

    public ServiceService(String senderOrganisationNumber, String senderName, String senderSupportText,
                          String senderSupportEmailAddress, String senderSupportPhoneNumber, String senderSupportURL,
                          String senderPKCS8KeyPath, String senderPEMCertPath) {
        this.senderOrganisationNumber = senderOrganisationNumber;
        this.senderName = senderName;
        this.senderSupportText = senderSupportText;
        this.senderSupportEmailAddress = senderSupportEmailAddress;
        this.senderSupportPhoneNumber = senderSupportPhoneNumber;
        this.senderSupportURL = senderSupportURL;
        this.senderPKCS8KeyPath = senderPKCS8KeyPath;
        this.senderPEMCertPath = senderPEMCertPath;
    }

    public DeliveryResult sendSecureMessage(String recipient,
                                                String subject,
                                                String message,
                                                String language,
                                                String contentType) throws Exception {
        SecureDelivery secureDelivery = new SecureDelivery();
        DeliveryHeader header = createDeliveryHeader(recipient);
        secureDelivery.setHeader(header);

        SecureMessage secureMessage = new SecureMessage();
        SupportInfo supportInfo = createSupportInfo();
        MessageHeader messageHeader = createMessageHeader(subject, language, supportInfo);
        secureMessage.setHeader(messageHeader);

        MessageBody body = new MessageBody();
        body.setBody(message.getBytes("UTF-8"));
        body.setContentType(contentType);
        secureMessage.setBody(body);
        secureDelivery.getMessage().add(secureMessage);

        SignedDelivery signedDelivery = createSignedDelivery(secureDelivery);
        SealedDelivery sealedDelivery = sealSignedDelivery(signedDelivery);

        RecipientService recipientService = new RecipientService(senderOrganisationNumber);
        String url = recipientService.getServiceAddress(recipient);
        ServicePort servicePort = getPort(ServicePort.class, Service.class, url);

        return servicePort.deliverSecure(sealedDelivery);
    }

    private DeliveryHeader createDeliveryHeader(String recipient) {
        DeliveryHeader header  = new DeliveryHeader();
        Sender sender = new Sender();
        sender.setId(senderOrganisationNumber);
        sender.setName(senderName);
        header.setSender(sender);
        header.getRecipient().add(recipient);

        return header;
    }

    private SupportInfo createSupportInfo() {
        SupportInfo supportInfo = new SupportInfo();
        supportInfo.setText(senderSupportText);
        supportInfo.setEmailAdress(senderSupportEmailAddress);
        supportInfo.setPhoneNumber(senderSupportPhoneNumber);
        supportInfo.setURL(senderSupportURL);

        return supportInfo;
    }

    private MessageHeader createMessageHeader(String subject, String language, SupportInfo supportInfo) {
        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setId(UUID.randomUUID().toString());
        messageHeader.setLanguage(language);
        messageHeader.setSubject(subject);
        messageHeader.setSupportinfo(supportInfo);

        return messageHeader;
    }

    private SignedDelivery createSignedDelivery(SecureDelivery secureDelivery) throws URISyntaxException, CertificateException {
        SignedDelivery signedDelivery = new SignedDelivery();
        signedDelivery.setDelivery(secureDelivery);

        Xml unsignedXml = Xml.fromJaxbObject(signedDelivery);
        SignedXml signedXml = unsignedXml.sign(getKeyPair());

        return signedXml.toJaxbObject(SignedDelivery.class);
    }

    private SealedDelivery sealSignedDelivery(SignedDelivery signedDelivery) throws DatatypeConfigurationException, CertificateException, URISyntaxException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

        SealedDelivery sealedDelivery = new SealedDelivery();
        Seal seal = new Seal();
        seal.setReceivedTime(date);
        seal.setSignaturesOK(true);
        sealedDelivery.setSeal(seal);
        sealedDelivery.setSignedDelivery(signedDelivery);

        Xml unsignedXml = Xml.fromJaxbObject(sealedDelivery);
        SignedXml signedXml = unsignedXml.sign(getKeyPair());

        return signedXml.toJaxbObject(SealedDelivery.class);
    }

    private X509CertificateWithPrivateKey getKeyPair() throws URISyntaxException, CertificateException {
        URI certUri = new URI(senderPEMCertPath);
        X509Certificate cert = CertificateUtils.readCertificateFromUrl(certUri);
        URI keyUri = new URI(senderPKCS8KeyPath);
        PrivateKey key = CertificateUtils.readKey(keyUri);

        return new X509CertificateWithPrivateKey(cert, key);
    }
}
