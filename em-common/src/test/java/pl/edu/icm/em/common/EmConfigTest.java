/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EmConfigTest {
    private File file1;
    private File file2;

    @BeforeEach
    void before() throws IOException {
        file1 = copyToTmp("/simple.conf");
        file2 = copyToTmp("/complex.conf");
    }

    private File copyToTmp(String name) throws IOException {
        File file = File.createTempFile("test", ".conf").getAbsoluteFile();
        file.delete();
        Files.copy(getClass().getResourceAsStream(name), file.toPath());
        file.deleteOnExit();
        return file;
    }

    @Test
    @DisplayName("Should load config from hocon file")
    void loadHoconFile() throws IOException {
        Bento bento = EmConfig.configurer(new String[]{})
                .loadHoconFile(file1.getAbsolutePath())
                .loadHoconFile(file2.getAbsolutePath())
                .setParam("name", "Filip Dreger")
                .getConfig();

        assertThat(bento.getString("name")).isEqualTo("Filip Dreger");

        assertThat(bento.getInt("a")).isEqualTo(1);
        assertThat(bento.getFloat("b")).isEqualTo(2.4f);
        assertThat(bento.getBoolean("c")).isFalse();

        assertThat(bento.getString("some.embedded.json.some")).isEqualTo("json");
        assertThat(bento.getInt("some.complex.property.with.name")).isEqualTo(456);
        assertThat(bento.getString("some.embedded.json.with.nested")).isEqualTo("properties");
        assertThat(bento.getInt("some.complex.code.here")).isEqualTo(123);
    }
}