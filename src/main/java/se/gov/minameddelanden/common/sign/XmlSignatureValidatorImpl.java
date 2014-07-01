package se.gov.minameddelanden.common.sign;

import org.w3c.dom.Document;
import se.gov.minameddelanden.common.CertificateChainTrustVerifier;
import se.gov.minameddelanden.common.exception.ApplicationException;
import se.gov.minameddelanden.common.exception.SystemException;

import javax.xml.crypto.dsig.XMLSignature;

public class XmlSignatureValidatorImpl implements XmlSignatureValidator {

    private final CertificateChainTrustVerifier certificateChainTrustVerifier;

    public XmlSignatureValidatorImpl(CertificateChainTrustVerifier certificateChainTrustVerifier) {
        this.certificateChainTrustVerifier = certificateChainTrustVerifier;
    }

    @Override
    public XMLSignature validateSignature(Document doc) throws SystemException, ApplicationException {
        return SignatureUtils.validateSignature(doc, certificateChainTrustVerifier);
    }
}
