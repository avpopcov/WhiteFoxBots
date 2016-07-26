package lt.lyre.accomplishbot.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by Dmitrij on 2016-06-24.
 */
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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}