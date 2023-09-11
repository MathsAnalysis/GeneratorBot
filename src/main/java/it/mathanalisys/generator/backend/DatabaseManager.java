package it.mathanalisys.generator.backend;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import it.mathanalisys.generator.Generator;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@Data
public class DatabaseManager {

    private MongoClient client;
    private MongoDatabase database;
    private MongoCredential credential;

    private MongoCollection<Document>
            files, filesPlus, filesPlusPlus, cooldowns;


    @SneakyThrows
    public DatabaseManager(){
        boolean authEnabled = Generator.get().getConfiguration().getBoolean("MONGO-AUTHENTICATION-ENABLED");

        if (authEnabled){
            credential = MongoCredential.createCredential(
                    Generator.get().getConfiguration().getString("MONGO-AUTHENTICATION-USERNAME"),
                    Generator.get().getConfiguration().getString("MONGO-AUTHENTICATION-DATABASE"),
                    Generator.get().getConfiguration().getString("MONGO-AUTHENTICATION-PASSWORD").toCharArray()
            );
            client = new MongoClient(new ServerAddress(
                    Generator.get().getConfiguration().getString("MONGO-HOST"),
                    Generator.get().getConfiguration().getInt("MONGO-PORT")
            ), credential, MongoClientOptions.builder().build());
            System.out.println("MongoDB connected with authentication");
        }else {
            client = new MongoClient(new ServerAddress(
                    Generator.get().getConfiguration().getString("MONGO-HOST"),
                    Generator.get().getConfiguration().getInt("MONGO-PORT")
            ));
            System.out.println("MongoDB is connected! \nWe recommend that you use the authentication!");
        }


        this.database = client.getDatabase(Generator.get().getConfiguration().getString("MONGO-DATABASE"));
        this.files = database.getCollection("basic");
        this.filesPlus = database.getCollection("basic_plus");
        this.filesPlusPlus = database.getCollection("basic_plus_plus");
        this.cooldowns = database.getCollection("cooldowns");
    }

    public Document getAndRemoveAccountPlusPlus() {
        List<Document> randomAccounts = filesPlusPlus.aggregate(List.of(Aggregates.sample(1))).into(new ArrayList<>());

        if (randomAccounts.isEmpty()) {
            return null;
        }

        Document randomAccount = randomAccounts.get(0);

        filesPlus.deleteOne(new Document("_id", randomAccount.getObjectId("_id")));

        return randomAccount;
    }


    public Document getAndRemoveAccountPlus() {
        List<Document> randomAccounts = filesPlus.aggregate(List.of(Aggregates.sample(1))).into(new ArrayList<>());

        if (randomAccounts.isEmpty()) {
            return null;
        }

        Document randomAccount = randomAccounts.get(0);
        filesPlus.deleteOne(new Document("_id", randomAccount.getObjectId("_id")));
        return randomAccount;
    }

    public Document getAndRemoveRandomAccount() {
        List<Document> randomAccounts = files.aggregate(List.of(Aggregates.sample(1))).into(new ArrayList<>());

        if (randomAccounts.isEmpty()) {
            return null;
        }

        Document randomAccount = randomAccounts.get(0);
        files.deleteOne(new Document("_id", randomAccount.getObjectId("_id")));

        return randomAccount;
    }


    public void addCooldown(String userId, int durationInSeconds, String name, String type) {
        long expiryTimestamp = System.currentTimeMillis() + (durationInSeconds * 1000L);
        Document doc = new Document("userId", userId)
                .append("expiryTimestamp", expiryTimestamp)
                .append("name", name)
                .append("type", type);
        cooldowns.insertOne(doc);
    }

    public boolean isInCooldown(String userId, String type) {
        Document query = new Document("userId", userId).append("type", type);
        Document cooldownEntry = cooldowns.find(query).first();
        if (cooldownEntry != null) {
            long expiryTimestamp = cooldownEntry.getLong("expiryTimestamp");
            if (System.currentTimeMillis() < expiryTimestamp) {
                return true;
            } else {
                cooldowns.deleteOne(query);  // Rimuovi il cooldown scaduto
            }
        }
        return false;
    }

    public int getRemainingCooldown(String userId, String type) {
        Document query = new Document("userId", userId).append("type", type);
        Document cooldownEntry = cooldowns.find(query).first();
        if (cooldownEntry != null) {
            long expiryTimestamp = cooldownEntry.getLong("expiryTimestamp");
            long remainingTime = expiryTimestamp - System.currentTimeMillis();
            return (int) (remainingTime / 1000);
        }
        return 0;
    }

}
