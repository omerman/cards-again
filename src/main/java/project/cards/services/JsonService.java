package project.cards.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by omerpr on 14/02/2015.
 */
public class JsonService {
	public static <K, V> Map<String, Object> toObjectMap(Map<K, V> map) {
		Map<String, Object> retVal = new HashMap<>();
		for(K key : map.keySet()) {
			retVal.put(String.valueOf(key), map.get(key));
		}
		return retVal;
	}
}
