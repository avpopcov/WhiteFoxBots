package lt.lyre.accomplishbot.models;

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
        language = "English";
        lastCommand = "/add";
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
    private String language;

    @Property("lastCommand")
    private String lastCommand;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String lastCommand) {
        this.lastCommand = lastCommand;
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
