
package it.mathanalisys.generator.config.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public abstract class PropertiesFile {

    private File file;
    private Map<String, Object> values = new HashMap<>();

    public PropertiesFile(String name) {
        this.file = new File(name + ".properties");

        if (!this.file.exists()) {
            try {
                FileOutputStream output = new FileOutputStream(file);

                this.getKeys().forEach((key, value) -> {
                    try {
                        output.write((key + "=" + value + "\n").getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Properties properties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(this.file);
            properties.load(input);

            this.getKeys().forEach((key, value) -> {
                values.put(key, properties.getOrDefault(key, "Can not found key " + key));
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public abstract Map<String, Object> getKeys();

    public String getString(String key) {
        if (this.values.containsKey(key)) {
            return String.valueOf(this.values.get(key));
        }
        return "String at path '" + key + "' can not be found.";
    }

    public int getInt(String key) {
        if (this.values.containsKey(key)) {
            try {
                return Integer.parseInt(String.valueOf(this.values.get(key)));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public long getLong(String key) {
        if (this.values.containsKey(key)) {
            try {
                return Long.parseLong(String.valueOf(this.values.get(key)));
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }

    public boolean getBoolean(String key) {
        if (this.values.containsKey(key)) {
            try {
                return Boolean.parseBoolean(String.valueOf(this.values.get(key)));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}