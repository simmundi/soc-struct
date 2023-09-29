/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.em.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.config.Configurer;
import net.snowyhollows.bento.config.WorkDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class EmConfigurer extends Configurer {
    private final Consumer<EmConfigurer> postConfigure;
    private final File rootPath;
    private boolean postConfigured;

    public EmConfigurer(WorkDir workDir, File rootPath, Consumer<EmConfigurer> postConfigure) {
        super(workDir);
        this.rootPath = rootPath;
        this.postConfigure = postConfigure;
    }

    @Override
    public Bento getConfig() {
        Bento bento = super.getConfig();
        maybePostConfigure();
        return bento;
    }

    @Override
    public EmConfigurer initialize(BentoFactory<?>... factories) {
        maybePostConfigure();
        super.initialize(factories);
        return this;
    }

    @Override
    public <T> EmConfigurer use(BentoFactory<T> factory, Consumer<T> user) {
        maybePostConfigure();
        super.use(factory, user);
        return this;
    }

    @Override
    public <T> EmConfigurer useWithIo(BentoFactory<T> factory, IoConsumer<T> user) throws IOException {
        maybePostConfigure();
        super.useWithIo(factory, user);
        return this;
    }

    @Override
    public EmConfigurer loadConfigDir(String dirPath) throws IOException {
        try {
            assertNotPostConfigured();
            super.loadConfigDir(dirPath);
        } catch (NullPointerException npe) {
            System.err.println("Config path not found in the working directory");
        }
        return this;
    }

    @Override
    public EmConfigurer loadConfigFile(String filename) throws IOException {
        assertNotPostConfigured();
        super.loadConfigFile(filename);
        return this;
    }

    public EmConfigurer loadHoconFile(String filename) {
        File file = new File(filename);
        file = file.isAbsolute() ? file : new File(rootPath.getAbsolutePath(), file.getPath());
        Config config = ConfigFactory.parseFile(file);
        config.entrySet().forEach(entry -> {
            setParam(entry.getKey(), entry.getValue().unwrapped().toString());
        });
        return this;
    }

    public EmConfigurer loadHoconResource(String resource) throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resource);
            }

            Config config = ConfigFactory.parseReader(new InputStreamReader(inputStream));
            config.entrySet().forEach(entry -> {
                setParam(entry.getKey(), entry.getValue().unwrapped().toString());
            });
        } catch (Exception e) {
            throw new IOException("Couldn't read resource " + resource, e);
        }
        return this;
    }

    @Override
    public EmConfigurer setParam(String key, Object value) {
        assertNotPostConfigured();
        super.setParam(key, value);
        return this;
    }

    @Override
    public EmConfigurer overrideParam(String key, Object value) {
        assertNotPostConfigured();
        super.overrideParam(key, value);
        return this;
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
