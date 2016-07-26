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

    @Property("rootListId")
    private ObjectId rootListId;

    @Property("manageView")
    private boolean manageView;

    @Property("visibleCheckedMessages")
    private boolean visibleCheckedMessages;

    @Property("visibleMoreFunctions")
    private boolean visibleMoreFunctions;

    @Property("textDelimiter")
    private String delimiter;

    @Property("userLanguage")
    private Languages language;

    @Property("firstName")
    private String firstName;

    @Property("lastCommand")
    private String lastCommand;

    @Property("lastIncomingQueryCommand")
    private String lastIncomingQueryCommand;

    @Property("lastListMessageId")
    private Integer lastListMessageId;

    @Property("currentListId")
    private ObjectId currentListId;

    @Property("creationDate")
    private Date added;

    @Embedded
    private List<UserListHeader> lists;
}
