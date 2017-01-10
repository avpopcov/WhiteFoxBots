package lt.lyre.accomplishbot.models;

import lombok.Data;
import lt.lyre.accomplishbot.commands.BotCommands;
import lt.lyre.accomplishbot.localization.Languages;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.Date;
import java.util.List;

@Data
@Entity("door")
public class Door {
    public Door() {
        added = new Date();
    }

    @Id
    private ObjectId id;

    @Property("isClosed")
    private boolean closed;

    @Property("creationDate")
    private Date added;
}
