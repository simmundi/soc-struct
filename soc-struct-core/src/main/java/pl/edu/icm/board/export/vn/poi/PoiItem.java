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

package pl.edu.icm.board.export.vn.poi;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;
import pl.edu.icm.trurl.io.visnow.VnCoords;

@WithDao
public class PoiItem implements VnCoords {
    private float x;
    private float y;
    private Type subsets;
    private int slots;
    private int taken;

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Type getSubsets() {
        return subsets;
    }

    public void setSubsets(Type subsets) {
        this.subsets = subsets;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public int getTaken() {
        return taken;
    }

    public void setTaken(int taken) {
        this.taken = taken;
    }

    public enum Type {
        CLERGY_HOUSE,
        NURSING_HOME,
        DORM,
        PRISON,
        MONASTERY,
        BARRACKS,

        EDU_PRESCHOOL,
        EDU_PRIMARY,
        EDU_HIGH,
        EDU_PRIMARY_AND_HIGH,
        EDU_UNIVERSITY,

        WORKPLACE,

        HEALTHCARE_POZ,
        HEALTHCARE_OTHER
    }
}
