package lt.lyre.accomplishbot.localization;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by Dmitrij on 2016-07-09.
 */
public class LocalizationManager {

    private Map<Languages, ResourceBundle> resources = new Hashtable();

    public LocalizationManager() {
        resources.put(Languages.ENGLISH, ResourceBundle.getBundle("accomplish_en", Locale.ENGLISH));
        resources.put(Languages.LITHUANIAN, ResourceBundle.getBundle("accomplish_lt", new Locale("LT")));
        resources.put(Languages.RUSSIAN, ResourceBundle.getBundle("accomplish_ru", new Locale("RU")));
    }

    public String getResource(String key, Languages language) {
        ResourceBundle bundle = resources.get(language);

        if (bundle == null) {
            return null;
        }

        return bundle.getString(key);
    }
}
