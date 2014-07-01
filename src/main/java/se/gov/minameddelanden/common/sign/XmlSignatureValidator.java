package se.gov.minameddelanden.common.sign;

import org.w3c.dom.Document;
import se.gov.minameddelanden.common.exception.ApplicationException;
import se.gov.minameddelanden.common.exception.SystemException;

import javax.xml.crypto.dsig.XMLSignature;

public interface XmlSignatureValidator {
    XMLSignature validateSignature(Document doc) throws SystemException, ApplicationException;
}
