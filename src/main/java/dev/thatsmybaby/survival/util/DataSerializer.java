package dev.thatsmybaby.survival.util;

import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

public class DataSerializer {

    /**
     * Returns a serialized array of {@link ItemStack}s
     *
     * @param inventoryContents The contents of the inventory
     * @return The serialized inventory contents
     */
    public static String serializeInventory(ItemStack[] inventoryContents) {
        // Return an empty string if there is no inventory item data to serialize
        if (inventoryContents.length == 0) {
            return "";
        }

        // Create an output stream that will be encoded into base 64
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try (BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream)) {
            // Define the length of the inventory array to serialize
            bukkitOutputStream.writeInt(inventoryContents.length);

            // Write each serialize each ItemStack to the output stream
            for (ItemStack inventoryItem : inventoryContents) {
                bukkitOutputStream.writeObject(serializeItemStack(inventoryItem));
            }

            // Return encoded data, using the encoder from SnakeYaml to get a ByteArray conversion
            return Base64Coder.encodeLines(byteOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize item stack data");
        }
    }

    /**
     * Returns an array of ItemStacks from serialized inventory data. Note: empty slots will be represented by {@code null}
     *
     * @param inventoryData The serialized {@link ItemStack[]} array
     * @return The inventory contents as an array of {@link ItemStack}s
     * @throws IOException            If the deserialization fails reading data from the InputStream
     * @throws ClassNotFoundException If the deserialization class cannot be found
     */
    @SneakyThrows
    public static ItemStack[] deserializeInventory(String inventoryData) {
        // Return empty array if there is no inventory data (set the player as having an empty inventory)
        if (inventoryData.isEmpty()) {
            return new ItemStack[0];
        }

        // Create a byte input stream to read the serialized data
        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(inventoryData))) {
            try (BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream)) {
                // Read the length of the Bukkit input stream and set the length of the array to this value
                ItemStack[] inventoryContents = new ItemStack[bukkitInputStream.readInt()];

                // Set the ItemStacks in the array from deserialized ItemStack data
                //int slotIndex = 0;

                for (int slotIndex = 0; slotIndex < inventoryContents.length; slotIndex++) {
                    inventoryContents[slotIndex] = deserializeItemStack(bukkitInputStream.readObject());
                }
//                for (ItemStack ignored : inventoryContents) {
//                    inventoryContents[slotIndex] = deserializeItemStack(bukkitInputStream.readObject());
//                    slotIndex++;
//                }

                // Return the finished, serialized inventory contents
                return inventoryContents;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ItemStack[0];
    }

    /**
     * Returns the serialized version of an {@link ItemStack} as a string to object Map
     *
     * @param item The {@link ItemStack} to serialize
     * @return The serialized {@link ItemStack}
     */
    private static Map<String, Object> serializeItemStack(ItemStack item) {
        return item != null ? item.serialize() : null;
    }

    /**
     * Returns the deserialized {@link ItemStack} from the Object read from the {@link BukkitObjectInputStream}
     *
     * @param serializedItemStack The serialized item stack; a String-Object map
     * @return The deserialized {@link ItemStack}
     */
    @SuppressWarnings("unchecked") // Ignore the "Unchecked cast" warning
    private static ItemStack deserializeItemStack(Object serializedItemStack) {
        return serializedItemStack != null ? ItemStack.deserialize((Map<String, Object>) serializedItemStack) : null;
    }

    /**
     * Returns a serialized array of {@link PotionEffect}s
     *
     * @param potionEffects The potion effect array
     * @return The serialized potion effects
     */
    public static String serializePotionEffects(PotionEffect[] potionEffects) {
        // Return an empty string if there are no effects to serialize
        if (potionEffects.length == 0) {
            return "";
        }

        // Create an output stream that will be encoded into base 64
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try (BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(byteOutputStream)) {
            // Define the length of the potion effect array to serialize
            bukkitOutputStream.writeInt(potionEffects.length);

            // Write each serialize each PotionEffect to the output stream
            for (PotionEffect potionEffect : potionEffects) {
                bukkitOutputStream.writeObject(serializePotionEffect(potionEffect));
            }

            // Return encoded data, using the encoder from SnakeYaml to get a ByteArray conversion
            return Base64Coder.encodeLines(byteOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize potion effect data");
        }
    }

    /**
     * Returns an array of ItemStacks from serialized potion effect data
     *
     * @param potionEffectData The serialized {@link PotionEffect[]} array
     * @return The {@link PotionEffect}s
     * @throws IOException            If the deserialization fails reading data from the InputStream
     * @throws ClassNotFoundException If the deserialization class cannot be found
     */
    @SneakyThrows
    public static PotionEffect[] deserializePotionEffects(String potionEffectData) {
        // Return empty array if there is no potion effect data (don't apply any effects to the player)
        if (potionEffectData.isEmpty()) {
            return new PotionEffect[0];
        }

        // Create a byte input stream to read the serialized data
        try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64Coder.decodeLines(potionEffectData))) {
            try (BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(byteInputStream)) {
                // Read the length of the Bukkit input stream and set the length of the array to this value
                PotionEffect[] potionEffects = new PotionEffect[bukkitInputStream.readInt()];

                // Set the potion effects in the array from deserialized PotionEffect data
                int potionIndex = 0;
                for (PotionEffect ignored : potionEffects) {
                    potionEffects[potionIndex] = deserializePotionEffect(bukkitInputStream.readObject());
                    potionIndex++;
                }

                // Return the finished, serialized potion effect array
                return potionEffects;
            }
        }
    }

    /**
     * Returns the serialized version of an {@link ItemStack} as a string to object Map
     *
     * @param potionEffect The {@link ItemStack} to serialize
     * @return The serialized {@link ItemStack}
     */
    private static Map<String, Object> serializePotionEffect(PotionEffect potionEffect) {
        return potionEffect != null ? potionEffect.serialize() : null;
    }

    /**
     * Returns the deserialized {@link PotionEffect} from the Object read from the {@link BukkitObjectInputStream}
     *
     * @param serializedPotionEffect The serialized potion effect; a String-Object map
     * @return The deserialized {@link PotionEffect}
     */
    @SuppressWarnings("unchecked") // Ignore the "Unchecked cast" warning
    private static PotionEffect deserializePotionEffect(Object serializedPotionEffect) {
        return serializedPotionEffect != null ? new PotionEffect((Map<String, Object>) serializedPotionEffect) : null;
    }


    /**
     * A record used to store data for a player's location
     */
    public record PlayerLocation(double x, double y, double z, float yaw, float pitch,
                                 String worldName, World.Environment environment) implements Serializable {
    }

    /**
     * A record used to store data for advancement synchronisation
     *
     * @deprecated Old format - Use {@link AdvancementRecordDate} instead
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed") // Suppress deprecation warnings here (still used for backwards compatibility)
    public record AdvancementRecord(String advancementKey,
                                    ArrayList<String> awardedAdvancementCriteria) implements Serializable {
    }

    /**
     * A record used to store data for native advancement synchronisation, tracking advancement date progress
     */
    public record AdvancementRecordDate(String key, Map<String, Date> criteriaMap) implements Serializable {
        AdvancementRecordDate(String key, List<String> criteriaList) {
            this(key, new HashMap<>() {{
                criteriaList.forEach(s -> put(s, Date.from(Instant.EPOCH)));
            }});
        }
    }

    /**
     * A record used to store data for a player's statistics
     */
    public record StatisticData(HashMap<Statistic, Integer> untypedStatisticValues,
                                HashMap<Statistic, HashMap<Material, Integer>> blockStatisticValues,
                                HashMap<Statistic, HashMap<Material, Integer>> itemStatisticValues,
                                HashMap<Statistic, HashMap<EntityType, Integer>> entityStatisticValues) implements Serializable {
    }


}
