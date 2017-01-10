package lt.lyre.accomplishbot;

import com.mongodb.MongoClient;
import lt.lyre.accomplishbot.configuration.MongoDbConfig;
import lt.lyre.accomplishbot.models.Door;
import lt.lyre.accomplishbot.models.User;
import lt.lyre.accomplishbot.utils.CollectionHelper;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.ArrayList;
import java.util.List;

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

    public void insertUser(User user) {
        mongoDatastore.save(user);
    }

    public void setNewTrustedUser(String userName){
        User user = new User(userName);

        mongoDatastore.save(user);
    }

    public ArrayList<User> getAllTrustedUsers(){
        ArrayList<User> userList = new ArrayList<User>();
        return userList;
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

    public User getUserByUserName(String userName) {
        List<User> result = mongoDatastore.createQuery(User.class)
                .field("userName").equal(userName)
                .asList();

        return CollectionHelper.getGenericList(result);
    }

    public Door getCurrentDoorState(){
        Door door = new Door();

        return door;
    }

    public void setDoorState(ObjectId itemId, boolean isClosed) {
        Door door = new Door();

        door.setClosed(isClosed);
        door.setId(itemId);

        mongoDatastore.save(door);
    }
}
