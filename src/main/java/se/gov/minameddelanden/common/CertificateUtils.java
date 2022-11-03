package se.gov.minameddelanden.common;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang.StringUtils.substringBetween;
import static se.gov.minameddelanden.common.CertificateUrlsLoader.certificatesFromUrls;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

public class CertificateUtils {
    private static final String RFC1779 = "RFC1779";
	private static final String SERIAL_NUMBER_OID = "2.5.4.5";
    private static final Logger LOGGER = Logger.getLogger(CertificateUtils.class.getName());
    private static final String WL_PROXY_CLIENT_CERT_HEADER = "WL-Proxy-Client-Cert";
    private final static Cache<Iterable<String>, Map<X500Principal, X509Certificate>> CA_CERTS_CACHE = expiringCertificatesCache(5, MINUTES);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static Cache<Iterable<String>, Map<X500Principal, X509Certificate>> expiringCertificatesCache(int duration, TimeUnit timeUnit) {
        return CacheBuilder.newBuilder().expireAfterWrite(duration, timeUnit).build(CacheLoader.from(certificatesFromUrls()));
    }

    public static String getSerialNumber(X509Certificate[] certificates) {
    	String serialNumber = null;
    	X509Certificate certificate = getFirstCertificateOrNull(certificates);
    	
    	if (certificate != null) { 
	        String subjectDN = certificate.getSubjectX500Principal().getName(RFC1779, Collections.singletonMap(SERIAL_NUMBER_OID, "serialNumber"));
	        LOGGER.fine("Got certificate with subject DN " + subjectDN);
	        Matcher matcher = Pattern.compile("\\bserialNumber=(\\d+)").matcher(subjectDN);

	        if (!matcher.find()) {
	            LOGGER.fine("Didn't find a serialNumber (Object ID " + SERIAL_NUMBER_OID + ") in the certificate subject DN.");
	            return null;
	        }
	        
	        serialNumber = matcher.group(1);
	        LOGGER.fine("Found serialNumber (Object ID " + SERIAL_NUMBER_OID + ") " + serialNumber + " in the certificate subject DN.");
    	}

    	return serialNumber;
    }

