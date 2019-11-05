package com.rotunomp.discordBot.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Properties {
    public static String get(String property) {
        File file = new File("config.properties");
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            java.util.Properties properties = new java.util.Properties();
            properties.load(reader);

            return properties.getProperty(property);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }
}
