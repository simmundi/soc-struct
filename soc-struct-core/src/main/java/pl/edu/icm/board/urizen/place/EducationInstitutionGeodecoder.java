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

package pl.edu.icm.board.urizen.place;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.geography.prg.AddressPointManager;
import pl.edu.icm.board.geography.prg.model.GeocodedPoi;

import java.io.IOException;
import java.util.function.Consumer;

public class EducationInstitutionGeodecoder {

    private final EducationalInstitutionLoader educationalInstitutionLoader;
    private final AddressPointManager addressPointManager;

    @WithFactory
    public EducationInstitutionGeodecoder(EducationalInstitutionLoader educationalInstitutionLoader, AddressPointManager addressPointManager) {
        this.educationalInstitutionLoader = educationalInstitutionLoader;
        this.addressPointManager = addressPointManager;
    }

    public void foreach(Consumer<GeocodedPoi<EducationalInstitutionFromCsv>> consumer) throws IOException {
        educationalInstitutionLoader.load();
        educationalInstitutionLoader.forEach(institution -> {
            var results = addressPointManager.lookup(
                    institution.getCommuneTeryt(),
                    institution.getPostalCode(),
                    institution.getLocality(),
                    institution.getStreet(),
                    institution.getStreetNumber()
            );
            consumer.accept(new GeocodedPoi<>(results, institution));
        });
    }
}
