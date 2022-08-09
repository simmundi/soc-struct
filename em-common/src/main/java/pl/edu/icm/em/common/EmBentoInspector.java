package pl.edu.icm.em.common;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.inspector.BentoInspector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class EmBentoInspector implements BentoInspector {
    private final LinkedHashMap<String, String> values = new LinkedHashMap();

    @Override
    public void createChild(Bento parent, Bento bento) {
        // noop
    }

    @Override
    public void createObject(Bento creator, Object key, Object value) {
        String keyString = key instanceof BentoFactory ? key.getClass().getSimpleName() : Objects.toString(key);
        values.put(keyString, Objects.toString(value));
    }

    @Override
    public void disposeOfChild(Bento bento) {
        // noop
    }

    Stream<Map.Entry<String, String>> values() {
        return values.entrySet().stream();
    }
}
