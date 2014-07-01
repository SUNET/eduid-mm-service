package se.gov.minameddelanden.common;

import javax.xml.crypto.dsig.XMLSignature;

public class SignedXml extends Xml {
    private XMLSignature signature;

    SignedXml(byte[] bytes, XMLSignature signature) {
        super(bytes);
        this.signature = signature;
    }

    public XMLSignature getSignature() {
        return signature;
    }
}
