package lt.lyre.accomplishbot.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by avpopcov on 16.7.24.
 */
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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}
