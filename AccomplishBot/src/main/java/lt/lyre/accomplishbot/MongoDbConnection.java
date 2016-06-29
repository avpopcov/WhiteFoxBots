package lt.lyre.accomplishbot;

import com.mongodb.MongoClient;
import lt.lyre.accomplishbot.models.User;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class MongoDbConnection {
    public static final String ACCOMPLISH_BOT_DATABASE = "accomplish_bot";
    private Datastore mongoDatastore;

    public Datastore getMongoDatastore() {
        return mongoDatastore;
    }

    public MongoDbConnection() {
        try {
            final MongoClient mongo = new MongoClient("192.168.1.132", 27017);
            final Morphia morphia = new Morphia();
            morphia.mapPackage("lt.lyre.accomplishbot.models");
            mongoDatastore = morphia.createDatastore(mongo, ACCOMPLISH_BOT_DATABASE);
            mongoDatastore.ensureIndexes();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void insertListItem(String listName, List<String> items, long telegramId) {

//        DBCollection table = getMongoDatabase().getCollection("lists");
//        BasicDBObject document = new BasicDBObject();
//        document.put("listName", listName);
//        document.put("userId", telegramId);
//
//        List<BasicDBObject> objectItems = new ArrayList<>();
//
//        for (String item : items) {
//            BasicDBObject itemInstance = new BasicDBObject();
//            itemInstance.append("itemName", item);
//            itemInstance.append("isFinished", false);
//
//            objectItems.add(itemInstance);
//        }
//
//        document.put("items", objectItems);
//
//        table.insert(document);
    }

    public void insertUser(User user) {
        mongoDatastore.save(user);
    }

    public void logLastCommand(User user, String command) {
        final UpdateOperations<User> updateOperations = mongoDatastore.createUpdateOperations(User.class)
                .set("lastCommand", command);
        mongoDatastore.update(user, updateOperations);
    }

    public User getUserByTelegramId(long telegramId) {

        List<User> result = mongoDatastore.createQuery(User.class)
                .field("telegramId").equal(telegramId)
                .asList();

        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result.stream().findAny().get();
        }
    }
}
