package lt.lyre.accomplishbot.models;

import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.localization.Languages;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;
import java.util.List;

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

    public boolean isVisibleCheckedMessages() {
        return visibleCheckedMessages;
    }

    public void setVisibleCheckedMessages(boolean visibleCheckedMessages) {
        this.visibleCheckedMessages = visibleCheckedMessages;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setVisibleMoreFunctions(boolean visibleMoreFunctions) {
        this.visibleMoreFunctions = visibleMoreFunctions;
    }

    public boolean isVisibleMoreFunctions() {
        return visibleMoreFunctions;
    }

    public boolean isManageView() {
        return manageView;
    }

    public void setManageView(boolean manageView) {
        this.manageView = manageView;
    }

    public ObjectId getCurrentListId() {
        return currentListId;
    }

    public void setCurrentListId(ObjectId currentList) {
        this.currentListId = currentList;
    }

    public ObjectId getRootListId() {
        return rootListId;
    }

    public void setRootListId(ObjectId rootListId) {
        this.rootListId = rootListId;
    }

    public void setLists(List<UserListHeader> lists) {
        this.lists = lists;
    }

    public List<UserListHeader> getLists() {

        return lists;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }
}
