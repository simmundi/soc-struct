package pl.edu.icm.board.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import pl.edu.icm.trurl.ecs.selector.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RandomForChunkProvider implements Function<Chunk, RandomGenerator> {
    private final Map<Integer, RandomGenerator> chunkToRandomGeneratorMap = new ConcurrentHashMap<>();
    private final int initialSeed;

    RandomForChunkProvider(int initialSeed) {
        this.initialSeed = initialSeed;
    }

    @Override
    public RandomGenerator apply(Chunk chunk) {
        var chunkId = chunk.getChunkInfo().getChunkId();
        return chunkToRandomGeneratorMap.computeIfAbsent(chunkId, i -> new Well19937c(initialSeed + i));
    }
}
