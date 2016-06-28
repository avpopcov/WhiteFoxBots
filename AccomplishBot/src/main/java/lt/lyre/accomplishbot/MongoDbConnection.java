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

    public void insertUserQuery(long telegramId, String command) {
        DBCollection table = getMongoDatabase().getCollection("users");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("telegramId", telegramId);

        DBCursor cursor = table.find(searchQuery);

        if (!cursor.hasNext()) {
            return;
        }

        DBObject userObject = cursor.next();
        userObject.put("lastCommand", command);

        table.update(searchQuery, userObject);
    }

    public User getUserByTelegramId(long telegramId) {
        DBCollection table = getMongoDatabase().getCollection("users");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("telegramId", telegramId);

        DBCursor cursor = table.find(searchQuery);

        if (!cursor.hasNext()) {
            return null;
        }

        DBObject userObject = cursor.next();

        User user = new User();

        user.setTelegramId(Long.valueOf(userObject.get("telegramId").toString()));
        user.setUserName(userObject.get("userName").toString());

        return user;
    }
}
