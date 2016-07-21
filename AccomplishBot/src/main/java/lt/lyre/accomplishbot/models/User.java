package lt.lyre.accomplishbot.models;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Languages getLanguage() {
        return language;
    }

    public void setLanguage(Languages language) {
        this.language = language;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
    }

    public Integer getLastListMessageId() {
        return lastListMessageId;
    }

    public void setLastListMessageId(Integer lastListMessageId) {
        this.lastListMessageId = lastListMessageId;
    }

    public void setLastIncomingQueryCommand(String lastIncomingQueryCommand) {
        this.lastIncomingQueryCommand = lastIncomingQueryCommand;
    }

    public String getLastIncomingQueryCommand() {
        return lastIncomingQueryCommand;
    }

    public UserList getCurrentList() {
        return currentList;
    }

    public void setCurrentList(UserList currentList) {
        this.currentList = currentList;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }
}