    public static X509CertificateWithPrivateKey generateCertificate(X500Principal dn, X509CertificateWithPrivateKey caCert) throws NoSuchAlgorithmException, CertificateEncodingException, SignatureException, InvalidKeyException, CertificateParsingException {
        X509V3CertificateGenerator certificateGenerator = new X509V3CertificateGenerator();
        KeyPair keyPair = generateRsaKeyPair();
        Calendar expiry = Calendar.getInstance();
        BigInteger serialNumberBigInteger = new BigInteger("1");
        Date start = expiry.getTime();
        expiry.add(Calendar.DATE, 1);
        Date end = expiry.getTime();
        X500Principal issuerDn = caCert.getCertificate().getSubjectX500Principal();
        initCertificate(certificateGenerator, keyPair, serialNumberBigInteger, start, end, dn, issuerDn);

        certificateGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert.getCertificate()));
        certificateGenerator.addExtension(X509Extensions.SubjectKeyIdentifier, false, SubjectKeyIdentifier.getInstance((keyPair.getPublic())));

        return new X509CertificateWithPrivateKey(certificateGenerator.generate(caCert.getPrivateKey()), keyPair.getPrivate());
    }

    private static void initCertificate(X509V3CertificateGenerator certificateGenerator, KeyPair keyPair, BigInteger serialNumberBigInteger, Date start, Date end, X500Principal dn, X500Principal issuerDn) {
        certificateGenerator.setSerialNumber(serialNumberBigInteger);
        certificateGenerator.setNotBefore(start);
        certificateGenerator.setNotAfter(end);
        certificateGenerator.setIssuerDN(issuerDn);
        certificateGenerator.setSubjectDN(dn);
        certificateGenerator.setPublicKey(keyPair.getPublic());
        certificateGenerator.setSignatureAlgorithm("SHA256WithRSA");
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    public static X509Certificate readCertificateFromStream(InputStream in) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(in);
    }

    static Map<X500Principal, X509Certificate> getCertificatesFromUrls(Iterable<String> urls) {
        try {
            LOGGER.finer("Getting certs for " + urls + " from cache.");
            return CA_CERTS_CACHE.get(urls);
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<X500Principal, X509Certificate> getCertificatesFromUrls(String... urls) {
        return getCertificatesFromUrls(asList(urls));
    }

    static X509Certificate readCertificateFromFile(File file) throws FileNotFoundException, CertificateException {
        LOGGER.log(Level.FINER, "Reading certificate from file: " + file.getAbsolutePath());
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStream streamForUrl = new BufferedInputStream(fileInputStream);

        try {
            return CertificateUtils.readCertificateFromStream(streamForUrl);
        }
        finally {
            IOUtils.closeQuietly(streamForUrl);
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    public static X509Certificate readCertificateFromUrl(URI uri) throws CertificateException {
        LOGGER.log(Level.FINER, "Reading certificate from url: " + uri);
        InputStream streamForUrl;

        try {
            streamForUrl = MiscUtils.getDataSourceForUrl(uri).getInputStream();
        }
        catch (IOException e) {
            throw new CertificateException(e);
        }

        try {
            return CertificateUtils.readCertificateFromStream(streamForUrl);
        }
        finally {
            IOUtils.closeQuietly(streamForUrl);
        }
    }

    private static PrivateKey readPkcs8Key(byte[] privateKeyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String algorithmDoesntMatterForPKCS8 = "RSA";
        KeyFactory keyFactory = KeyFactory.getInstance(algorithmDoesntMatterForPKCS8);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(disarmPkcs8Pem(privateKeyBytes)));
    }

    private static byte[] disarmPkcs8Pem(byte[] privateKeyBytes) {
        try {
            String bytesAsString = EncodingUtils.bytesToString(privateKeyBytes, EncodingUtils.ASCII);
            if (null != (bytesAsString = substringBetween(bytesAsString, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----"))) {
                privateKeyBytes = Base64.fromBase64String(bytesAsString).toBytes();
            }
        }
        catch (EncodingUtils.EncodingException ignored) {
        }

        return privateKeyBytes;
    }

    public static PrivateKey readKey(URI keyUri) {
        byte[] privateKeyBytes = MiscUtils.getBytesFromUri(keyUri);
        try {
            KeyPair keyPair = (KeyPair) new PEMParser(new InputStreamReader(new ByteArrayInputStream(privateKeyBytes), EncodingUtils.ASCII)).readObject();

            if (null != keyPair) {
                return keyPair.getPrivate();
            }

            return readPkcs8Key(privateKeyBytes);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to read key from " + keyUri + " It needs to be in ASCII-armored (PEM) or PKCS8 or PKCS8.PEM format.", e);
        }
    }

	public static String getIssuer(X509Certificate[] certificates) {
		String retval = null;
		X509Certificate certificate = getFirstCertificateOrNull(certificates);
		
		if (certificate != null) {
			retval = ldap(certificate.getIssuerX500Principal().getName(), "O");
		}

		return retval;
	}

	public static X509Certificate getFirstCertificateOrNull(X509Certificate[] certificates) {
        if (certificates == null) {
            LOGGER.fine("Got no certificate chain.");
            return null;
        }

        if (certificates.length == 0) {
            LOGGER.warning("Got zero length certificate chain. This should probably not happen.");
            return null;
        }

        LOGGER.fine("Got a certificate chain of " + certificates.length + " certificates.");
        return certificates[0];
	}

	private static String ldap(String source, String identifier) {
		String retval = "";

		try {
			LdapName ldapDN = new LdapName(source);
			for (Rdn rdn : ldapDN.getRdns()) {
				if (identifier.equals(rdn.getType())) {
					retval = (String) rdn.getValue();
					break;
				}
			}
		}
		catch (InvalidNameException e) {
		}

		return retval;
	}
}
