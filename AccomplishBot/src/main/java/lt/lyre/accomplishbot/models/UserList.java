package lt.lyre.accomplishbot.models;

import lombok.Data;
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
@Data
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
}
