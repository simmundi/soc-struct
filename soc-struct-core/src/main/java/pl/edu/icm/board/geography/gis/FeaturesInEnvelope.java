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
