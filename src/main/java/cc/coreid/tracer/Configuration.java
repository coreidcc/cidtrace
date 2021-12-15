package cc.coreid.tracer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class.getName());

    TrieElem root;

    public static Properties parseCfgStr(String args) {
        try {
            final Properties res = new Properties();
            if (args != null) {
                res.load(new StringReader(args.replaceAll(",", "\n")));
            } else {
                log.config("Configuration.parseCfgStr: no configuration received");
            }
            return res;
        } catch (IOException ex) {
            throw new RuntimeException("Configuration.parseCfgStr: failed load properties from arguments", ex);
        }
    }

    public static Properties loadConfigFromFile(String fileName) {
        try {
            final Properties res = new Properties();
            res.load(new FileInputStream(fileName));
            return res;
        } catch (IOException ex) {
            String errMsg = String.format("%s: failed load properties file '%s'", Configuration.class,fileName);
            throw new RuntimeException(errMsg, ex);
        }
    }

    public static Configuration from(Properties properties) {
        Configuration cfg = new Configuration();

        String incl = (String) properties.get("include");
        for(String i : incl.split(",")) {
            i = i.trim();
            if(!i.isEmpty() && !",".equals(i)) {
                cfg.insert(i, Value.INCL);
            }
        }
        String excl = (String) properties.get("exclude");
        for(String i : excl.split(",")) {
            i = i.trim();
            if(!i.isEmpty() && !",".equals(i)) {
                cfg.insert(i, Value.EXCL);
            }
        }

        return cfg;
    }

    public enum Value { UNDEF, EXCL, INCL }

    static class TrieElem {
        HashMap<String, TrieElem> children = new HashMap<>();
        Value value;
        String key;

        public TrieElem(String key, Value value) {
            this.key = key;
            this.value = value;
        }

        public TrieElem(TrieElem parent, String key ) {
            this.key = "".equals(parent.key) ? key : parent.key+"."+key;
            this.value = Value.UNDEF;
        }

        boolean isValue() {
            return value != Value.UNDEF;
        }

        boolean containsChild(String key) {
            return children.containsKey(key);
        }

        TrieElem getChild(String key) {
            return children.get(key);
        }

        TrieElem insertChild(String key, TrieElem child) {
            children.put(key, child);
            return child;
        }
    }

    Configuration() {
        root = new TrieElem("", Value.EXCL);
    }
    Configuration(Value value) {
        root = new TrieElem("", value);
    }

    public TrieElem insert(String key, Value value) {
        TrieElem current = root;
        for (String p : searchElem(key)) {
            if (current.containsChild(p)) {
                current = current.getChild(p);
            } else {
                current = current.insertChild(p, new TrieElem(current, p));
            }
        }
        current.value = value;
        return current;
    }

    public TrieElem find(String key) {
        TrieElem current = root;
        TrieElem match = root;
        for (String p : searchElem(key)) {
            if (current.containsChild(p)) {
                current = current.getChild(p);
                if(current.isValue()) {
                    match = current;
                }
            } else {
                return match;
            }
        }
        return match;
    }

    public boolean instrument(String className) {
        TrieElem e = find(className);
        return e.value == Value.INCL;
    }

    // String.split has odd behaviour -> wrap it just in case we need to fix it
    String[] searchElem(String key) {
        return key.split("\\.");
    }


}
