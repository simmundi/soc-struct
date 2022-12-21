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

package pl.edu.icm.board.geography.commune;

public enum AdministrationAreaType {
    VILLAGE,
    CITY_S,
    CITY_M,
    CITY_L,
    CITY_XL;

    public static AdministrationAreaType fromCityPopulation(int population) {
        if (population >= 100_000) {
            return CITY_XL;
        } else if (population >= 50_000) {
            return CITY_L;
        } else if (population >= 20_000) {
            return CITY_M;
        } else return CITY_S;
    }
}
