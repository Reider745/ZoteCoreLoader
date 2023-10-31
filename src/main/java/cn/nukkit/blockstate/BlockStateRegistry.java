package cn.nukkit.blockstate;

import cn.nukkit.block.Block;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class BlockStateRegistry {
    private static final HashMap<String, BlockState> statesForIdAndData = new HashMap<>();

    public static void registerState(JSONObject jsonObject){
        int stateId = jsonObject.getInt("stateId");
        String nameId = jsonObject.getString("nameId");
        JSONObject states = jsonObject.getJSONObject("states");

        final HashMap<String, Integer> states_result = new HashMap<>();

        Iterator<String> it = states.keys();
        while (it.hasNext()) {
            String key = it.next();
            states_result.put(key, states.getInt(key));
        }

        registerState(jsonObject.getInt("oldId"), jsonObject.getInt("data"), states_result);
    }

    public static void registerState(final int id, final int data, final HashMap<String, Integer> states){
        BlockState state = new BlockState(id, data, states);
        statesForIdAndData.put(id+":"+data, state);
    }

    public static BlockState getForBlock(final Block block){
        return statesForIdAndData.get(block.getId()+":"+block.getDamage());
    }
}
