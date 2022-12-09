package se.sunet.mm.service.mmclient;

import se.gov.minameddelanden.common.CertificateUtils;
import se.gov.minameddelanden.common.SignedXml;
import se.gov.minameddelanden.common.X509CertificateWithPrivateKey;
import se.gov.minameddelanden.common.Xml;
import se.gov.minameddelanden.schema.message.MessageBody;
import se.gov.minameddelanden.schema.message.Seal;
import se.gov.minameddelanden.schema.message.v2.SecureDeliveryHeader;
import se.gov.minameddelanden.schema.message.v3.*;
import se.gov.minameddelanden.schema.sender.Sender;
import se.gov.minameddelanden.schema.service.DeliveryResult;
import se.gov.minameddelanden.service.Service;
import se.gov.minameddelanden.service.ServicePortV3;

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

    private static String serviceEndpoint = null;
    private SenderInformation senderInformation;

    public ServiceService(String wsBaseEndpoint, SenderInformation senderInformation) {
        super(wsBaseEndpoint);
        this.senderInformation = senderInformation;
    }

    public DeliveryResult sendSecureMessage(String recipient,
                                                String subject,
                                                String message,
                                                String language,
                                                String contentType) throws Exception {
        SecureDelivery secureDelivery = new SecureDelivery();
        SecureDeliveryHeader header = createDeliveryHeader(recipient);
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

        RecipientService recipientService = new RecipientService(getWsBaseEndpoint(), senderInformation.getSenderOrganisationNumber());
        String url = recipientService.getServiceAddress(recipient);
        ServicePortV3 servicePort = getPort(ServicePortV3.class, Service.class, url);

        return servicePort.deliverSecure(sealedDelivery);
    }

    private SecureDeliveryHeader createDeliveryHeader(String recipient) {
        SecureDeliveryHeader header  = new SecureDeliveryHeader();
        Sender sender = new Sender();
        sender.setId(senderInformation.getSenderOrganisationNumber());
        sender.setName(senderInformation.getSenderName());
        header.setSender(sender);
        header.setRecipient(recipient);

        return header;
    }

    private SupportInfo createSupportInfo() {
        SupportInfo supportInfo = new SupportInfo();
        supportInfo.setText(senderInformation.getSenderSupportText());
        supportInfo.setEmailAdress(senderInformation.getSenderSupportEmailAddress());
        supportInfo.setPhoneNumber(senderInformation.getSenderSupportPhoneNumber());
        supportInfo.setURL(senderInformation.getSenderSupportURL());

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
        URI certUri = new URI(senderInformation.getSenderPEMCertPath());
        X509Certificate cert = CertificateUtils.readCertificateFromUrl(certUri);
        URI keyUri = new URI(senderInformation.getSenderPKCS8KeyPath());
        PrivateKey key = CertificateUtils.readKey(keyUri);

        return new X509CertificateWithPrivateKey(cert, key);
    }
}
