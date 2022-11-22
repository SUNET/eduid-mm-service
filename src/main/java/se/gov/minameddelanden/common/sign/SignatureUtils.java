package se.gov.minameddelanden.common.sign;

import static java.util.Collections.singletonList;
import static se.gov.minameddelanden.common.Base64.fromBase64String;
import static se.gov.minameddelanden.common.MiscUtils.lastOf;
import static se.gov.minameddelanden.common.XmlUtil.asElementsList;
import static se.gov.minameddelanden.common.XmlUtil.getElementByTagNameNS;
import static se.gov.minameddelanden.common.XmlUtil.removeXmlNsPrefixes;
import static se.gov.minameddelanden.common.XmlUtil.renameElementTo;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.XMLValidateContext;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import se.gov.minameddelanden.common.CertificateChainTrustVerifier;
import se.gov.minameddelanden.common.X509CertificateWithPrivateKey;
import se.gov.minameddelanden.common.Xml;
import se.gov.minameddelanden.common.XmlUtil;
import se.gov.minameddelanden.common.exception.ApplicationErrorCode;
import se.gov.minameddelanden.common.exception.ApplicationException;
import se.gov.minameddelanden.common.exception.SystemErrorCode;
import se.gov.minameddelanden.common.exception.SystemException;

@SuppressWarnings("restriction")
public class SignatureUtils {
    private static final Logger LOGGER = Logger.getLogger(SignatureUtils.class.getName());

    private SignatureUtils() {
    }

	public static boolean isPKCS7(byte[] rawSignature) {
        return false;
    }

