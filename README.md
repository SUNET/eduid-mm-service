# eduID "Mina meddelanden" service

A java based gateway service to "Mina meddelanden".

## Getting started

### Build the application
```bash
$ cd /path/to/eduid-mm-service
$ mvn package
...
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  42.917 s
[INFO] Finished at: 2023-01-09T16:11:36+01:00
[INFO] ------------------------------------------------------------------------
```

### Run the application
```bash
$ cd /path/to/eduid-mm-service
$ java -jar target/eduid-mm-service-0.1-SNAPSHOT.jar -c src/test/resources/mm-service.properties 
[main] INFO org.eclipse.jetty.util.log - Logging initialized @149ms to org.eclipse.jetty.util.log.Slf4jLog
[main] INFO org.eclipse.jetty.server.Server - jetty-9.4.50.v20221201; built: 2022-12-01T22:07:03.915Z; git: da9a0b30691a45daf90a9f17b5defa2f1434f882; jvm 11.0.16+8-post-Ubuntu-0ubuntu122.04
[main] INFO org.eclipse.jetty.server.session - DefaultSessionIdManager workerName=node0
[main] INFO org.eclipse.jetty.server.session - No SessionScavenger set, using defaults
[main] INFO org.eclipse.jetty.server.session - node0 Scavenging every 660000ms
[main] INFO org.eclipse.jetty.server.handler.ContextHandler - Started o.e.j.s.ServletContextHandler@551aa95a{/,null,AVAILABLE}
[main] INFO org.eclipse.jetty.server.AbstractConnector - Started ServerConnector@3f2a3a5{HTTP/1.1, (http/1.1)}{localhost:8880}
[main] INFO org.eclipse.jetty.server.Server - Started @258ms
```

### Using the application

#### User is reachable
```bash
$ curl -s -X POST http://localhost:8880/user/reachable --header 'Content-Type: application/json' --data '{"identity_number": "192705178354"}' | jq
{
  "SenderAccepted": true,
  "AccountStatus": {
    "RecipientId": "192705178354",
    "Type": "Secure",
    "ServiceSupplier": {
      "ServiceAddress": "https://notarealhost.skatteverket.se/webservice/acc1accao/Service/v3"
    }
  }
}
```

#### User is not reachable
```bash
$ curl -s -X POST http://localhost:8880/user/reachable --header 'Content-Type: application/json' --data '{"identity_number": "191212121212"}' | jq
{
  "SenderAccepted": false,
  "AccountStatus": {
    "RecipientId": "191212121212",
    "Type": "Not",
    "ServiceSupplier": {
      "ServiceAddress": ""
    }
  }
}
```

#### Send a message successfully
```bash
$ curl -s -X POST http://localhost:8880/message/send --header 'Content-Type: application/json' --data '{"recipient": "192705178354", "subject": "Test message subject", "message": "This is the test message.", "language": "svSE", "content_type": "text/plain"}' | jq
{
  "recipient": "192705178354",
  "transaction_id": "d2721b4f-5f5e-43c9-9f9d-62f0ca0d8183",
  "delivered": true
}
```

#### Failing to send a message
```bash
$ curl -s -X POST http://localhost:8880/message/send --header 'Content-Type: application/json' --data '{"recipient": "191212121212", "subject": "Test message subject", "message": "This is the test message.", "language": "svSE", "content_type": "text/plain"}' | jq
{
  "recipient": "191212121212",
  "transaction_id": "6f549beb-f884-4b3d-82e8-beff2e01d863",
  "delivered": false
}
```