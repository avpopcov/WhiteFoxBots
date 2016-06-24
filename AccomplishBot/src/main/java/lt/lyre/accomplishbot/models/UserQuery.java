package lt.lyre.accomplishbot.models;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class UserQuery {
    private long telegramId;
    private String query;

    public UserQuery(long telegramId, String query) {
        this.telegramId = telegramId;
        this.query = query;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
