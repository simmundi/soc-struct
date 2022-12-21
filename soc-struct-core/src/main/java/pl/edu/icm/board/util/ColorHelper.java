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

package pl.edu.icm.board.util;

import net.snowyhollows.bento.annotation.WithFactory;

import java.awt.*;

public class ColorHelper {

    public final Color COLOR_A;
    public final Color COLOR_B;
    public final Color COLOR_C;
    public final Color COLOR_D;
    public final Color COLOR_E;

    @WithFactory
    public ColorHelper(String colorA, String colorB, String colorC, String colorD, String colorE) {
        COLOR_A = Color.decode(colorA);
        COLOR_B = Color.decode(colorB);
        COLOR_C = Color.decode(colorC);
        COLOR_D = Color.decode(colorD);
        COLOR_E = Color.decode(colorE);
    }

    public Color colorFromTeryt(String teryt) {
        String base = teryt.substring(0, 2);
        switch (base) {
            case "16":
            case "26":
            case "28":
            case "32":
                return COLOR_A;
            case "22":
            case "24":
            case "14":
                return COLOR_B;
            case "20":
            case "08":
            case "10":
                return COLOR_C;
            case "30":
            case "06":
            case "12":
                return COLOR_D;
            case "02":
            case "04":
            case "18":
                return COLOR_E;
        }
        return Color.WHITE;
    }
}
