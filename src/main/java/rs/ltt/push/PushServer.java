package rs.ltt.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import com.google.common.primitives.Longs;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rs.ltt.jmap.common.entity.PushMessage;
import rs.ltt.jmap.common.entity.PushVerification;
import rs.ltt.jmap.common.entity.StateChange;
import spark.ExceptionHandler;
import spark.Route;
import spark.Spark;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class PushServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushServer.class);
    private static final String EMPTY_STRING = "";
    private static final Route execute = (request, response) -> {
        final String token = request.queryParams("token");
        final String cid = request.queryParams("cid");
        if (Strings.isNullOrEmpty(token)) {
            throw new IllegalArgumentException("Missing query parameter 'token'");
        }
        if (Strings.isNullOrEmpty(cid)) {
            throw new IllegalArgumentException("Missing query parameter 'cid'");
        }
        final Long credentialsId = Longs.tryParse(cid);
        if (credentialsId == null || credentialsId < 0) {
            throw new IllegalArgumentException("Query parameter 'cid' is not a valid number");
        }
        final PushMessage pushMessage = Services.GSON.fromJson(request.body(), PushMessage.class);
        LOGGER.info("called execute with token {}, cid {} and message {}", token, credentialsId, pushMessage);
        if (execute(token, credentialsId, pushMessage)) {
            return EMPTY_STRING;
        } else {
            response.status(404);
            return "Push target not found";
        }
    };
    private static final ExceptionHandler<? super RuntimeException> exception = (exception, request, response) -> {
        response.status(400);
        final String message = exception.getMessage();
        if (Strings.isNullOrEmpty(message)) {
            response.body(exception.getClass().getSimpleName());
        } else {
            response.body(message);
        }
    };

    private static boolean execute(final String token, final long cid, final PushMessage pushMessage) {
        if (pushMessage instanceof StateChange) {
            return execute(token, cid, (StateChange) pushMessage);
        }
        if (pushMessage instanceof PushVerification) {
            return execute(token, cid, (PushVerification) pushMessage);
        }
        throw new IllegalStateException(
                String.format("Handling of %s is not implemented", pushMessage.getClass().getSimpleName())
        );
    }

    private static boolean execute(final String token, final long cid, final StateChange stateChange) {
        final List<Message> messages = Messages.of(token, cid, stateChange);
        final int successCount;
        try {
            successCount = FirebaseMessaging.getInstance().sendAll(messages).getSuccessCount();
        } catch (final FirebaseMessagingException e) {
            LOGGER.info("Unable to send StateChange", e);
            return false;
        }
        return successCount == messages.size();
    }

    private static boolean execute(final String token, final long cid, final PushVerification verification) {
        try {
            FirebaseMessaging.getInstance().send(Messages.of(token, cid, verification));
            return true;
        } catch (final FirebaseMessagingException e) {
            LOGGER.info("Unable to send PushVerification", e);
            return false;
        }
    }

    public static void main(final String... args) {
        try {
            final DefaultParser defaultParser = new DefaultParser();
            final CommandLine commandLine = defaultParser.parse(new PushServerOptions(), args);
            final String ip = commandLine.getOptionValue("listen");
            final String port = commandLine.getOptionValue("port");
            final String serviceAccount = commandLine.getOptionValue("service-account");
            if (Strings.isNullOrEmpty(ip) || Strings.isNullOrEmpty(port) || Strings.isNullOrEmpty(serviceAccount)) {
                throw new IllegalArgumentException("Missing parameter");
            }
            final File serviceAccountFile = new File(serviceAccount);
            start(ip, Integer.parseInt(port), serviceAccountFile);
        } catch (final Exception e) {
            System.err.println(e.getMessage());
            System.err.print("\n");
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar lttrs-push-server-0.1-all.jar", new PushServerOptions());
        }
    }

    public static void start(final String ip, final int port, final File file) {
        try {
            final FileInputStream serviceAccount = new FileInputStream(file);
            final FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            LOGGER.info("Unable to initialize Firebase");
        }
        Spark.ipAddress(ip);
        Spark.port(port);
        Spark.exception(RuntimeException.class, PushServer.exception);
        Spark.get("/", (request, response) -> {
            response.redirect("https://github.com/inputmice/lttrs-push-server");
            return null;
        });
        Spark.post("/execute", PushServer.execute);
    }

}
