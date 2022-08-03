package pl.edu.icm.board.urizen.replicants;

import net.snowyhollows.bento.annotation.WithFactory;

import java.io.FileNotFoundException;

public class ReplicantsUrizen {
    private final ClergyHouseUrizen clergyHouseUrizen;
    private final NursingHomeUrizen nursingHomeUrizen;
    private final DormUrizen dormUrizen;
    private final MonasteryUrizen monasteryUrizen;
    private final PrisonUrizen prisonUrizen;
    private final BarracksUrizen barracksUrizen;
    private final ImmigrantsSpotUrizen immigrantsSpotUrizen;
    private final HomelessSpotUrizen homelessSpotUrizen;

    @WithFactory
    public ReplicantsUrizen(ClergyHouseUrizen clergyHouseUrizen,
                            NursingHomeUrizen nursingHomeUrizen,
                            DormUrizen dormUrizen,
                            MonasteryUrizen monasteryUrizen,
                            PrisonUrizen prisonUrizen,
                            BarracksUrizen barracksUrizen,
                            HomelessSpotUrizen homelessSpotUrizen,
                            ImmigrantsSpotUrizen immigrantsSpotUrizen) {
        this.clergyHouseUrizen = clergyHouseUrizen;
        this.nursingHomeUrizen = nursingHomeUrizen;
        this.dormUrizen = dormUrizen;
        this.monasteryUrizen = monasteryUrizen;
        this.prisonUrizen = prisonUrizen;
        this.barracksUrizen = barracksUrizen;
        this.immigrantsSpotUrizen = immigrantsSpotUrizen;
        this.homelessSpotUrizen = homelessSpotUrizen;
    }

    public void createReplicants() {
        try {
            clergyHouseUrizen.fabricate();
            homelessSpotUrizen.fabricate();
            nursingHomeUrizen.fabricate();
            dormUrizen.fabricate();
            monasteryUrizen.fabricate();
            prisonUrizen.fabricate();
            barracksUrizen.fabricate();
            immigrantsSpotUrizen.fabricate();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
