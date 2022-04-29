package dev.thatsmybaby.survival.provider;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.factory.CrossServerTeleportFactory;
import dev.thatsmybaby.survival.object.PlayerSync;
import dev.thatsmybaby.survival.util.DataSerializer;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class MongoHandler {

    @Getter private final static MongoHandler instance = new MongoHandler();

    private final Survival plugin = Survival.getInstance();

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> clans, document, statistics;

    private final String host = plugin.getSettings().getString("Mongo.Host");
    private final int port = plugin.getSettings().getInt("Mongo.Port");
    private final String databaseName = plugin.getSettings().getString("Mongo.Database");
    private final String username = this.plugin.getSettings().getString("Mongo.Auth.Username");
    private final String password = plugin.getSettings().getString("Mongo.Auth.Password");
    private final String authDatabase = plugin.getSettings().getString("Mongo.Auth.Database");

    public boolean connect() {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.OFF);
        try {
            Bukkit.getLogger().info("Estableciendo conexión con mongo...");
            if (this.plugin.getSettings().getBoolean("Mongo.Auth.Enabled")) {
                ServerAddress serverAddress = new ServerAddress(host, port);

                MongoCredential credential = MongoCredential.createCredential(username, authDatabase, password.toCharArray());

                client = new MongoClient(serverAddress, credential, MongoClientOptions.builder().build());
            } else {
                client = new MongoClient(host, port);
            }

            this.database = client.getDatabase(databaseName);
            this.document = this.database.getCollection("player_data");
            this.statistics = this.database.getCollection("player_statistic");
            this.clans = this.database.getCollection("clans");

            Bukkit.getLogger().info("¡Se conectó mongo correctamente!");
            return true;
        } catch (Exception ex) {
            Bukkit.getLogger().info("Error al iniciar MongoDB, revisa Settings.yml.");
            return false;
        }
    }

    public void loadPlayerSync(UUID uuidParsed, String name) {
        Document document = this.document.find(Filters.eq("uniqueId", uuidParsed.toString())).first();

        if (document == null) {
            this.document.insertOne(new Document("uniqueId", uuidParsed.toString()).append("username", name));
        } else {
            this.document.findOneAndReplace(Filters.eq("uniqueId", uuidParsed.toString()), document.append("username", name));
        }

        PlayerSync.add(uuidParsed, new PlayerSync(
                document != null ? CrossServerTeleportFactory.getInstance().loadPlayerCrossServerLocation(uuidParsed, document) : new HashMap<>(),
                document != null && document.getBoolean("auto_pickup", true),
                document != null && document.getBoolean("can_see_chat", true),
                document != null && document.getBoolean("can_see_announces", true),
                document != null && document.getBoolean("can_show_welcome", true),
                document != null && document.getBoolean("accepting_tpa_requests", true),
                false
        ));
    }

    public PlayerSync loadPlayerSync(UUID uuidParsed) {
        PlayerSync playerSync = PlayerSync.of(uuidParsed);

        if (playerSync != null) {
            return playerSync;
        }

        Document document = this.document.find(Filters.eq("uniqueId", uuidParsed.toString())).first();

        if (document == null) {
            return null;
        }

        return new PlayerSync(
                CrossServerTeleportFactory.getInstance().loadPlayerCrossServerLocation(uuidParsed, document),
                document.getBoolean("auto_pickup", true),
                document.getBoolean("can_see_chat", true),
                document.getBoolean("can_see_announces", true),
                document.getBoolean("can_show_welcome", true),
                document.getBoolean("accepting_tpa_requests", true),
                false
        );
    }

    public void updatePlayerSync(UUID uuidParsed, PlayerSync playerSync) {
        Document document = this.document.find(Filters.eq("uniqueId", uuidParsed.toString())).first();

        if (document == null) {
            return;
        }

        document.put("auto_pickup", playerSync.isAutoPickup());
        document.put("can_see_chat", playerSync.canSeeChat());
        document.put("can_see_announces", playerSync.canSeeAnnounces());
        document.put("can_show_welcome", playerSync.canShowWelcome());
        document.put("accepting_tpa_requests", playerSync.isAcceptingTpaRequests());

        this.document.findOneAndReplace(Filters.eq("uniqueId", uuidParsed.toString()), document);
    }

    public void updatePlayerStatistics(Player player) {
        Document document = new Document();

        document.put("inventory", DataSerializer.serializeInventory(player.getInventory().getContents()));
        document.put("enderChest", DataSerializer.serializeInventory(player.getEnderChest().getContents()));
        document.put("totalExperience", player.getTotalExperience());
        document.put("expLevel", player.getLevel());
        document.put("expProgress", player.getExp());

        this.statistics.replaceOne(Filters.eq("uniqueId", player.getUniqueId().toString()), new Document("uniqueId", player.getUniqueId()).append("name", player.getName()).append("statistics", document), new UpdateOptions().upsert(true));
    }

    public void loadPlayerStatistics(Player player, Document document) {
        if (document == null) {
            document = MongoHandler.getInstance().getStatistics().find(Filters.eq("uniqueId", player.getUniqueId().toString())).first();
        }

        if (document == null) {
            return;
        }

        document = (Document) document.get("statistic");

        player.getInventory().setContents(DataSerializer.deserializeInventory(document.getString("inventory")));
        player.getEnderChest().setContents(DataSerializer.deserializeInventory(document.getString("enderChest")));

        player.setTotalExperience(document.getInteger("totalExperience"));
        player.setLevel(document.getInteger("expLevel"));
        player.setExp(document.getDouble("expProgress").floatValue());
    }
}