package lt.lyre.accomplishbot.models;

import lombok.Data;
import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.localization.Languages;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

/**
 * Created by Dmitrij on 2016-06-24.
 */
@Data
@Entity("users")
public class User {
    public User() {
        added = new Date();
        delimiter = ",";
        language = Languages.ENGLISH;
        lastCommand = BotCommands.CMD_ADD.getCommandString();
    }

    @Id
    private ObjectId id;

    @Property("telegramId")
    private long telegramId;

    @Property("userName")
    private String userName;

    @Property("textDelimiter")
    private String delimiter;

    @Property("userLanguage")
    private Languages language;

    @Property("lastCommand")
    private String lastCommand;

    @Property("lastIncomingQueryCommand")
    private String lastIncomingQueryCommand;

    @Property("lastListMessageId")
    private Integer lastListMessageId;

    @Reference("currentList")
    private UserList currentList;

    @Property("creationDate")
    private Date added;
}
