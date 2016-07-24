package lt.lyre.accomplishbot.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Data
@Embedded
public class UserListItem {
    public UserListItem() {
        id = new ObjectId();
    }

    public UserListItem(String itemName) {
        id = new ObjectId();
        this.itemName = itemName;
    }

    @Property("_id")
    private ObjectId id;

    @Property("itemName")
    private String itemName;

    @Property("isFinished")
    private boolean isFinished;
}
