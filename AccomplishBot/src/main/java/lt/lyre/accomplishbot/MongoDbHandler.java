package lt.lyre.accomplishbot;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import lt.lyre.accomplishbot.configuration.MongoDbConfig;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.models.UserList;
import lt.lyre.accomplishbot.models.UserListItem;
import lt.lyre.accomplishbot.utils.CollectionHelper;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;
import java.util.stream.Collectors;

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

    public boolean finishListItem(ObjectId listId, ObjectId listItemId, long telegramId) {
        UpdateOperations<UserList> updateOperations = mongoDatastore.createUpdateOperations
                (UserList.class).set("items.$.isFinished", true);

        mongoDatastore.update(mongoDatastore.createQuery(UserList.class)
                .filter("telegramId", telegramId)
                .filter("_id", listId)
                .filter("items._id", listItemId), updateOperations);

        return true;
    }

    public boolean redoListItem(ObjectId listId, ObjectId listItemId, long telegramId) {
        UpdateOperations<UserList> updateOperations = mongoDatastore.createUpdateOperations
                (UserList.class).set("items.$.isFinished", false);

        mongoDatastore.update(mongoDatastore.createQuery(UserList.class)
                .filter("telegramId", telegramId)
                .filter("_id", listId)
                .filter("items._id", listItemId), updateOperations);

        return true;
    }

    public void removeListItem(ObjectId listId, ObjectId listItemId, long telegramId) {
        UpdateOperations<UserList> updateOperations = mongoDatastore.createUpdateOperations
                (UserList.class)
                .disableValidation()
                .removeAll("items", new BasicDBObject("_id", listItemId))
                .enableValidation();

        mongoDatastore.update(mongoDatastore.createQuery(UserList.class)
                .filter("telegramId", telegramId)
                .filter("_id", listId), updateOperations);
    }

    public void removeList(ObjectId listId, long telegramId) {
        mongoDatastore.delete(mongoDatastore.createQuery(UserList.class)
                .filter("telegramId", telegramId)
                .filter("_id", listId));

        User user = getUserByTelegramId(telegramId);

        if (user != null) {
            user.setCurrentList(getUserListsByTelegramId(telegramId).stream().findFirst().orElse(null));
        }
    }

    public UserList insertListItem(ObjectId listId, List<String> items, long telegramId) {
        UserList list = getUserListById(listId, telegramId); // For now, we wanna have UNO list

        if (list == null) {
            list = new UserList();
            list.setTelegramId(telegramId);
            list.setListName("test");
        }

        list.getItems().addAll(items.stream().map(item -> new UserListItem(item)).collect(Collectors.toList()));

        mongoDatastore.save(list);

        return list;
    }

    public void setCurrentList(User user, UserList list) {
        user.setCurrentList(list);
        mongoDatastore.save(user);
    }

    public void insertUser(User user) {
        mongoDatastore.save(user);
    }

    public void updateUser(User user) {
        mongoDatastore.save(user);
    }

    public User getUserByTelegramId(long telegramId) {
        List<User> result = mongoDatastore.createQuery(User.class)
                .field("telegramId").equal(telegramId)
                .asList();

        return CollectionHelper.getGenericList(result);
    }

    public UserList getUserListById(ObjectId listId, long telegramId) {
        List<UserList> result = mongoDatastore.createQuery(UserList.class)
                .field("_id").equal(listId)
                .field("telegramId").equal(telegramId)
                .asList();

        return CollectionHelper.getGenericList(result);
    }

    public List<UserList> getUserListsByTelegramId(long telegramId) {
        return mongoDatastore.createQuery(UserList.class)
                .field("telegramId").equal(telegramId)
                .asList();
    }
}