    public static byte[] base64EncodedSignatureToBytes(String input) {
        if (input.contains("%")) {
            LOGGER.fine("Signaturen var URLEncoded");
            try {
                input = URLDecoder.decode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return fromBase64String(input).toBytes();
    }

    public static Map<String, String> extractSubjectFields(String subject) {

        // Example subject:
        // SERIALNUMBER=199008252398, GIVENNAME=Oskar, SURNAME=Johansson, CN=Oskar Johansson, C=SE

        // Vissa certifikat (bara Steria?) innehåller + istf. , som separator. Vet inte om det skall vara
        // sa eller om cerifikaten skapats pa ett felaktigt sätt men vi kodar oss runt det
        // här:
        subject = subject.replaceAll("\\+", ",");

        String[] flds = subject.split(",");
        Map<String, String> fldmap = new HashMap<String, String>();

        for (String fld : flds) {
            String[] val = fld.split("=");
            fldmap.put(val[0].trim(), val[1].trim());
        }

        return fldmap;
    }

    public static XMLSignature signXml(Document doc, X509CertificateWithPrivateKey certificateWithPrivateKey) {

        removeXmlNsPrefixes(doc);

        Element element = doc.getDocumentElement();
        String ref = element.getAttribute("Id");

        if(ref == null || ref.length() == 0) {
            ref = "";
        }
        else {
            ref = "#" + ref;
            setId(doc);
        }

        return signXml(doc, ref, certificateWithPrivateKey);
    }

    public static XMLSignature signXml(Document doc, String uriRef, X509CertificateWithPrivateKey certificateWithPrivateKey) {

        try {
            // Create a DOM XMLSignatureFactory that will be used to
            // generate the enveloped signature.
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Create a Reference to the enveloped document (in this case,
            // you are signing the whole document, so a URI of "" signifies
            // that, and also specify the SHA1 digest algorithm and
            // the ENVELOPED Transform.
            DigestMethod digestMethod = fac.newDigestMethod(DigestMethod.SHA1, null);
            Transform transform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            Reference ref = fac.newReference(uriRef, digestMethod, singletonList(transform), null, null);

            // Create the SignedInfo.
            CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
            SignatureMethod signatureMethod = fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
            SignedInfo si = fac.newSignedInfo(canonicalizationMethod, signatureMethod, singletonList(ref));

            // Create the KeyInfo containing the X509Data.
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List<Serializable> x509Content = new ArrayList<Serializable>();
            x509Content.add(certificateWithPrivateKey.getCertificate().getSubjectX500Principal().getName());
            x509Content.add(certificateWithPrivateKey.getCertificate());
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(singletonList(xd));

            // Create a DOMSignContext and specify the RSA PrivateKey and
            // location of the resulting XMLSignature's parent element.
            XMLSignContext dsc = new DOMSignContext(certificateWithPrivateKey.getPrivateKey(), doc.getDocumentElement());

            // Create the XMLSignature, but don't sign it yet.
            XMLSignature signature = fac.newXMLSignature(si, ki);

            // Marshal, generate, and sign the enveloped signature.
            signature.sign(dsc);
            return signature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static X509Certificate getSigningCertificate(byte[] rawSignature) throws Exception {
        if (isPKCS7(rawSignature)) {
            throw new RuntimeException("PKCS7 not supported");
        } else {
            return getXmlSigningCert(rawSignature);
        }
    }

    public static X509Certificate getSigningCertificateFromXMLSignature(XMLSignature xmlSignature) {
        X509Certificate[] crts = getCerts(xmlSignature.getKeyInfo());
        return getSigningCert(crts);
    }

	static X509Certificate parsePkcs7(byte[] rawSignature) throws RuntimeException {
        throw new RuntimeException("PKCS7 not supported");
    }

    static X509Certificate getXmlSigningCert(byte[] rawSignature) throws Exception {
        XMLSignature xmlSignature = getXmlSignature(rawSignature);
        X509Certificate[] crts = getCerts(xmlSignature.getKeyInfo());
        return getSigningCert(crts);
    }

    static XMLSignature getXmlSignature(final byte[] bytes) throws Exception {
        Document document = Xml.fromBytes(bytes).toDOM();
        Node signatureElement = getSignatureElementOrRenamedSignatureType(document);

        XMLValidateContext ctx = new DOMValidateContext(new X509KeySelector(), signatureElement);
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        return fac.unmarshalXMLSignature(ctx);
    }

    private static Element getSignatureElementOrRenamedSignatureType(Document doc) throws SystemException {
        try {
            return lastOf(asElementsList(doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature")));
        } catch (NoSuchElementException e) {
            try {
                Element signatureTypeElement = lastOf(asElementsList(doc.getElementsByTagNameNS(XMLSignature.XMLNS, "SignatureType")));
                return renameElementTo(signatureTypeElement, "Signature");
            } catch (NoSuchElementException e1) {
                throw new SystemException(SystemErrorCode.INVALID_INPUT,
                        "Cannot find Signature element or SignatureType element in document:\n" + Xml.fromDOM(doc), e1);
            }
        }
    }

    static X509Certificate getSigningCert(X509Certificate[] certs) {
        for (X509Certificate cert : certs) {
            boolean[] certKeyUsage = cert.getKeyUsage();
            if (certKeyUsage != null && certKeyUsage.length >= 2) {
                if (certKeyUsage[1] || // Non-repudiation
                        certKeyUsage[0]) { // DigitalSignature
                    return cert;
                }
            }
        }
        return certs[0];
    }

    static X509Certificate[] getCerts(KeyInfo keyInfo) {
        List<X509Certificate> certs = new ArrayList<X509Certificate>();

        for (Object content : keyInfo.getContent()) {
            if (!(content instanceof X509Data)) {
                continue;
            }

            X509Data x509Data = (X509Data) content;

            for (Object o : x509Data.getContent()) {
                if (o instanceof X509Certificate) {
                    certs.add((X509Certificate) o);
                }
            }
        }

        return certs.toArray(new X509Certificate[certs.size()]);
    }

    private static void setId(Document document) {

        Element element = document.getDocumentElement();
        String ref = element.getAttribute("Id");

        if(ref != null && ref.length() > 0) {

            Attr attr = element.getAttributeNode("Id");
            element.setIdAttributeNode(attr, true);
        }
    }

    public static XMLSignature validateSignature(Document document, CertificateChainTrustVerifier certificateChainTrustVerifier) throws SystemException, ApplicationException {

        setId(document);
        workaroundXmlIdIssueWithMobiltBankID(document);
        removeXmlNsPrefixes(document);

        try {
            Element signatureElement = getSignatureElementOrRenamedSignatureType(document);
            XMLValidateContext ctx = new DOMValidateContext(new X509KeySelector(), signatureElement);
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = fac.unmarshalXMLSignature(ctx);

            if (LOGGER.isLoggable(Level.FINER)) {
                Element signer;
                try {
                    signer = getElementByTagNameNS(signatureElement, XMLSignature.XMLNS, "X509SubjectName");
                } catch (NoSuchElementException e) {
                    signer = getElementByTagNameNS(signatureElement, XMLSignature.XMLNS, "KeyInfo");
                }
                LOGGER.finer("Validating signature signed by: " + XmlUtil.nodesToString(signer.getChildNodes()));
            }

            boolean validSignature = signature.validate(ctx);
            if (!validSignature) {
                throw new ApplicationException(ApplicationErrorCode.ACCESS_DENIED,
                        "Ogiltig signatur i dokument:\n" + Xml.fromDOM(document));
            }

            if(certificateChainTrustVerifier != null) {
                verifyXmlSignatureCertChain(signature, certificateChainTrustVerifier);
            }

            return signature;
        } catch (SystemException e) {
            throw e;
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            String message = e.getMessage();
            checkJavaVersion6u18OrLaterSupportingRsaSha256And512(message);
            throw new RuntimeException(message + "\n" + removeBase64Newlines(Xml.fromDOM(document).toIndentedString()), e);
        }
    }

    private static void workaroundXmlIdIssueWithMobiltBankID(Document document) {

        try {

            //
            // Fix för att gå runt felaktigt Id-attribut i XML:en från BankID-klienten
            // Fungerade i tidigare JRE:er, slutade fungera from Sun JRE 1.6.0_51 (=HPUX JRE 1.6.0_20)
            //
            // Se: http://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
            // beskriver felet för senare java 1.7 men samma problem verkar dyka upp i senare 1.6 versioner också

            // Talar om att Id-attributet verkligen är ett Id-attribut och inget annat!
            Element bankIdSignedData = lastOf(asElementsList(document.getElementsByTagNameNS("http://www.bankid.com/signature/v1.0.0/types", "bankIdSignedData")));
            Attr idAttr = bankIdSignedData.getAttributeNode("Id");
            bankIdSignedData.setIdAttributeNode(idAttr, true);
        }
        catch(NoSuchElementException e) {
            // Inte BankID-XML: No worries! it could be any other xmlSig
        }
    }

    private static String removeBase64Newlines(String s) {
        return s.replaceAll("(?m)([a-zA-Z0-9/+]+)$\r?\n", "$1 ");
    }

    static void verifyXmlSignatureCertChain(XMLSignature xmlSignature, CertificateChainTrustVerifier certificateChainTrustVerifier) throws Exception {
        X509Certificate[] crts = getCerts(xmlSignature.getKeyInfo());
        X509Certificate crt = crts[0];

        LOGGER.fine("XML-Signatur OK -> Certifikat [" + crt.getSubjectDN().getName() + "] utgivet av [" + crt.getIssuerDN().getName() + "]");

        certificateChainTrustVerifier.verifyCertificateChain(crts);
    }

    /**
     * Parsar PKSC7 och XMLSignaturer. Hanterar base64 och URLEncodade signaturer.
     * Om signaturen anges som enligt Subject-format skapas en mockad signatur som kan användas
     * i enhetsprov med mockad signaturkontroll.
     * <p/>
     * Exempel som ger en mockad signatur
     * SERIALNUMBER=199008252398, GIVENNAME=Oskar, SURNAME=Johansson, CN=Oskar Johansson, C=SE
     *
     * @param rawSignature
     * @throws Exception
     */
    public static ParsedSignature parseSignature(byte[] rawSignature) throws Exception {
        String possiblyMockedSignature = new String(rawSignature, "UTF-8");
        boolean signatureMocked = possiblyMockedSignature.startsWith("SERIALNUMBER");
        if (signatureMocked) {
            Map<String, String> subjectFields = extractSubjectFields(possiblyMockedSignature);
            return new ParsedSignature(null, subjectFields.get("CN"), subjectFields.get("SERIALNUMBER"));
        } else {
            Map<String, String> subjectFields = extractSubjectFields(getSigningCertificate(rawSignature).getSubjectDN().getName());
            return new ParsedSignature(getSigningCertificate(rawSignature), subjectFields.get("CN"), subjectFields.get("SERIALNUMBER"));
        }
    }

    static String findRsaSha256Or512AlgorithmUri(String message) {
        Matcher matcher = Pattern.compile(Pattern.quote("http://www.w3.org/2001/04/xmldsig-more#rsa-sha") + "(256|512)").matcher(message);
        String algoritm = null;
        if (matcher.find()) {
            algoritm = matcher.group();
        }
        return algoritm;
    }

    public static void checkJavaVersion6u18OrLaterSupportingRsaSha256And512(String string) {
        String algURI = findRsaSha256Or512AlgorithmUri(string);
        if (null == algURI) {
            return;
        }
        String javaVersion = System.getProperty("java.version");
        Matcher matcher = Pattern.compile("1\\.6\\.0_(\\d+)").matcher(javaVersion);
        if (matcher.matches()) {
            int updateVersion = Integer.parseInt(matcher.group(1));
            if (updateVersion < 18) {
                String javaVendor = System.getProperty("java.vendor");
                throw new UnsupportedOperationException("Algorithm \"" + algURI + "\" is not supported. Support for rsa-sha256 and rsa-sha512 was added in Java 1.6.0_18, but we are running " + javaVendor + " Java " + javaVersion);
            }
        }
    }
}
