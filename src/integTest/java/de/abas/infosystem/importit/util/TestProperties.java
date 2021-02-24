package de.abas.infosystem.importit.util;

import de.abas.infosystem.importit.ImportitException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TestProperties {

    private TestProperties() {
    }

    public static final String EDP_HOST = "EDP_HOST";
    public static final String EDP_CLIENT = "EDP_CLIENT";
    public static final String EDP_PORT = "EDP_PORT";
    public static final String EDP_PASSWORD = "EDP_PASSWORD";


    public static Map<String,String> loadProperties() throws ImportitException {
        Map<String , String> properties= new HashMap<>();

        final Properties pr = new Properties();
        final File configFile = new File("gradle.properties");
        try (Reader fileReader =new FileReader(configFile)){
            pr.load(fileReader);
            properties.put(EDP_HOST, pr.getProperty(EDP_HOST));
            properties.put(EDP_CLIENT, pr.getProperty(EDP_CLIENT));
            properties.put(EDP_PORT, pr.getProperty(EDP_PORT));
            properties.put(EDP_PASSWORD, pr.getProperty(EDP_PASSWORD));
           return properties;

        } catch (final FileNotFoundException e) {
            throw new ImportitException("Could not find configuration file " + configFile.getAbsolutePath());
        } catch (final IOException e) {
            throw new ImportitException("Could not load configuration file " + configFile.getAbsolutePath());
        }
    }
}
