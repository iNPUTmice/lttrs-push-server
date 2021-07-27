# Ltt.rs Push Server

This server receives pushes ([StateChanges](https://datatracker.ietf.org/doc/html/rfc8620#section-7.1) and PushVerifications) from a JMAP server and sends them to [Ltt.rs for Android](https://github.com/inputmice/lttrs-android) via [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging).

Ltt.rs for Android will create a [PushSubscription](https://datatracker.ietf.org/doc/html/rfc8620#section-7.2) on the user’s JMAP server with the URL of this push server. (`https://push.ltt.rs` in the official PlayStore distribution.)

### Compile
```shell
./gradlew shadowJar
```

### Run
```shell
java -jar build/libs/lttrs-push-server-0.1-all.jar -l 127.0.0.1 -p 1234 -s /path/to/service-accounts.json
```
