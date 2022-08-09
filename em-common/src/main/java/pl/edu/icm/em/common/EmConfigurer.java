package pl.edu.icm.em.common;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.config.Configurer;
import net.snowyhollows.bento.config.WorkDir;

import java.io.IOException;
import java.util.function.Consumer;

class EmConfigurer extends Configurer {
    private final Consumer<EmConfigurer> postConfigure;
    private boolean postConfigured;

    public EmConfigurer(WorkDir workDir, Consumer<EmConfigurer> postConfigure) {
        super(workDir);
        this.postConfigure = postConfigure;
    }

    @Override
    public Bento getConfig() {
        Bento bento = super.getConfig();
        maybePostConfigure();
        return bento;
    }

    @Override
    public Configurer initialize(BentoFactory<?>... factories) {
        maybePostConfigure();
        return super.initialize(factories);
    }

    @Override
    public <T> Configurer use(BentoFactory<T> factory, Consumer<T> user) {
        maybePostConfigure();
        return super.use(factory, user);
    }

    @Override
    public <T> Configurer useWithIo(BentoFactory<T> factory, IoConsumer<T> user) throws IOException {
        maybePostConfigure();
        return super.useWithIo(factory, user);
    }

    @Override
    public Configurer loadConfigDir(String dirPath) throws IOException {
        try {
            assertNotPostConfigured();
            return super.loadConfigDir(dirPath);
        } catch (NullPointerException npe) {
            System.err.println("Config path not found in the working directory");
            return this;
        }
    }

    @Override
    public Configurer loadConfigFile(String filename) throws IOException {
        assertNotPostConfigured();
        return super.loadConfigFile(filename);
    }

    @Override
    public Configurer setParam(String key, Object value) {
        assertNotPostConfigured();
        return super.setParam(key, value);
    }

    @Override
    public Configurer overrideParam(String key, Object value) {
        assertNotPostConfigured();
        return super.overrideParam(key, value);
    }

    private void maybePostConfigure() {
        if (!postConfigured) {
            postConfigure.accept(this);
            postConfigured = true;
        }
    }

    private void assertNotPostConfigured() {
        if (postConfigured) {
            throw new IllegalStateException("Programmer error: configuration modified after already locked");
        }
    }
}
