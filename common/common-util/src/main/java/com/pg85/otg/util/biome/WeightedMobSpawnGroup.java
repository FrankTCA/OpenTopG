package com.pg85.otg.util.biome;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.helpers.StringHelper;
import com.pg85.otg.util.minecraft.EntityNames;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds data for a bukkit nms.BiomeMeta class. The name does not
 * match but ours make more sense.
 */
public class WeightedMobSpawnGroup {
    private final String mob;
    private final int max;
    private final int weight;
    private final int min;

    public WeightedMobSpawnGroup(String mobName, int weight, int min, int max) {
        this.mob = mobName;
        this.weight = weight;
        this.min = min;
        this.max = max;
    }

    public WeightedMobSpawnGroup(EntityNames mobName, int weight, int min, int max) {
        this(mobName.getInternalName(), weight, min, max);
    }

    public String getInternalName() {
        return EntityNames.toInternalName(this.getMob());
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public static List<WeightedMobSpawnGroup> fromJson(String originalJson) throws InvalidConfigException {
        // Example: [{"mob": "Sheep", "weight": 12, "min": 4, "max": 4}]
        List<WeightedMobSpawnGroup> mobGroups = new ArrayList<WeightedMobSpawnGroup>();

        String json = originalJson.trim();
        if (json.length() <= 2) {
            // Empty Json
            return mobGroups;
        }
        // Remove the [..]
        json = removeFirstAndLastChar(json);

        // Every group is separated by a , but in the group the , is also
        // used.
        // So convert the ( to {, the ) to } and use an existing function to
        // get each group
        json = json.replace('{', '(');
        json = json.replace('}', ')');

        String[] groups = StringHelper.readCommaSeperatedString(json);

        for (String group : groups) {
            mobGroups.add(readSingleGroup(group));
        }

        return mobGroups;
    }

    private static WeightedMobSpawnGroup readSingleGroup(String json) throws InvalidConfigException {
        String group = removeFirstAndLastChar(json.trim());
        String[] groupParts = StringHelper.readCommaSeperatedString(group);
        String mobName = null;
        int weight = -1;
        int min = -1;
        int max = -1;

        // Read all options
        for (String option : groupParts) {
            String[] optionParts = option.split(":");
            // Mob name can use resourcelocation: "Mob" : "minecraft:creeper" or path only: "Mob" : "creeper"
            if (optionParts.length != 2 && optionParts.length != 3) {
                throw new InvalidConfigException("Invalid JSON structure near " + option);
            }
            String key = optionParts[0].trim();
            String value = optionParts[1].trim();

            if (key.equalsIgnoreCase("\"mob\"")) {
                // Mob name can use resourcelocation: "Mob" : "minecraft:creeper" or path only: "Mob" : "creeper"
                value = optionParts[1].trim() + (optionParts.length > 2 ? ":" + optionParts[2].trim() : "");
                // Remove the quotes from the mob name
                mobName = removeFirstAndLastChar(value);
            }
            if (key.equalsIgnoreCase("\"weight\"")) {
                weight = StringHelper.readInt(value, 0, 1000);
            }
            if (key.equalsIgnoreCase("\"min\"")) {
                min = StringHelper.readInt(value, 0, 1000);
            }
            if (key.equalsIgnoreCase("\"max\"")) {
                max = StringHelper.readInt(value, 0, 1000);
            }
        }

        // Check if data is complete and valid
        if (mobName == null || min == -1 || max == -1 || weight == -1) {
            throw new InvalidConfigException("Excepted mob, weight, min and max, but one or more were missing in mob group " + json);
        }
        if (min > max) {
            throw new InvalidConfigException("Minimum group size may not be larger that maximum group size for mob group " + json);
        }

        return new WeightedMobSpawnGroup(mobName, weight, min, max);
    }

    /**
     * Converts a list of mob groups to a single, JSON-formatted string.
     *
     * @param list The list to convert.
     * @return The mob groups.
     */
    public static String toJson(List<WeightedMobSpawnGroup> list) {
        StringBuilder json = new StringBuilder("[");
        for (WeightedMobSpawnGroup group : list) {
            group.toJson(json);
            json.append(", ");
        }
        // Remove ", " at end
        if (json.length() != 1) {
            json.deleteCharAt(json.length() - 1);
            json.deleteCharAt(json.length() - 1);
        }
        // Add closing bracket
        json.append(']');
        return json.toString();
    }

    /**
     * Converts this group to a JSON string.
     *
     * @param json The {@link StringBuilder} to append the JSON to.
     */
    private void toJson(StringBuilder json) {
        json.append("{\"mob\": \"" +
                getInternalName() +
                "\", \"weight\": " +
                getWeight() +
                ", \"min\": " +
                getMin() +
                ", \"max\": " +
                getMax() + "}");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toJson(builder);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + max;
        result = prime * result + min;
        result = prime * result + getMob().hashCode();
        result = prime * result + weight;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof WeightedMobSpawnGroup other)) {
            return false;
        }
        if (max != other.max || min != other.min || weight != other.weight) {
            return false;
        }
        return getMob().equals(other.getMob());
    }

    private static String removeFirstAndLastChar(String string) {
        return string.substring(1, string.length() - 1);
    }

    public String getMob() {
        return mob;
    }
}
