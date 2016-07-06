package lt.lyre.accomplishbot;

import com.mongodb.MongoClient;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

import lt.lyre.accomplishbot.configuration.MongoDbConfig;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.models.UserListItem;
import lt.lyre.accomplishbot.utils.CollectionHelper;

/**
 * Created by Dmitrij on 2016-06-24.
 */
public class MongoDbHandler {
    private Datastore mongoDatastore;

    public MongoDbHandler() {
        final Morphia morphia = new Morphia();
        morphia.mapPackage(MongoDbConfig.MAPPING_PACKAGE);
        mongoDatastore = morphia.createDatastore(new MongoClient(MongoDbConfig.HOST, MongoDbConfig.PORT), MongoDbConfig.DATABASE);
        mongoDatastore.ensureIndexes();
    }

    public boolean finishListItem(String listName, String listItemName, long telegramId) {
        UpdateOperations<UserList> updateOperations = mongoDatastore.createUpdateOperations
                (UserList.class).set("items.$.isFinished", true);

        mongoDatastore.update(mongoDatastore.createQuery(UserList.class)
            .filter("telegramId", telegramId)
            .filter("listName", listName)
            .filter("items.itemName", listItemName), updateOperations);

        return true;
    }

    public boolean redoListItem(String listName, String listItemName, long telegramId) {
        UpdateOperations<UserList> updateOperations = mongoDatastore.createUpdateOperations
            (UserList.class).set("items.$.isFinished", false);

        mongoDatastore.update(mongoDatastore.createQuery(UserList.class)
            .filter("telegramId", telegramId)
            .filter("listName", listName)
            .filter("items.itemName", listItemName), updateOperations);

        return true;
    }

    public void removeListItem(String listName, String listItemName, long telegramId) {
        mongoDatastore.delete(mongoDatastore.createQuery(UserList.class)
                .filter("telegramId", telegramId)
                .filter("listName", listName)
                .filter("items.itemName", listItemName));
    }

    public UserList insertListItem(String listName, List<String> items, long telegramId) {
        UserList list = getUserListByName(listName, telegramId); // For now, we wanna have UNO list

        if (list == null) {
            list = new UserList();
            list.setTelegramId(telegramId);
            list.setListName(listName);
        }

        list.getItems().addAll(items.stream().map(item -> new UserListItem(item)).collect(Collectors.toList()));

        mongoDatastore.save(list);

        return list;
    }

    public void insertUser(User user) {
        mongoDatastore.save(user);
    }

    public void updateUser(User user) {
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

        return CollectionHelper.getGenericList(result);
    }

    public UserList getUserListByName(String listName, long telegramId) {
        List<UserList> result = mongoDatastore.createQuery(UserList.class)
                .field("listName").equal(listName)
                .field("telegramId").equal(telegramId)
                .asList();

        return CollectionHelper.getGenericList(result);
    }

    public List<UserList> getUserListByTelegramId(long telegramId) {
        return mongoDatastore.createQuery(UserList.class)
                .field("telegramId").equal(telegramId)
                .asList();
    }
}
