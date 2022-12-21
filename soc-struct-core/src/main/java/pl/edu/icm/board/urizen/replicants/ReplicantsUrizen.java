/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

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
