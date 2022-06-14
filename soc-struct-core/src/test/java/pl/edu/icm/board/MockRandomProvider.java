package pl.edu.icm.board;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import pl.edu.icm.board.util.RandomProvider;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class MockRandomProvider extends RandomProvider {
    private RandomGenerator randomGenerator;
    private RandomDataGenerator randomDataGenerator;

    public MockRandomProvider() {
        super(0);
        this.randomGenerator = Mockito.mock(RandomGenerator.class);
        this.randomDataGenerator = Mockito.mock(RandomDataGenerator.class);
        when(randomDataGenerator.getRandomGenerator()).thenReturn(randomGenerator);
        doAnswer((Answer<Integer>) invocation -> {
            int value1 = invocation.getArgument(0);
            int value2 = invocation.getArgument(1);
            return (value1 + value2)/2;
        }).when(randomDataGenerator).nextInt(anyInt(),anyInt());    }

    @Override
    public RandomDataGenerator getRandomDataGenerator(String label) {
        return randomDataGenerator;
    }

    @Override
    public RandomDataGenerator getRandomDataGenerator(Class label) {
        return randomDataGenerator;
    }

    @Override
    public RandomGenerator getRandomGenerator(String label) {
        return randomGenerator;
    }

    @Override
    public RandomGenerator getRandomGenerator(Class label) {
        return randomGenerator;
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }
}
