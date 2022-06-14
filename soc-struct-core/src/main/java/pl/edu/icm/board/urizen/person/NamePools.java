package pl.edu.icm.board.urizen.person;

import pl.edu.icm.trurl.bin.BinPool;

public class NamePools {
    public final BinPool<String> maleNames = new BinPool<>();
    public final BinPool<String> femaleNames = new BinPool<>();
    public final BinPool<String> surnames = new BinPool<>();

    NamePools(){

    }
}
