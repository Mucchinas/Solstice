package io.summertime.core;

import java.io.InputStream;
import java.util.Properties;

public class Observatory {
    private static final Properties properties = new Properties();

    public static void load() {
        try (InputStream is = Observatory.class.getClassLoader().getResourceAsStream("summertime.properties")) {
            if (is != null) {
                properties.load(is);
                System.out.println("Configurazione 'summertime.properties' caricata.");
            }
        } catch (Exception e) {
            System.out.println("Nessun file di configurazione trovato, uso i default.");
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
    }
}