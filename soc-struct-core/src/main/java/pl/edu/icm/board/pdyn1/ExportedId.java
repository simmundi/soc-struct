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

package pl.edu.icm.board.pdyn1;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class ExportedId {
    int pdyn2Id;
    int pdyn1Id;

    public ExportedId() {
    }

    public int getPdyn2Id() {
        return pdyn2Id;
    }

    public void setPdyn2Id(int pdyn2Id) {
        this.pdyn2Id = pdyn2Id;
    }

    public int getPdyn1Id() {
        return pdyn1Id;
    }

    public void setPdyn1Id(int pdyn1Id) {
        this.pdyn1Id = pdyn1Id;
    }
}
