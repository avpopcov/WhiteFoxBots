package lt.lyre.accomplishbot;

import com.mongodb.*;
import lt.lyre.accomplishbot.models.User;

import java.net.UnknownHostException;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class MongoDbConnection {
    public DB mongoDatabase;

    public DB getMongoDatabase() {
        return mongoDatabase;
    }

    public MongoDbConnection() {
        try {
            MongoClient mongo = new MongoClient("192.168.1.132", 27017);
            mongoDatabase = mongo.getDB("accomplish_bot");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(User user) {
        DBCollection table = getMongoDatabase().getCollection("users");
        BasicDBObject document = new BasicDBObject();
        document.put("userName", user.getUserName());
        document.put("telegramId", user.getTelegramId());
        document.put("createdDate", user.getAdded());
        table.insert(document);
    }


    public boolean shouldCreateUserById(long telegramId) {
        DBCollection table = getMongoDatabase().getCollection("users");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("telegramId", telegramId);

        DBCursor cursor = table.find(searchQuery);

        return !cursor.hasNext();
    }
}
