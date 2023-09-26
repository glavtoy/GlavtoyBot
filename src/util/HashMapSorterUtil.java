package util;

import user.User;

import java.util.*;

public class HashMapSorterUtil {

    public static HashMap<User, Integer> sort(HashMap<User, Integer> hashMap) {

        List<Map.Entry<User, Integer>> list = new ArrayList<>(hashMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<User, Integer>>() {
            @Override
            public int compare (Map.Entry < User, Integer > entry1, Map.Entry < User, Integer > entry2){
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });

        Collections.reverse(list);

        LinkedHashMap<User, Integer> sortedHashMap = new LinkedHashMap<>();
        for(Map.Entry<User, Integer> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
