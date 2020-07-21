import java.io.*;
import java.util.Properties;

public class ConfigUtil {
    private static final String CONFIG_FILE = "ApkCrack.config.properties";

    public static ApkConfig loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            return createConfig(file);
        } else {
            return null;
        }
    }

    public static void saveConfig(ApkConfig config) {
        Properties p = new Properties();
        p.setProperty("certFile", config.certFile);
        p.setProperty("storeFile", config.storeFile);
        p.setProperty("storePassword", config.storePassword);
        p.setProperty("keyAlias", config.keyAlias);
        p.setProperty("keyPassword", config.keyPassword);
        try {
            FileOutputStream fos = new FileOutputStream(new File(CONFIG_FILE));
            p.store(fos, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ApkConfig createConfig(File file) {
        Properties p = new Properties();
        ApkConfig config = new ApkConfig();
        try {
            p.load(new FileInputStream(file));
            config.certFile = p.getProperty("certFile");
            config.storeFile = p.getProperty("storeFile");
            config.storePassword = p.getProperty("storePassword");
            config.keyAlias = p.getProperty("keyAlias");
            config.keyPassword = p.getProperty("keyPassword");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
