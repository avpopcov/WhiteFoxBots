package lt.lyre.accomplishbot.models;

import lombok.Data;
import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.localization.Languages;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;
import java.util.List;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Data
@Entity("users")
public class User {
    public User() {
        added = new Date();
        language = Languages.ENGLISH;
        admin = false;
    }

    public User(String userName) {
        this.userName = userName;
        telegramId = 0;
    }

    @Id
    private ObjectId id;

    @Property("telegramId")
    private long telegramId;

    @Property("userName")
    private String userName;

    @Property("isAdmin")
    private boolean admin;

    @Property("userLanguage")
    private Languages language;

    @Property("firstName")
    private String firstName;

    @Property("lastCommand")
    private String lastCommand;

    @Property("lastIncomingQueryCommand")
    private String lastIncomingQueryCommand;

    @Property("lastMessageId")
    private Integer lastMessageId;

    @Property("creationDate")
    private Date added;

    @Embedded
    private List<UserPreferences> userPreferences;
}
