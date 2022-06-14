package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento2.annotation.WithFactory;

public class ReplicantsCounter {
    private final int replicantsCount;

    @WithFactory
    public ReplicantsCounter(
            int clergyHouseReplicantsCount,
            int nursingHomeReplicantsCount,
            int prisonReplicantsCount,
            int dormReplicantsCount,
            int monasteryReplicantsCount,
            int barracksReplicantsCount,
            int homelessSpotReplicantsCount,
            int immigrantsSpotReplicantsCount) {
        this.replicantsCount = clergyHouseReplicantsCount
                + nursingHomeReplicantsCount
                + prisonReplicantsCount
                + dormReplicantsCount
                + monasteryReplicantsCount
                + barracksReplicantsCount
                + homelessSpotReplicantsCount
                + immigrantsSpotReplicantsCount;
    }

    public int getReplicantsCount() {
        return replicantsCount;
    }
}
