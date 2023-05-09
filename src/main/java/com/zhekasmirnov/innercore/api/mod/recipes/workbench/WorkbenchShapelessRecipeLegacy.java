package com.zhekasmirnov.innercore.api.mod.recipes.workbench;

import org.json.JSONObject;

/**
 * Created by zheka on 10.09.2017.
 */

public class WorkbenchShapelessRecipeLegacy extends WorkbenchRecipeLegacy {

    public WorkbenchShapelessRecipeLegacy(JSONObject json) {
        super(json);
        workbenchRecipe = new WorkbenchShapelessRecipe(id, count, data, null);
    }
}
