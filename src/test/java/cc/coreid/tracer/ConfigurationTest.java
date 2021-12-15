package cc.coreid.tracer;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void matchDefault() {
        Configuration t0 = new Configuration();
        assertEquals(t0.root, t0.find("java.lang.String"));
        assertEquals(t0.root.value, Configuration.Value.EXCL);
        Configuration t1 = new Configuration(Configuration.Value.INCL);
        assertEquals(t1.root, t1.find("java.lang.String"));
        assertEquals(t1.root.value, Configuration.Value.INCL);
    }

    @Test
    void insert() {
        Configuration t = new Configuration();
        Configuration.TrieElem e0 = t.insert("java", Configuration.Value.EXCL);
        assertEquals(e0.key, "java");
        assertTrue(e0.isValue());
        assertEquals(e0.value, Configuration.Value.EXCL);

        Configuration.TrieElem e2 = t.insert("java.lang.String", Configuration.Value.INCL);
        assertEquals(e2.key, "java.lang.String");
        assertTrue(e2.isValue());
        assertEquals(e2.value, Configuration.Value.INCL);

        Configuration.TrieElem e1 = e0.getChild("lang");
        assertEquals(e1.key, "java.lang");
        assertFalse(e1.isValue());
        assertEquals(e1.value, Configuration.Value.UNDEF);
        assertEquals(e2, e1.getChild("String"));
    }

    @Test
    void find() {
        Configuration t = new Configuration();
        Configuration.TrieElem e0;
        Configuration.TrieElem e1 = t.insert("java", Configuration.Value.EXCL);
        Configuration.TrieElem e2 = t.insert("java.lang.String", Configuration.Value.INCL);

        e0 = t.find("java");
        assertEquals(e1, e0);
        e0 = t.find("java.lang.String");
        assertEquals(e2, e0);
        assertTrue(e0.isValue());

        e0 = t.find("java.lang");
        assertEquals(e1, e0);
        assertTrue(e0.isValue());

        e0 = t.find("java.util");
        assertEquals(e1, e0);
        assertTrue(e0.isValue());
    }

    @Test
    void loadConfig() {
        Properties p = Configuration.loadConfigFromFile("src/test/resources/test00.cfg");
        Configuration cfg = Configuration.from(p);

        assertFalse(cfg.instrument("com"));         // not in config

        assertFalse(cfg.instrument("java"));        // exclude match
        assertFalse(cfg.instrument("java.lang"));   // exclude pre-fix

        assertTrue(cfg.instrument("org"));          // include match
        assertTrue(cfg.instrument("org.lang"));     // include pre-fix


        assertFalse(cfg.instrument("cc.coreid.trace"));                 // exclude
        assertTrue(cfg.instrument("cc.coreid.tracer.test"));            // include more specific
        assertFalse(cfg.instrument("cc.coreid.tracer.test.excl"));      // exclude more specific
        assertTrue(cfg.instrument("cc.coreid.tracer.test.incl"));       // include more specific pre-fix
    }

}