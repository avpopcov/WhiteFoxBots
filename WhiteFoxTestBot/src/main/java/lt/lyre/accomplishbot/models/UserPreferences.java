package lt.lyre.accomplishbot.models;

import lombok.Data;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by avpop on 2017-01-10.
 */
@Data
public class UserPreferences  {

    public UserPreferences() {
        this.showNotificationsDoor = false;
    }

    @Property("showNotificationsDoor")
    private boolean showNotificationsDoor;
}