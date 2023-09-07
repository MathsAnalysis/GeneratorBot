package it.mathanalisys.generator.backend;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import lombok.Data;
import lombok.SneakyThrows;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class DatabaseManager {

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document>
            files, filesPlus, filesPlusPlus, cooldowns;


    private static final String DB_NAME = "account";


    @SneakyThrows
    public DatabaseManager(){
        this.client = new MongoClient(new ServerAddress("localhost", 27017));
        this.database = client.getDatabase(DB_NAME);
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


    // TODO: 07/09/2023 System to add cooldowns to users

    // Metodo per aggiungere un cooldown
    public void addCooldown(String userId, int durationInSeconds, String type) {
        long expiryTimestamp = System.currentTimeMillis() + (durationInSeconds * 1000L);
        Document doc = new Document("userId", userId)
                .append("expiryTimestamp", expiryTimestamp)
                .append("type", type);
        cooldowns.insertOne(doc);
    }

    // Metodo per controllare se un utente è in cooldown
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

    // Metodo per ottenere il tempo rimanente del cooldown
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
