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

package pl.edu.icm.board.urizen.healthcare;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;

import java.io.IOException;
import java.util.function.Consumer;

public class HealthcareGeodecoder {

    private final HealthcareLoader healthcareLoader;
    private final AddressPointManager addressPointManager;

    @WithFactory
    public HealthcareGeodecoder(HealthcareLoader healthcareLoader, AddressPointManager addressPointManager) {
        this.healthcareLoader = healthcareLoader;
        this.addressPointManager = addressPointManager;
    }

    public void foreach(Consumer<GeocodedPoi<HealthcareFromCsv>> consumer) throws IOException {
        healthcareLoader.load();
        healthcareLoader.forEach(primaryCare -> {
            var results = addressPointManager.lookup(
                    primaryCare.getCommuneTeryt(),
                    primaryCare.getPostalCode(),
                    primaryCare.getLocality(),
                    primaryCare.getStreet(),
                    primaryCare.getStreetNumber()
            );
            consumer.accept(new GeocodedPoi<>(results, primaryCare));
        });
    }
}
