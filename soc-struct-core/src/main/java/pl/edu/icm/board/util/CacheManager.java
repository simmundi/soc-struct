package pl.edu.icm.board.util;

import net.snowyhollows.bento.annotation.WithFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheManager {

    private final List<Freeable> freeables = new CopyOnWriteArrayList<>();

    @WithFactory
    public CacheManager() {
    }

    public void register(Freeable freeable) {
        freeables.add(freeable);
    }

    public void freeAllCaches() {
        for (Freeable freeable : freeables) {
            freeable.free();
        }
    }

    public interface Freeable {
        void free();
    }

    public interface Loadable {
        void load();
    }

    public interface HasCache extends Freeable, Loadable {}
}
