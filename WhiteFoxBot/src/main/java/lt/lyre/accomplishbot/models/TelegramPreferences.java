package lt.lyre.accomplishbot.models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import java.util.List;

/**
 * Created by avpop on 2017-01-10.
 */
public class TelegramPreferences {

    public TelegramPreferences() {
        showNotifications = false;
    }

    @Property("showNotifications")
    private boolean showNotifications;

}
