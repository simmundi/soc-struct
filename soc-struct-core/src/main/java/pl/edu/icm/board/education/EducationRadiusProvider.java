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

package pl.edu.icm.board.education;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.board.model.EducationLevel;

public class EducationRadiusProvider {
    private final int kindergartenRadius;
    private final int primarySchoolRadius;
    private final int highSchoolRadius;
    private final int universityRadius;
    private final int bigUniversityRadius;

@WithFactory
   public EducationRadiusProvider(int kindergartenRadius,
                                  int primarySchoolRadius,
                                  int highSchoolRadius,
                                  int universityRadius,
                                  int bigUniversityRadius) {
       this.kindergartenRadius = kindergartenRadius;
       this.primarySchoolRadius = primarySchoolRadius;
       this.highSchoolRadius = highSchoolRadius;
       this.universityRadius = universityRadius;
       this.bigUniversityRadius = bigUniversityRadius;
   }

   public int getRadius(EducationLevel level) {
       switch(level){
           case K:
               return kindergartenRadius;
           case P:
               return primarySchoolRadius;
           case H:
               return highSchoolRadius;
           case U:
               return universityRadius;
           case BU:
               return bigUniversityRadius;
           default:
               return 0;
       }
   }
}
