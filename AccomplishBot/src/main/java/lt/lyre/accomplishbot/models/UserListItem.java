package lt.lyre.accomplishbot.models;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Embedded
public class UserListItem {
    public UserListItem() {
    }

    public UserListItem(String itemName) {
        this.itemName = itemName;
    }

    @Property("itemName")
    private String itemName;

    @Property("isFinished")
    private boolean isFinished;

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
