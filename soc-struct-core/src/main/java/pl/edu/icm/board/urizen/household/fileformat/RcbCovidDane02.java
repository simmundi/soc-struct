package pl.edu.icm.board.urizen.household.fileformat;

import pl.edu.icm.board.geography.KilometerGridCell;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class RcbCovidDane02 {
    private String id_oczka;
    private byte flaga_70lat_i_wiecej;
    private byte ile_osob_w_mieszkaniu;

    public String getId_oczka() {
        return id_oczka;
    }

    public void setId_oczka(String id_oczka) {
        this.id_oczka = id_oczka;
    }

    public byte getFlaga_70lat_i_wiecej() {
        return flaga_70lat_i_wiecej;
    }

    public void setFlaga_70lat_i_wiecej(byte flaga_70lat_i_wiecej) {
        this.flaga_70lat_i_wiecej = flaga_70lat_i_wiecej;
    }

    public byte getIle_osob_w_mieszkaniu() {
        return ile_osob_w_mieszkaniu;
    }

    public void setIle_osob_w_mieszkaniu(byte ile_osob_w_mieszkaniu) {
        this.ile_osob_w_mieszkaniu = ile_osob_w_mieszkaniu;
    }

    public boolean getFlag70plus() {
        return getFlaga_70lat_i_wiecej() == 1;
    }

    public KilometerGridCell getCell() {
        return KilometerGridCell.fromIdOczkaGus(id_oczka);
    }

    public int getInhabintantsCount() {
        return ile_osob_w_mieszkaniu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RcbCovidDane02 that = (RcbCovidDane02) o;
        return flaga_70lat_i_wiecej == that.flaga_70lat_i_wiecej && ile_osob_w_mieszkaniu == that.ile_osob_w_mieszkaniu && id_oczka.equals(that.id_oczka);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_oczka, flaga_70lat_i_wiecej, ile_osob_w_mieszkaniu);
    }
}
