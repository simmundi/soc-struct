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
