package lt.lyre.accomplishbot.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity("lists")
public class UserList extends UserListHeader{
    public UserList() {
        items = new ArrayList<>();
    }

    @Property("telegramId")
    private long telegramId;

    @Embedded
    private List<UserListItem> items;
}
