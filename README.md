# Ltt.rs Push Server

This server receives pushes (StateChanges and PushVerifications) from a JMAP server and sends them to [Ltt.rs for Android](https://github.com/inputmice/lttrs-android) via [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging).

Ltt.rs for Android will create a PushSubscription on the user’s JMAP server with the URL of this push server. (`https://push.ltt.rs` in the official distribution.)

### Compile
```shell
./gradlew shadowJar
```

### Run
```shell
java -jar build/libs/lttrs-push-server-0.1-all.jar -l 127.0.0.1 -p 1234 -s /path/to/service-accounts.json
```
