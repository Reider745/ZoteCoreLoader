package com.zhekasmirnov.innercore.api.constants;

import com.zhekasmirnov.apparatus.minecraft.enums.GameEnums;

public final class BlockRenderLayer {
    public static final int alpha = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "alpha"));
    public static final int alpha_seasons = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "alpha_seasons"));
    public static final int alpha_single_side = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "alpha_single_side"));
    public static final int blend = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "blend"));
    public static final int doubleside = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "double_side"));
    public static final int opaque = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "opaque"));
    public static final int far = opaque;
    public static final int opaque_seasons = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "opaque_seasons"));
    public static final int seasons_far = opaque_seasons;
    public static final int seasons_far_alpha = alpha_seasons;
    public static final int water = GameEnums.getInt(GameEnums.getSingleton().getEnum("block_render_layer", "ray_traced_water"));
}
