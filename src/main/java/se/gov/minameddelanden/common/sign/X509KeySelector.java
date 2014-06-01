package se.gov.minameddelanden.common.sign;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

public class X509KeySelector extends KeySelector {

    @SuppressWarnings("unchecked")
	public KeySelectorResult select(KeyInfo keyInfo,
                                    KeySelector.Purpose purpose,
                                    AlgorithmMethod method,
                                    XMLCryptoContext context) throws KeySelectorException {

        for (XMLStructure info : (List<XMLStructure>)keyInfo.getContent()) {
            if (!(info instanceof X509Data))
                continue;
            X509Data x509Data = (X509Data) info;
            for (Object o : x509Data.getContent()) {
                if (!(o instanceof X509Certificate))
                    continue;
                final PublicKey key = ((X509Certificate) o).getPublicKey();
                // Make sure the algorithm is compatible
                // with the method.
                String methodAlgorithm = method.getAlgorithm();
                String keyAlgorithm = key.getAlgorithm();
                if (algEquals(methodAlgorithm, keyAlgorithm)) {
                    return new KeySelectorResult() {
                        public Key getKey() {
                            return key;
                        }
                    };
                }
            }
        }
        
        throw new KeySelectorException("No key found!");
    }

    static boolean algEquals(String algURI, String algName) {
        try {
            SignatureUtils.checkJavaVersion6u18OrLaterSupportingRsaSha256And512(algURI);
            return new URI(algURI).getFragment().startsWith(algName.toLowerCase());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
