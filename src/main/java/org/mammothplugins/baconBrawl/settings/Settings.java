package org.mammothplugins.baconBrawl.settings;

import org.mineacademy.fo.settings.SimpleSettings;


public final class Settings extends SimpleSettings {

    @Override
    protected int getConfigVersion() {
        return 1;
    }

    public static class AutoMode {

        public static Boolean ENABLED;
        public static String RETURN_BACK_SERVER;

        private static void init() {
            setPathPrefix("Auto_Mode");

            ENABLED = getBoolean("Enabled");
            RETURN_BACK_SERVER = getString("Return_Back_Server");
        }
    }
    
    private static void init() {
        setPathPrefix(null);
    }
}
