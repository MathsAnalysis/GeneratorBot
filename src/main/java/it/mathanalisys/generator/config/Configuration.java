package it.mathanalisys.generator.config;

import it.mathanalisys.generator.config.util.PropertiesFile;

import java.util.HashMap;
import java.util.Map;

public class Configuration extends PropertiesFile {

    public Configuration(String name) {
        super(name);
    }

    @Override
    public Map<String, Object> getKeys() {
        Map<String, Object> keys = new HashMap<>();

        keys.put("MONGO-HOST", "127.0.0.1");
        keys.put("MONGO-PORT", "27017");
        keys.put("MONGO-DATABASE", "generator");
        keys.put("MONGO-AUTHENTICATION-ENABLED", false);
        keys.put("MONGO-AUTHENTICATION-PASSWORD", "bumblebee");
        keys.put("MONGO-AUTHENTICATION-USERNAME", "admin");
        keys.put("MONGO-AUTHENTICATION-DATABASE", "admin");

        return keys;
    }
}