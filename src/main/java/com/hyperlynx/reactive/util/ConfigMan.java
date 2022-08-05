package com.hyperlynx.reactive.util;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigMan {
    public static class Common {
        public ForgeConfigSpec.IntValue crucibleTickDelay;
        public ForgeConfigSpec.IntValue crucibleRange;

        Common(ForgeConfigSpec.Builder builder){
            builder.comment("Performance Settings")
                    .push("config");
            crucibleTickDelay = builder.comment("The crucible performs its calculations once every X game ticks. Lower numbers are more responsive, but laggier. [Default: 30]")
                    .defineInRange("crucibleTickDelay", 30, 1, 900);
            crucibleRange = builder.comment("The crucible may check an area this many blocks in radius each full tick. This may have a performance impact on world load and when certain blocks are removed within the radius. [Default: 8]")
                    .defineInRange("crucibleTickDelay", 8, 2, 32);
            builder.pop();
        }
    }

    public static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

}
