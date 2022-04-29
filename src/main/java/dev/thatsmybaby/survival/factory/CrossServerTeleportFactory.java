package dev.thatsmybaby.survival.factory;

import com.google.common.collect.Maps;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import dev.thatsmybaby.survival.object.CrossServerLocation;
import dev.thatsmybaby.survival.provider.MongoHandler;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Location;
import dev.thatsmybaby.survival.Survival;
import dev.thatsmybaby.survival.util.LocationUtil;

import java.util.*;

@SuppressWarnings("unchecked")
public final class CrossServerTeleportFactory {

    @Getter private final static CrossServerTeleportFactory instance = new CrossServerTeleportFactory();

    @Getter private final static Map<String, CrossServerLocation> warpCrossServerLocations = Maps.newConcurrentMap();

    private final MongoCollection<Document> document = MongoHandler.getInstance().getDocument();
    private final MongoCollection<Document> warpsDocument = MongoHandler.getInstance().getDatabase().getCollection("warps");

    public CrossServerLocation createPlayerCrossServerLocation(UUID uuidParsed, String homeName, Location location) {
        Document document = this.document.find(Filters.eq("uniqueId", uuidParsed.toString())).first();

        if (document == null) {
            return null;
        }

        Map<String, List<String>> map = new HashMap<>();
        if (document.containsKey("homes")) {
            map = (Map<String, List<String>>) document.get("homes");
        }

        map.put(homeName, new ArrayList<>() {{
            add(LocationUtil.serializeLocation(location));
            add(Survival.getServerName());
        }});

        this.document.findOneAndReplace(Filters.eq("uniqueId", uuidParsed.toString()), document.append("homes", map));

        return new CrossServerLocation(homeName, LocationUtil.serializeLocation(location), location, Survival.getServerName());
    }

    public Map<String, CrossServerLocation> loadPlayerCrossServerLocation(UUID uuidParsed, Document document) {
        if (document == null) {
            document = this.document.find(Filters.eq("uniqueId", uuidParsed.toString())).first();
        }

        if (document == null) {
            return null;
        }

        if (!document.containsKey("homes")) {
            return new HashMap<>();
        }

        Map<String, CrossServerLocation> map = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>)document.get("homes")).entrySet()) {
            List<String> list = entry.getValue();

            map.put(entry.getKey().toLowerCase(), new CrossServerLocation(entry.getKey(), list.get(0), LocationUtil.deserializeLocation(list.get(0)), list.get(1)));
        }

        return map;
    }

    public Map<String, CrossServerLocation> loadPlayerCrossServerLocation(String name) {
        Document document = this.document.find(Filters.eq("username", name)).first();

        if (document == null) {
            return null;
        }

        if (!document.containsKey("homes")) {
            return new HashMap<>();
        }

        Map<String, CrossServerLocation> map = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>)document.get("homes")).entrySet()) {
            List<String> list = entry.getValue();

            map.put(entry.getKey().toLowerCase(), new CrossServerLocation(entry.getKey(), list.get(0), LocationUtil.deserializeLocation(list.get(0)), list.get(1)));
        }

        return map;
    }

    public void removePlayerCrossServerLocation(UUID uuidParsed, String homeName) {
        Document document = this.document.find(Filters.eq("uuidParsed", uuidParsed.toString())).first();

        if (document == null) {
            return;
        }

        if (!document.containsKey("homes")) {
            return;
        }

        Map<String, List<String>> map = (Map<String, List<String>>) document.get("homes");

        map.remove(homeName);

        this.document.findOneAndReplace(Filters.eq("uuidParsed", uuidParsed.toString()), document.append("homes", map));
    }

    public void loadWarpCrossServerLocations() {
        warpCrossServerLocations.clear();

        for (Document document : this.warpsDocument.find()) {
            CrossServerLocation crossServerLocation = new CrossServerLocation(
                    document.getString("name"),
                    document.getString("location"),
                    LocationUtil.deserializeLocation(document.getString("location")),
                    document.getString("server")
            );

            warpCrossServerLocations.put(crossServerLocation.getName(), crossServerLocation);
        }
    }

    public void createWarpCrossServerLocation(String name, Location location) {
        Document document = new Document("name", name).append("location", LocationUtil.serializeLocation(location)).append("server", Survival.getServerName());

        if (this.warpsDocument.find(Filters.eq("name", name)).first() == null) {
            this.warpsDocument.insertOne(document);
        } else {
            this.warpsDocument.findOneAndReplace(Filters.eq("name", name), document);
        }
    }

    public CrossServerLocation getWarpCrossServerLocation(String name) {
        return warpCrossServerLocations.get(name.toLowerCase());
    }

    public void removeWarpCrossServerLocation(String name) {
        if (this.warpsDocument.find(Filters.eq("name", name)).first() == null) {
            return;
        }

        this.warpsDocument.deleteOne(Filters.eq("name", name));
    }
}