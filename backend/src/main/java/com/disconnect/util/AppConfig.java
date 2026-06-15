package com.disconnect.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class AppConfig {

    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties PROPERTIES = loadProperties();
    private static final Properties DOTENV = loadDotEnv();

    private AppConfig() {
    }

    public static String get(String key, String defaultValue) {
        String envName = toEnvName(key);

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return clean(envValue);
        }

        envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return clean(envValue);
        }

        String dotEnvValue = DOTENV.getProperty(envName);
        if (dotEnvValue != null && !dotEnvValue.isBlank()) {
            return clean(dotEnvValue);
        }

        String propertyValue = PROPERTIES.getProperty(key);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return clean(propertyValue);
        }

        String propertyEnvValue = PROPERTIES.getProperty(envName);
        if (propertyEnvValue != null && !propertyEnvValue.isBlank()) {
            return clean(propertyEnvValue);
        }

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("y")
                || normalized.equals("sim")
                || normalized.equals("s");
    }

    private static String toEnvName(String key) {
        return key.toUpperCase(Locale.ROOT)
                .replace('.', '_')
                .replace('-', '_');
    }

    private static String clean(String value) {
        String cleaned = value.trim();

        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        if (cleaned.length() >= 2 && cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        return cleaned.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar application.properties.", e);
        }

        return properties;
    }

    private static Properties loadDotEnv() {
        Properties properties = new Properties();

        File envFile = new File(".env");

        if (!envFile.exists()) {
            envFile = new File("backend/.env");
        }

        if (!envFile.exists()) {
            return properties;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf("=");

                if (equalsIndex <= 0) {
                    continue;
                }

                String name = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                properties.setProperty(name, clean(value));
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar arquivo .env.", e);
        }

        return properties;
    }
}