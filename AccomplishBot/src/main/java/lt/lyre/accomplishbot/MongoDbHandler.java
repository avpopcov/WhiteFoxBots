package lt.lyre.accomplishbot;

import com.mongodb.MongoClient;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.models.UserListItem;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class MongoDbHandler {
    private static final String DATABASE = "accomplish_bot";
    private static final String HOST = "192.168.1.132";
    private static final int PORT = 27017;
    private Datastore mongoDatastore;

    public MongoDbHandler() {
        try {
            final Morphia morphia = new Morphia();
            morphia.mapPackage("lt.lyre.accomplishbot.models");
            mongoDatastore = morphia.createDatastore(new MongoClient(HOST, PORT), DATABASE);
            mongoDatastore.ensureIndexes();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void insertListItem(String listName, List<String> items, long telegramId) {
        UserList list = new UserList();
        list.setTelegramId(telegramId);
        list.setListName(listName);
        list.getItems().addAll(items.stream().map(item -> new UserListItem(item)).collect(Collectors.toList()));

        mongoDatastore.save(list);
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
