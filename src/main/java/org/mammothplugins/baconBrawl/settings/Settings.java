package org.mammothplugins.baconBrawl.settings;

import org.mineacademy.fo.settings.Lang;
import org.mineacademy.fo.settings.SimpleSettings;
import org.mineacademy.fo.settings.YamlStaticConfig;

/**
 * A sample settings class, utilizing {@link YamlStaticConfig} with prebuilt settings.yml handler
 * with a bunch of preconfigured keys, see resources/settings.yml
 * <p>
 * Foundation detects if you have "settings.yml" placed in your jar (in src/main/resources in source)
 * and will load this class automatically. The same goes for the {@link Lang} class which is
 * automatically loaded when we detect the presence of at least one localization/messages_X.yml
 * file in your jar.
 */
@SuppressWarnings("unused")
public final class Settings extends SimpleSettings {

    /**
     * @see org.mineacademy.fo.settings.SimpleSettings#getConfigVersion()
     */
    @Override
    protected int getConfigVersion() {
        return 1;
    }

    /**
     * Place the sections where user can create new "key: value" pairs
     * here so that they are not removed while adding comments.
     *
     * Example use in ChatControl: user can add new channels in "Channels.List"
     * section so we place "Channels.List" here.
     *
     * @return the ignored sections
     */
	/*@Override
	protected List<String> getUncommentedSections() {
		return Arrays.asList(
				"Example.Uncommented_Section");
	}*/

    /**
     * A sample settings section
     */
    public static class AutoMode {

        public static Boolean ENABLED;
        public static String RETURN_BACK_SERVER;

        /*
         * Automatically called method when we load settings.yml to load values in this subclass
         */
        private static void init() {
            setPathPrefix("Auto_Mode");

            ENABLED = getBoolean("Enabled");
            RETURN_BACK_SERVER = getString("Return_Back_Server");
        }
    }

    /*
     * Automatically called method when we load settings.yml to load values in this class
     *
     * See above for usage.
     */
    private static void init() {
        setPathPrefix(null);
    }
}
