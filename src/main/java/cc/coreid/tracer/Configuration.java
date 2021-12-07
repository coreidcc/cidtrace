package cc.coreid.tracer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

public class Configuration {
    private static final Logger log = Logger.getLogger(Configuration.class.getName());

    static Properties parseCfgStr(String args) {
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

}
