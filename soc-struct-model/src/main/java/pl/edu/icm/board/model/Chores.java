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

package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Chores {

    private byte chores;

    public enum Chore {
        WALKING_KIDS_TO_SCHOOL
    }

    public boolean hasChore(Chore chore) {
        return (chores & (1 << chore.ordinal())) != 0;
    }

    public void assign(Chore chore) {
        chores |= (1 << chore.ordinal());
    }

    public void removeChore(Chore chore) {
        chores &= ~(1 << chore.ordinal());
    }
}
