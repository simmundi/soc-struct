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

import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Value object describing all features contained within an envelope,
 * along with their area.
 *
 * Since, for now, only the feature with the biggest area is used, this class
 * serves little purpose aside from being an extension point.
 */
public class FeaturesInEnvelope {

    private Map<SimpleFeature, Double> features = new HashMap<>();

    public boolean isEmpty() {
        return features.isEmpty();
    }

    public void addFeature(SimpleFeature simpleFeature, double area) {
        features.put(simpleFeature, area);
    }

    public SimpleFeature getFeatureWithMostArea() {
        List<SimpleFeature> result = getFeaturesWithMostArea().collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new IllegalStateException("No features in cell");
        }
        if (result.size() != 1) {
            throw new IllegalStateException("Multiple features with equal area: " + result);
        }
        return result.get(0);
    }

    private Stream<SimpleFeature> getFeaturesWithMostArea() {
        double max = features.values().stream().mapToDouble(d -> d).max().orElse(0.0);
        return features.entrySet().stream()
                .filter(e -> e.getValue().doubleValue() == max)
                .map(e -> e.getKey());
    }

}
