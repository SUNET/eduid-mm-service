package se.gov.minameddelanden.common;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;

import se.gov.minameddelanden.common.exception.ApplicationErrorCode;
import se.gov.minameddelanden.common.exception.ApplicationException;
import se.gov.minameddelanden.common.exception.SystemErrorCode;
import se.gov.minameddelanden.common.exception.SystemException;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class CaMapCertificateChainTrustVerifier implements CertificateChainTrustVerifier {
	private static final Logger LOGGER = Logger.getLogger(CaMapCertificateChainTrustVerifier.class.getName());
	private Supplier<Map<X500Principal, X509Certificate>> caCerts;

	private CaMapCertificateChainTrustVerifier(Supplier<Map<X500Principal, X509Certificate>> caCerts) {
		this.caCerts = caCerts;
	}

	@Override
	public void verifyCertificateChain(X509Certificate[] certChain) throws Exception {
		verifyCertificateChain(certChain, new Date());
	}

	@Override
	public void verifyCertificateChain(X509Certificate[] certChain, Date date) throws Exception {

		LOGGER.fine("Kontrollerar certifikatkedja");

		for (int i = 0; i < certChain.length - 1; ++i) {
			X509Certificate cert = certChain[i];
			verifyCertValidityAndCrl(cert, date);
			X509Certificate issuer = certChain[i + 1];
			verifyCertAgainstIssuer(cert, issuer);
		}

		// Kolla alla utgivare
		X509Certificate cert = certChain[certChain.length - 1];
		verifyCertValidityAndCrl(cert, date);
		X500Principal issuerX500Principal = cert.getIssuerX500Principal();
		X509Certificate caCert = this.caCerts.get().get(issuerX500Principal);
		if (null == caCert) {
			throw new ApplicationException(ApplicationErrorCode.ACCESS_DENIED, "Fann inte ca cert [" + issuerX500Principal + "] för cert [" + cert.getSubjectX500Principal() + "].");
		}
		try {
			verifyCertAgainstIssuer(cert, caCert);
		}
		catch (SignatureException e) {
			throw new ApplicationException(ApplicationErrorCode.ACCESS_DENIED, "Utgivaren är inte betrodd", e);
		}

	}

	public static void verifyCertValidityAndCrl(X509Certificate cert, Date date) throws CertificateExpiredException, CertificateNotYetValidException {
		cert.checkValidity(date);
		checkCrl(cert);
	}

	public static void verifyCertAgainstIssuer(X509Certificate crt, X509Certificate caCert) throws Exception {
		LOGGER.fine("Kontrollerar certifikat: " + crt.getSubjectDN().getName() + " mot " + caCert.getSubjectDN().getName());

		try {
			// TODO
			// verifyCertValidityAndCrl(caCert);
			checkCACert(caCert);
			LOGGER.log(Level.FINE, "Verifying cert " + crt.getSubjectX500Principal() + " against issuer " + caCert.getSubjectX500Principal());
			crt.verify(caCert.getPublicKey());
			LOGGER.fine("Certifikat OK");
		}
		catch (SignatureException e) {
			throw e;
		}
		catch (Exception e) {
			LOGGER.log(Level.FINE, "Certifikat EJ OK: " + e.toString(), e);
			throw e;
		}
	}

	// TODO: check revokering
	private static void checkCrl(X509Certificate... x509Certificates) {
		LOGGER.fine("Revokeringskontroll ej implementerad");
	}

	private static void checkCACert(X509Certificate cert) throws Exception {

		if (cert.getBasicConstraints() < 0) {
			throw new SystemException(SystemErrorCode.CERTIFICATE_ERROR, "Utgivarcertifikat har inte basicConstraint=CA");
		}
	}

	public static CaMapCertificateChainTrustVerifier forUrls(String... urls) {
		return forUrls(asList(urls));
	}

	public static CaMapCertificateChainTrustVerifier forUrls(final Collection<String> urls) {
		return new CaMapCertificateChainTrustVerifier(new Supplier<Map<X500Principal, X509Certificate>>() {
			@Override
			public Map<X500Principal, X509Certificate> get() {
				return CertificateUtils.getCertificatesFromUrls(urls);
			}
		});
	}

	public static CaMapCertificateChainTrustVerifier forCACertificate(X509Certificate certificate) {
		return new CaMapCertificateChainTrustVerifier(Suppliers.ofInstance(singletonMap(certificate.getSubjectX500Principal(), certificate)));
	}
}