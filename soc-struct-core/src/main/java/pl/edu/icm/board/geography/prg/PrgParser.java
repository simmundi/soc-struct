package pl.edu.icm.board.geography.prg;

import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.trurl.xml.Parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrgParser extends Parser  {

    public final static String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public final static String NS_GML = "http://www.opengis.net/gml/3.2";
    public final static String NS_PRG_AD = "urn:gugik:specyfikacje:gmlas:panstwowyRejestrGranicAdresy:1.0";

    public final static QName FEATURE_COLLECTION = new QName(NS_GML, "FeatureCollection");
    public final static QName FEATURE_MEMBERS = new QName(NS_GML, "featureMembers");
    public final static QName PRG_PUNKT_ADRESOWY = new QName(NS_PRG_AD, "PRG_PunktAdresowy");
    public final static QName PRG_MIEJSCOWOSC = new QName(NS_PRG_AD, "miejscowosc");
    public final static QName PRG_CZESC_MIEJSCOWOSCI = new QName(NS_PRG_AD, "czescMiejscowosci");
    public final static QName PRG_ULICA = new QName(NS_PRG_AD, "ulica");
    public final static QName PRG_NUMER_PORZADKOWY = new QName(NS_PRG_AD, "numerPorzadkowy");
    public final static QName PRG_KOD_POCZTOWY = new QName(NS_PRG_AD, "kodPocztowy");
    public final static QName PRG_POZYCJA = new QName(NS_PRG_AD, "pozycja");
    public final static QName GML_POINT = new QName(NS_GML, "Point");
    public final static QName GML_POS = new QName(NS_GML, "pos");

    public final static QName ATTR_GML_ID = new QName(NS_GML, "id");
    public final static QName ATTR_XSI_NIL = new QName(NS_XSI, "true");

    public final static Pattern PATTERN_COORDS = Pattern.compile("^(\\d+\\.\\d++) (\\d+\\.\\d+)$");

    public PrgParser(XMLEventReader reader) {
        super(reader, null);
    }

    public void parse(Consumer<AddressPoint> consumer) throws XMLStreamException {
        inElement(FEATURE_COLLECTION, () -> {
            inElement(FEATURE_MEMBERS, () -> {
                forEach(PRG_PUNKT_ADRESOWY, () -> {
                    AddressPoint punkt = new AddressPoint();
                    punkt.setPrgId(getAttribValue(ATTR_GML_ID));
                    forEachSwitch(
                            caseIf(PRG_MIEJSCOWOSC, () -> punkt.setLocality(getElementTrimmedStringValue())),
                            caseIf(PRG_CZESC_MIEJSCOWOSCI, () -> {
                                if (getAttribValue(ATTR_XSI_NIL) == null) {
                                    punkt.setFineLocality(getElementTrimmedStringValue());
                                }
                            }),
                            caseIf(PRG_ULICA, () -> {
                                if (getAttribValue(ATTR_XSI_NIL) == null) {
                                    punkt.setStreet(getElementTrimmedStringValue());
                                }
                            }),
                            caseIf(PRG_KOD_POCZTOWY, () -> punkt.setPostalCode(getElementTrimmedStringValue())),
                            caseIf(PRG_NUMER_PORZADKOWY, () -> punkt.setNumber(getElementTrimmedStringValue())),
                            caseIf(PRG_POZYCJA, () -> {
                                inElement(GML_POINT, () -> {
                                    inElement(GML_POS, () -> {
                                        String pos = getElementStringValue();
                                        Matcher matcher = PATTERN_COORDS.matcher(pos);
                                        if (!matcher.matches()) {
                                            throw new IllegalArgumentException("doesn't match: <" + pos + ">");
                                        }
                                        punkt.setNorthing(Float.parseFloat(matcher.group(1)));
                                        punkt.setEasting(Float.parseFloat(matcher.group(2)));
                                    });
                                });
                            })
                    );
                    consumer.accept(punkt);
                });
            });
        });
    }
}
