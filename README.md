# Ltt.rs Push Server

## Direct push delivery

With JMAP servers gaining [VAPID](https://www.rfc-editor.org/rfc/rfc8292.html) support this 'proxy' server has become unnecessary. VAPID allows JMAP servers to deliver [WebPush](https://www.rfc-editor.org/rfc/rfc8030.html) notifications directly to [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging).

This project has been archived as a consequence of that.

## Historic use

This server receives pushes ([StateChanges](https://datatracker.ietf.org/doc/html/rfc8620#section-7.1) and PushVerifications) from a JMAP server and forwards them to [Ltt.rs for Android](https://github.com/inputmice/lttrs-android) via [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging).

Ltt.rs for Android will create a [PushSubscription](https://datatracker.ietf.org/doc/html/rfc8620#section-7.2) on the user’s JMAP server with the URL of this push server. (The official PlayStore distribution uses `https://push.ltt.rs`.)

This intermediate server (proxy) is required for two reasons:
* A JMAP server doesn’t know how to talk to Firebase.
* Only the app developer has the private keys to send push messages to the app.

The latter means that if you want to run your own push server you need to compile the app and sign up for Firebase Could Messaging. Likewise if you want to compile the playstore flavor of Ltt.rs for Android you need to set up your own push server.

### Compile
```shell
./gradlew shadowJar
```

### Run
```shell
java -jar build/libs/lttrs-push-server-0.1-all.jar -l 127.0.0.1 -p 1234 -s /path/to/service-accounts.json
```
