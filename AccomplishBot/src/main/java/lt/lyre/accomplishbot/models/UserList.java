package lt.lyre.accomplishbot.models;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Data
@Entity("lists")
public class UserList {
    public UserList() {
    }

    public UserList(String name) {
        listName= name;
    }

    @Id
    private ObjectId id = new ObjectId();

    @Property("telegramId")
    private long telegramId;

    @Property("listName")
    private String listName;

    @Property("isFinished")
    private boolean isFinished;

    @Reference
    private UserList parent;
}
