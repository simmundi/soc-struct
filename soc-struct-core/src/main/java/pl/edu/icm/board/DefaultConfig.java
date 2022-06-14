package pl.edu.icm.board;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import net.snowyhollows.bento2.Bento;
import net.snowyhollows.bento2.BentoFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

public class DefaultConfig {

    public static final class Configurer {

        private final Bento bento;

        private Configurer() {
            this.bento = Bento.createRoot();
        }

        public Configurer propertiesDir(String dirPath) throws IOException {
            loadAllConfigFiles(dirPath, bento);
            return this;
        }

        public Configurer propertiesFile(String filename) throws IOException {
            loadProperties(filename, bento);
            return this;
        }

        public Configurer propertiesStream(InputStream inputStream) throws IOException {
            loadProperties(new InputStreamReader(inputStream, Charsets.UTF_8), bento);
            return this;
        }

        public Configurer param(String key, Object value) {
            bento.register(key, value);
            return this;
        }

        public<T, U extends T> Configurer param(BentoFactory<T> factory, U object) {
            bento.register(factory, object);
            return this;
        }

        public Configurer override(String key, Object value) {
            assertKeyDefined(key);
            param(key, value);
            return this;
        }

        public<T, U extends T> Configurer override(BentoFactory<T> factory, U object) {
            assertKeyDefined(factory);
            param(factory, object);
            return this;
        }

        public Configurer initializing(BentoFactory<?>... factories) {
            for (BentoFactory<?> factory : factories) {
                bento.get(factory);
            }
            return this;
        }

        public Bento getConfig() {
            return bento;
        }

        public<T> Configurer use(BentoFactory<T> factory, LenientConsumer<T> user) throws IOException {
            user.accept(bento.get(factory, null));
            return this;
        }

        private void assertKeyDefined(Object key) {
            Preconditions.checkArgument(bento.get(key, null) != null,
                    "key `%s` is not undefined. Use 'param' instead of 'override'.",
                    key);
        }
    }

    private DefaultConfig() {
    }

    public static Configurer createWith() throws IOException {
        return new Configurer().propertiesDir("input/config");
    }

    public static Bento create() throws IOException {
        return create("input/config");
    }

    public static Bento create(String configPath)
            throws IOException {
        return new Configurer()
                .propertiesDir(configPath)
                .getConfig();
    }

    private static void loadProperties(String configPath, Bento config) throws IOException {
        Preconditions.checkArgument(new File(configPath).isFile(), "%s is not a file", configPath);
        loadProperties(new FileReader(configPath, Charsets.UTF_8), config);
    }

    private static void loadProperties(Reader reader, Bento config) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            config.register(entry.getKey(), entry.getValue());
        }
    }

    private static void loadAllConfigFiles(String configDir, Bento config) throws IOException {
        Preconditions.checkArgument(new File(configDir).isDirectory(), "%s is not a directory", configDir);
        for (File file : new File(configDir).listFiles(f -> !f.isDirectory())) {
            loadProperties(file.getAbsolutePath(), config);
        }
    }

    public interface LenientConsumer<T> {
        void accept(T t) throws IOException;
    }
}
