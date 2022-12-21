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

package pl.edu.icm.board.geography.gis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengis.feature.simple.SimpleFeature;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FeaturesInEnvelopeTest {

    @Mock
    SimpleFeature featureA;
    @Mock
    SimpleFeature featureB;
    @Mock
    SimpleFeature featureC;

    @Test
    void getFeatureWithMostArea() {
        // given
        FeaturesInEnvelope features = new FeaturesInEnvelope();
        features.addFeature(featureA, 1.6);
        features.addFeature(featureB, 1.1);
        features.addFeature(featureC, 1.2);

        // execute
        var result = features.getFeatureWithMostArea();
        var empty = features.isEmpty();

        // assert
        assertThat(result).isEqualTo(featureA);
        assertThat(empty).isEqualTo(false);
    }

    @Test
    void getFeatureWithMostArea__empty() {
        // given
        FeaturesInEnvelope features = new FeaturesInEnvelope();

        // executefeatureA
        var result = features.isEmpty();

        // assert
        assertThat(result).isEqualTo(true);
        assertThatThrownBy(() -> features.getFeatureWithMostArea())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getFeatureWithMostArea__ambiguous() {
        // given
        FeaturesInEnvelope features = new FeaturesInEnvelope();
        features.addFeature(featureA, 1.6);
        features.addFeature(featureB, 1.6);
        features.addFeature(featureC, 1.2);

        // execute & assert
        assertThatThrownBy(() -> features.getFeatureWithMostArea())
                .isInstanceOf(IllegalStateException.class);
    }

}
