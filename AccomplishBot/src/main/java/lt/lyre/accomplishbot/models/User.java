package lt.lyre.accomplishbot.models;

import java.util.Date;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class User {

    public User() {
        added = new Date();
    }

    private long telegramId;
    private String userName;
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

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }
}
