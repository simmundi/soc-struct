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

package pl.edu.icm.em.socstruct.component.edu;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.util.AbstractCategory;

public class EducationLevel extends AbstractCategory {
    public final static EducationLevel K = new EducationLevel("K", 0, "kindergarten", false);
    public final static EducationLevel P = new EducationLevel("P", 1, "primary school", false);
    public final static EducationLevel H = new EducationLevel("H", 2, "high school", false);
    public final static EducationLevel PH = new EducationLevel("PH", 3, "primary and high school complex", false);
    public final static EducationLevel BU = new EducationLevel("BU", 4, "big university", false);
    public final static EducationLevel U = new EducationLevel("U", 5, "university", false);

    private final String description;
    private final boolean univeristy;

    @WithFactory
    public EducationLevel(String name, int ordinal, String description, boolean univeristy) {
        super(name, ordinal);
        this.description = description;
        this.univeristy = univeristy;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUniveristy() {
        return univeristy;
    }
}
