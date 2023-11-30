package com.reider745.item;

import java.util.ArrayList;
import java.util.HashMap;

public class Repairs {
    private static HashMap<Integer, ArrayList<Integer>> repairs = new HashMap<>();

    public static void update(int id, ArrayList<Integer> ids){
        repairs.put(id, ids);
    }

    public static ArrayList<Integer> getRepairs(int id) {
        return repairs.get(id);
    }
}
