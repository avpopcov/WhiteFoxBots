package lt.lyre.accomplishbot.localization;

import java.io.UnsupportedEncodingException;
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

        /*
        Warning! Lame workaround ahead. Leaving it here until I write manual resource stream reader.
        Native Java resource crap reader scans bytes in ISO-8859-1, not mighty UTF-8.
        Workaround simply converts encodings each time we get a resource.
        The rest is self-explanatory. Peace out.
         */
        String decoded = null;
        byte[] lameBytes = null;
        try {
            String potentialText = bundle.getString(key);
            if (potentialText != null) {
                lameBytes = potentialText.getBytes("ISO-8859-1");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            decoded = new String(lameBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decoded;
    }
}
