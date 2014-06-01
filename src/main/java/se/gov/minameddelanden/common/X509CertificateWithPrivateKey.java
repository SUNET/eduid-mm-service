package se.gov.minameddelanden.common;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class X509CertificateWithPrivateKey {
    private X509Certificate certificate;
    private PrivateKey privateKey;

    public X509CertificateWithPrivateKey(X509Certificate certificate, PrivateKey privateKey) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
