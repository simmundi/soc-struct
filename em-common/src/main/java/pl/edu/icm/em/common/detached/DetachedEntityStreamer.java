package pl.edu.icm.em.common.detached;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DetachedEntityStreamer {
    private final EngineBuilder engineBuilder;

    @WithFactory
    public DetachedEntityStreamer(EngineBuilder engineBuilder) {
        this.engineBuilder = engineBuilder;
    }

    public Stream<DetachedEntity> streamDetached() {
        Engine engine = engineBuilder.getEngine();
        int count = engine.getCount();
        return IntStream.range(0, count).mapToObj(id -> new DetachedEntity(id, engine.getDaoManager()));
    }
}
