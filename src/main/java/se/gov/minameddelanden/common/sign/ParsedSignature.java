package se.gov.minameddelanden.common.sign;

import java.security.cert.X509Certificate;

public class ParsedSignature {

    private final X509Certificate signingCertificate;
    private final String name;
    private final String personNummer;

    public ParsedSignature(X509Certificate signingCertificate, String name, String personNummer) {
        this.signingCertificate = signingCertificate;
        this.name = name;
        this.personNummer = personNummer;
    }

    public String getName() {
        return name;
    }

    public String getPersonNummer() {
        return personNummer;
    }

    public X509Certificate getSigningCertificate() {
        return signingCertificate;
    }

}
