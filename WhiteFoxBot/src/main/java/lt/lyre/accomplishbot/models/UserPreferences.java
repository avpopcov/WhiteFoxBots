package lt.lyre.accomplishbot.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import java.util.List;

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