package moe.quill.stratumcommon.Services;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import moe.quill.StratumCommon.Database.IDatabaseService;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DatabaseService implements IDatabaseService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public DatabaseService(Plugin plugin) {

        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .build());
        this.database = mongoClient.getDatabase(
                Objects.requireNonNull(plugin.getConfig().getString("database.name"))
        );
    }

    /**
     * Get a colection from the database
     *
     * @param collectionName to get
     * @return the collection matching the given string
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    /**
     * Get the mongodb client from the db service
     *
     * @return the mongo client from the database manager
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
