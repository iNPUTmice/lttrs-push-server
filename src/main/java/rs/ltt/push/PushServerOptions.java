package rs.ltt.push;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class PushServerOptions  extends Options {
    public PushServerOptions() {
        super();
        addOption(new Option("l","listen",true,"IP address to listen on"));
        addOption(new Option("p","port", true, "Port to listen on"));
        addOption(new Option("s","service-account", true, "The path to the service account file (Download from Firebase console)"));
    }
}
