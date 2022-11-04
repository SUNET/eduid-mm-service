# Input files from the Navet CA operator
#   https://eid.expisoft.se/expitrust-test-certifikat/
#
CA_CERT    := ExpiTrust-Test-CA-v7.cer
CLIENT_P12 := Kommun_B.p12
# NEW CLIENT_PIN := 9335362769630050
# OLD CLIENT_PIN := 3625092702451054
CLIENT_PIN := 9335362769630050

all: remove_keystore keystore client_p8 clean

remove_keystore:
	rm -f trust.jks

keystore:
	keytool -import -trustcacerts -alias root -file ${CA_CERT} -keystore trust.jks -storepass SECRET -noprompt

pems:
	openssl pkcs12 -in ${CLIENT_P12} -passin "pass:${CLIENT_PIN}" -out client.crt.pem -clcerts -nokeys
	openssl pkcs12 -in ${CLIENT_P12} -passin "pass:${CLIENT_PIN}" -out client.key.pem -nocerts -nodes
	openssl x509 -inform der -in ${CA_CERT} -out ca.pem

show_cert: pems
	openssl x509 -text < client.crt.pem

show_ca: pems
	openssl x509 -text < ca.pem

curl_test: pems
	curl --key client.key.pem --cert client.crt.pem --cacert ca.pem https://notarealhost.skatteverket.se/webservice/accao/Recipient

s_client_test: pems
	openssl s_client -connect notarealhost.skatteverket.se:443 -CAfile ca.pem -cert client.crt.pem -key client.key.pem -showcerts

client_p8: pems
	# make a private key in the PKCS8 format without encryption, because that's what the Java code requires
	openssl pkcs8 -topk8 -in client.key.pem -out Kommun_B.p8 -outform DER -nocrypt

clean:
	rm -f client.crt.pem client.key.pem
