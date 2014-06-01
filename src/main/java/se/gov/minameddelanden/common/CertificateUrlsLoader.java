package se.gov.minameddelanden.common;

import com.google.common.base.Function;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

class CertificateUrlsLoader implements Function<Iterable<String>, Map<X500Principal, X509Certificate>> {
    private static final Logger LOGGER = Logger.getLogger(CertificateUrlsLoader.class.getName());
    public static final Pattern URI_SCHEME_REGEXP = Pattern.compile("^[a-zA-Z][\\w.+-]+:");

    static CertificateUrlsLoader certificatesFromUrls() {
        return new CertificateUrlsLoader();
    }

    @Override
    public Map<X500Principal, X509Certificate> apply(Iterable<String> urls) {
        return readCertsFromUrls(urls);
    }

    private Map<X500Principal, X509Certificate> readCertsFromUrls(Iterable<String> urls) {
        LOGGER.fine("Reading certs from " + urls);
        Map<X500Principal, X509Certificate> caCerts = new HashMap<X500Principal, X509Certificate>();
        Map<String, Throwable> exceptions = new LinkedHashMap<String, Throwable>();
        for (String url : urls) {
            URI uri;
            try {
                uri = parseUrlOrAssumeFileUrl(url);
            } catch (URISyntaxException e) {
                LOGGER.log(Level.WARNING, "Could not parse URI or find file: " + url, e);
                exceptions.put(url, e);
                continue;
            }
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                caCerts.putAll(readCertsFromDirectoryOrFile(new File(uri)));
            } else {
                try {
                    X509Certificate caCert = CertificateUtils.readCertificateFromUrl(uri);
                    addCertToMap(caCert, caCerts);
                } catch (CertificateException e) {
                    LOGGER.log(Level.WARNING, "Could not read cert from " + url, e);
                    exceptions.put(url, e);
                }
            }
        }
        if (caCerts.isEmpty()) {
            throw new RuntimeException("Failed to read any certs from urls " + urls+": "+exceptions);
        }
        return caCerts;
    }

    private Map<X500Principal, X509Certificate> readCertsFromDirectoryOrFile(File file) {
        return readCertsFromDirectoryOrFile(file, new HashMap<X500Principal, X509Certificate>());
    }

    private static Map<X500Principal, X509Certificate> readCertsFromDirectoryOrFile(File file, Map<X500Principal, X509Certificate> caCerts) {
        if (!file.isDirectory()) {
            try {
                X509Certificate caCert = CertificateUtils.readCertificateFromFile(file);
                addCertToMap(caCert, caCerts);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Could not read certificate from " + file, e);
            }
        } else {
            if (hasReadPermission(file)) {
                for (File child : file.listFiles()) {
                    readCertsFromDirectoryOrFile(child, caCerts);
                }
            }
        }
        return caCerts;
    }

    private static boolean hasReadPermission(File file) {
        return nullWhenMissingReadPermission(file) != null;
    }

    private static File[] nullWhenMissingReadPermission(File file) {
        return file.listFiles();
    }

    private static X509Certificate addCertToMap(X509Certificate caCert, Map<X500Principal, X509Certificate> caCerts) {
        return caCerts.put(caCert.getSubjectX500Principal(), caCert);
    }

    static URI parseUrlOrAssumeFileUrl(final String uriString) throws URISyntaxException {
        URI uri;
        if (URI_SCHEME_REGEXP.matcher(uriString).lookingAt()) {
            uri = new URI(uriString);
        } else {
            uri = assumeFilePath(uriString);
        }
        if (!uriString.equals(uri.toString())) {
            LOGGER.fine("Interpreting "+ uriString +" as "+uri);
        }
        return uri;
    }

    private static URI assumeFilePath(String uriString) throws URISyntaxException {
        String assumingFilePath = uriString.replace('\\', '/');
        if (!assumingFilePath.startsWith("/")) {
            assumingFilePath = "/"+assumingFilePath;
        }
        return new URI("file:"+assumingFilePath);
    }

}
