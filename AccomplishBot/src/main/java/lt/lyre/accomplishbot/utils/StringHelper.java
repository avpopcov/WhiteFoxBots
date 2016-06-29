package lt.lyre.accomplishbot.utils;

import java.util.ArrayList;

/**
 * Created by Dmitrij on 2016-06-29.
 */
public class StringHelper {
    public static boolean containsCommandPrefix(ArrayList<String> list, String text) {
        for (String item : list) {
            if (text.startsWith(item)) {
                return true;
            }
        }

        return false;
    }
}
