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

import net.snowyhollows.bento.annotation.WithFactory;

import java.util.List;


public class TerytsOfBigCities {

    @WithFactory
    public TerytsOfBigCities(){

    }


    /**
     * @return teryt numbers for capitals of voivodeships
     */
     public List<String> getAllTeryts() {
         return List.of("1465011", "0264011", "0463011", "0663011", "0861011", "0862011",
                 "1061011", "1261011", "1661011", "1863011", "2061011", "2261011",
                 "2469011", "2661011", "2862011", "3064011", "3262011", "0461011");
    }
}
