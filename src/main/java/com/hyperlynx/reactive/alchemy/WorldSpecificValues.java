package com.hyperlynx.reactive.alchemy;

import com.hyperlynx.reactive.util.PrimedWSV;

/*
This class is a holder for alias strings for WSV generation that might need to be repeated.
 */
public class WorldSpecificValues {
    public final static PrimedWSV GOLEM_CAUSE = new PrimedWSV("golem_cause", 1, 2);
    // Determines the reason that golems animate when a carved pumpkin in placed. The options are...
    // 1: SOUL is trapped by the pumpkin.
    // 2: CURSE manifests near the pumpkin.

    public final static PrimedWSV ELECTRIC_EFFECT = new PrimedWSV("electric_effect", 1, 3);
    // Determines whether electricity (1) causes annihilation reactions, (2) causes synthesis reactions, or (3) causes effect reactions.
}
