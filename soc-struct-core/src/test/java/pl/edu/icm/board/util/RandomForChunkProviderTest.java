package pl.edu.icm.board.util;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;

import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class RandomForChunkProviderTest {

    @Test
    void apply() {
        //given
        RandomForChunkProvider randomForChunkProvider = new RandomForChunkProvider("test".hashCode());
        RandomForChunkProvider randomForChunkProvider2 = new RandomForChunkProvider("test".hashCode());

        //execute
        var randomIntsSingleThread = IntStream.range(0, 48)
                .mapToObj(i -> new Chunk(ChunkInfo.of(i, 0), IntStream.empty()))
                .map(randomForChunkProvider)
                .mapToInt(RandomGenerator::nextInt).toArray();

        var randomIntsMultiThread = IntStream.range(0, 48)
                .parallel()
                .mapToObj(i -> new Chunk(ChunkInfo.of(i, 0), IntStream.empty()))
                .map(randomForChunkProvider2)
                .mapToInt(RandomGenerator::nextInt).toArray();

        //assert
        assertThat(randomIntsMultiThread).containsExactlyInAnyOrder(randomIntsSingleThread);
    }
}