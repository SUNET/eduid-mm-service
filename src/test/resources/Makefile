# Input files from the Navet CA operator
#   https://eid.expisoft.se/expitrust-test-certifikat/
#
CA_CERT    := ExpiTrust-Test-CA-v7.cer
CLIENT_NAME := Kommun_A
CLIENT_PIN := 1503000243252622

all: remove_keystore keystore client_p8 clean

remove_keystore:
	rm -f trust.jks

keystore:
	keytool -import -trustcacerts -alias root -file ${CA_CERT} -keystore trust.jks -storepass SECRET -noprompt

pems:
	openssl pkcs12 -in ${CLIENT_NAME}.p12 -passin "pass:${CLIENT_PIN}" -out ${CLIENT_NAME}.crt.pem -clcerts -nokeys
	openssl pkcs12 -in ${CLIENT_NAME}.p12 -passin "pass:${CLIENT_PIN}" -out ${CLIENT_NAME}.key.pem -nocerts -nodes
	openssl x509 -inform der -in ${CA_CERT} -out ca.pem
	cp ${CLIENT_NAME}.crt.pem ${CLIENT_NAME}.crt

show_cert: pems
	openssl x509 -text < ${CLIENT_NAME}.crt.pem

show_ca: pems
	openssl x509 -text < ca.pem

curl_test: pems
	curl --key ${CLIENT_NAME}.key.pem --cert ${CLIENT_NAME}.crt.pem --cacert ca.pem https://notarealhost.skatteverket.se/webservice/accao/Recipient

s_client_test: pems
	openssl s_client -connect notarealhost.skatteverket.se:443 -CAfile ca.pem -cert ${CLIENT_NAME}.crt.pem -key ${CLIENT_NAME}.key.pem -showcerts

client_p8: pems
	# make a private key in the PKCS8 format without encryption, because that's what the Java code requires
	openssl pkcs8 -topk8 -in ${CLIENT_NAME}.key.pem -out ${CLIENT_NAME}.p8 -outform DER -nocrypt

clean:
	rm -f ${CLIENT_NAME}.crt.pem ${CLIENT_NAME}.key.pem
