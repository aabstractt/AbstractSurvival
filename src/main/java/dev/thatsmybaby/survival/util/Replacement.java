package dev.thatsmybaby.survival.util;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

@Data
public class Replacement {

    private Map<Object, Object> replacements = Maps.newHashMap();
    private String message;

    public Replacement(String message) {
        this.message = message;
    }

    public Replacement add(Object current, Object replacement) {
        replacements.put(current, replacement);
        return this;
    }

    public String toString() {
        replacements.keySet().forEach(current -> this.message = this.message.replace(String.valueOf(current), String.valueOf(replacements.get(current))));
        return ColorUtil.translate(this.message);
    }

    public String toString(boolean ignored) {
        replacements.keySet().forEach(current -> this.message = this.message.replace(String.valueOf(current), String.valueOf(replacements.get(current))));
        return this.message;
    }
}
