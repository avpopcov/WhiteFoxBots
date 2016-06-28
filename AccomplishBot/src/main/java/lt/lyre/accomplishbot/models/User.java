package lt.lyre.accomplishbot.models;

import java.util.Date;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class User {
    public User() {
        added = new Date();
        delimiter = ",";
        language = "English";
    }

    private long telegramId;
    private String userName;
    private String delimiter;
    private String language;
    private String lastCommand;
    private Date added;

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

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }
}
