/*
 * server configuration is in properties file
 */
 
import java.io.*;
import java.net.*;
import java.util.*;


class ConfigDefaults {

    public ConfigDefaults(Config c) {
        assignTo(c);
    }

    public void assignTo(Config c) {
        c.root = new File(System.getProperty("user.dir"));
        c.timeout = 5000;
        c.workers = 5;
        c.log = System.out;
    }

    public void checkAndComplete(Config c) {
        if (c.root == null) {
            c.root = new File(System.getProperty("user.dir"));
        }
        if (c.timeout <= 1000) {
            c.timeout = 5000;
        }
        if (c.workers < 25) {
            c.workers = 5;
        }
        if (c.log == null) {
            c.log = System.out;
        }
    }
}


class Config {
    File root = null;
    int timeout = 0;
    int workers = 0;
    OutputStream log = null;
    Logger logger = null;
    ConfigDefaults defaults = null;

    public Config(ConfigDefaults defaults, Logger logger) {
        this.logger = logger;
        this.defaults = defaults;
    }

    public void load() throws IOException {
        File storedProperties = new File(System.getProperty("java.home")
                    + File.separator
                    + "lib" + File.separator + "www-server.properties");
        if (!storedProperties.exists()) {
            defaults.assignTo(this);
            return;
        }

        Properties properties = new Properties();
        InputStream is = new BufferedInputStream(
                            new FileInputStream(storedProperties));
        properties.load(is);
        is.close();

        assign(properties);
        defaults.checkAndComplete(this);
    }

    protected void assign(Properties properties) {
        String rootDirName = properties.getProperty("root");
        if (rootDirName != null) {
            root = new File(rootDirName);
            if (!root.exists()) {
                throw new Error(rootDirName + " doesn't exist as server root");
            }
        }
        String property = "";
        property = properties.getProperty("timeout");
        if (property != null) {
            timeout = Integer.parseInt(property);
        }
        property = properties.getProperty("workers");
        if (property != null) {
            workers = Integer.parseInt(property);
        }
        property = properties.getProperty("log");
        if (property != null) {
            String logName = property;
            try {
                log = new PrintStream(new BufferedOutputStream(
                                  new FileOutputStream(logName)));
            } catch (FileNotFoundException e) {
            }
        }
    }

    public void list() {
        logger.p("root=" + root);
        logger.p("timeout=" + timeout);
        logger.p("workers=" + workers);
    }
}

