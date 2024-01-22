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

package pl.edu.icm.board.geography.prg;

import pl.edu.icm.board.geography.prg.model.AddressPoint;
import pl.edu.icm.em.socstruct.component.geo.Location;
import pl.edu.icm.trurl.xml.Parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.util.function.BiConsumer;
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

    public void parse(BiConsumer<AddressPoint, Location> consumer) throws XMLStreamException {
        inElement(FEATURE_COLLECTION, () -> {
            inElement(FEATURE_MEMBERS, () -> {
                forEach(PRG_PUNKT_ADRESOWY, () -> {
                    AddressPoint ap = new AddressPoint();
                    Location location = new Location();
                    ap.setPrgId(getAttribValue(ATTR_GML_ID));
                    forEachSwitch(
                            caseIf(PRG_MIEJSCOWOSC, () -> ap.setLocality(getElementTrimmedStringValue())),
                            caseIf(PRG_CZESC_MIEJSCOWOSCI, () -> {
                                if (getAttribValue(ATTR_XSI_NIL) == null) {
                                    ap.setFineLocality(getElementTrimmedStringValue());
                                }
                            }),
                            caseIf(PRG_ULICA, () -> {
                                if (getAttribValue(ATTR_XSI_NIL) == null) {
                                    ap.setStreet(getElementTrimmedStringValue());
                                }
                            }),
                            caseIf(PRG_KOD_POCZTOWY, () -> ap.setPostalCode(getElementTrimmedStringValue())),
                            caseIf(PRG_NUMER_PORZADKOWY, () -> ap.setNumber(getElementTrimmedStringValue())),
                            caseIf(PRG_POZYCJA, () -> {
                                inElement(GML_POINT, () -> {
                                    inElement(GML_POS, () -> {
                                        String pos = getElementStringValue();
                                        Matcher matcher = PATTERN_COORDS.matcher(pos);
                                        if (!matcher.matches()) {
                                            throw new IllegalArgumentException("doesn't match: <" + pos + ">");
                                        }
                                        location.setN(Float.parseFloat(matcher.group(1)));
                                        location.setE(Float.parseFloat(matcher.group(2)));
                                    });
                                });
                            })
                    );
                    consumer.accept(ap, location);
                });
            });
        });
    }
}
