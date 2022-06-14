package pl.edu.icm.board.util;

import net.snowyhollows.bento2.annotation.WithFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension point for adding controlled randomness
 */
public class RandomProvider {
    private final int randomProviderSeed;
    private final Map<String, RandomDataGenerator> randomGeneratorMap;
    private final Map<String, RandomForChunkProvider> randomDataForChunkProviderMap;

    @WithFactory
    public RandomProvider(int randomProviderSeed) {
        this.randomProviderSeed = randomProviderSeed;
        this.randomGeneratorMap = new HashMap<>();
        this.randomDataForChunkProviderMap = new HashMap<>();
    }

    public RandomDataGenerator getRandomDataGenerator(String label) {
        return this.randomGeneratorMap.computeIfAbsent(label, k -> {
            RandomDataGenerator random = new RandomDataGenerator();
            random.reSeed(this.randomProviderSeed + label.hashCode());
            return random;
        });
    }

    public RandomDataGenerator getRandomDataGenerator(Class<?> label) {
        return getRandomDataGenerator(label.getCanonicalName());
    }

    public RandomGenerator getRandomGenerator(String label) {
        return getRandomDataGenerator(label).getRandomGenerator();
    }

    public RandomGenerator getRandomGenerator(Class<?> label) {
        return getRandomGenerator(label.getCanonicalName());
    }

    private RandomForChunkProvider getRandomForChunkProvider(String label) {
        return this.randomDataForChunkProviderMap.computeIfAbsent(label, i -> new RandomForChunkProvider(this.randomProviderSeed + i.hashCode()));
    }

    public RandomForChunkProvider getRandomForChunkProvider(Class<?> label) {
        return getRandomForChunkProvider(label.getCanonicalName());
    }
}
