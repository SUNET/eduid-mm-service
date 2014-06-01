package se.gov.minameddelanden.common;

import java.security.cert.X509Certificate;
import java.util.Date;

public interface CertificateChainTrustVerifier {
	void verifyCertificateChain(X509Certificate[] certChain) throws Exception;
    void verifyCertificateChain(X509Certificate[] certChain, Date date) throws Exception;
}
