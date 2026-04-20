package io.summertime.core.props;

import io.summertime.core.common.Logger;

import java.io.InputStream;
import java.util.Properties;

public class PropertyResolver {

    private final Properties properties = new Properties();

    public PropertyResolver() {
        this("summertime.properties");
    }

    public PropertyResolver(String propertiesFile) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (is != null) {
                properties.load(is);
                Logger.info("Configuration '" + propertiesFile + "' loaded.");
            } else {
                Logger.info("No configuration file found at '" + propertiesFile + "', using default values.");
            }
        } catch (Exception e) {
            Logger.warn("Could not load configuration file '" + propertiesFile + "'.");
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Integer getIntProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Logger.warn("Invalid integer value for key '" + key + "': " + value);
            return null;
        }
    }

    public int getIntProperty(String key, int defaultValue) {
        Integer value = getIntProperty(key);
        return value != null ? value : defaultValue;
    }
}
