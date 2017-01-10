package lt.lyre.accomplishbot.utils;

import java.util.List;

/**
 * Created by Dmitrij on 2016-06-29.
 */
public class CollectionHelper {
    public static <K> K getGenericList(List<K> result) {
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return result.stream().findAny().get();
        }
    }

}
