package lt.lyre.accomplishbot;

import com.mongodb.MongoClient;
import lt.lyre.accomplishbot.configuration.MongoDbConfig;
import lt.lyre.accomplishbot.models.Door;
import lt.lyre.accomplishbot.models.User;
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

    public void setDoorState(ObjectId itemId, boolean isClosed) {
        Door door = new Door();

        door.setClosed(isClosed);
        door.setId(itemId);

        mongoDatastore.save(door);
    }

    public List<User> getOpenUsersChatId() {
        List<User> result = mongoDatastore.createQuery(User.class)
                .asList();

        return result;
    }
}
