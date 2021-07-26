package rs.ltt.push;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import rs.ltt.jmap.gson.JmapAdapters;

public final class Services {

    public static final Gson GSON;

    static {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        JmapAdapters.register(gsonBuilder);
        GSON = gsonBuilder.create();
    }

    private Services() {

    }
}
