package se.sunet.mm.service.mmclient;

import se.gov.minameddelanden.common.CertificateUtils;
import se.gov.minameddelanden.common.SignedXml;
import se.gov.minameddelanden.common.X509CertificateWithPrivateKey;
import se.gov.minameddelanden.common.Xml;
import se.gov.minameddelanden.message.Message;
import se.gov.minameddelanden.message.MessagePort;
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

public class MessageService extends ClientBase {
    public static final String ORGNR = "162021003898";
    public static final String SENDER_PKCS8_KEY_PATH = "classpath:/Kommun_B.p8";
    public static final String SENDER_PEM_CERT_PATH = "classpath:/Kommun_B.crt";

    private static String serviceEndpoint = null;

    public MessageService() {

    }

    public DeliveryResult sendSecureMessage(String recipient,
                                                String subject,
                                                String message,
                                                String language,
                                                String contentType) throws Exception {
        MessagePort port = getPort(MessagePort.class, Message.class, serviceEndpoint);

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


//        RecipientService recipientService = new RecipientService(System
  //              .getProperty("se.sunet.mm.service.senderOrganisationNumber"));
        RecipientService recipientService = new RecipientService(ORGNR);
        String url = recipientService.getServiceAddress(recipient);
        ServicePort servicePort = getPort(ServicePort.class, Service.class, url);
        DeliveryResult result = servicePort.deliverSecure(sealedDelivery);

        return result;
    }

    private DeliveryHeader createDeliveryHeader(String recipient) {
        DeliveryHeader header  = new DeliveryHeader();
        Sender sender = new Sender();
        sender.setId(ORGNR);
        sender.setName("Kommun B");
        header.setSender(sender);
        header.getRecipient().add(recipient);

        return header;
    }

    private SupportInfo createSupportInfo() {
        SupportInfo supportInfo = new SupportInfo();
        supportInfo.setText("Dummy text");
        supportInfo.setEmailAdress("info@kommun_b.se");
        supportInfo.setPhoneNumber("08-121212121212");
        supportInfo.setURL("http://www.kommun_b.se/");

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
        URI certUri = new URI(SENDER_PEM_CERT_PATH);
        X509Certificate cert = CertificateUtils.readCertificateFromUrl(certUri);
        URI keyUri = new URI(SENDER_PKCS8_KEY_PATH);
        PrivateKey key = CertificateUtils.readKey(keyUri);

        return new X509CertificateWithPrivateKey(cert, key);
    }
}
