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
