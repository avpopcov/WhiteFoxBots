package lt.lyre.accomplishbot.models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Entity("lists")
public class UserList {
    public UserList() {
        items = new ArrayList<>();
    }

    @Id
    private ObjectId id;

    @Property("telegramId")
    private long telegramId;

    @Property("listName")
    private String listName;

    @Embedded
    private List<UserListItem> items;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public List<UserListItem> getItems() {
        return items;
    }

    public void setItems(List<UserListItem> items) {
        this.items = items;
    }
}
