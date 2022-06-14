package pl.edu.icm.board.geography.commune;

import net.snowyhollows.bento2.annotation.WithFactory;

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
