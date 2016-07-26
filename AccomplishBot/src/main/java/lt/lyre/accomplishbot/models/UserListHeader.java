package lt.lyre.accomplishbot.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * Created by avpopcov on 16.7.24.
 */
@Data
public class UserListHeader {
    public UserListHeader(ObjectId id, String listName) {
        this.id = id;
        this.listName = listName;
    }

    public UserListHeader() {
    }

    @Id
    private ObjectId id;

    @Property("listName")
    private String listName;
}
