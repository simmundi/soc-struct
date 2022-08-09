package pl.edu.icm.board.geography.gis;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.collection.SpatialIndexFeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xsd.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

class CommuneGisReader {
    private final String graniceGminFilename;
    private final WorkDir workDir;

    @WithFactory
    public CommuneGisReader(String graniceGminFilename, WorkDir workDir) {
        this.graniceGminFilename = graniceGminFilename;
        this.workDir = workDir;
    }

    /**
     * Retrieves GIS data from PRG (database A03) as a feature source.
     * @return
     */
    public SimpleFeatureSource communes() {
        try (InputStream in = workDir.openForReading(new File(graniceGminFilename))) {
            GMLConfiguration gml = new GMLConfiguration();
            Parser parser = new Parser(gml);
            parser.setStrict(false);

            Map<String, Object> parseResult = (Map<String, Object>) parser.parse(in);
            List<SimpleFeature> simpleFeatureList = (List<SimpleFeature>) parseResult.get("member");
            SpatialIndexFeatureCollection spatialIndexFeatureCollection =
                    new SpatialIndexFeatureCollection(simpleFeatureList.get(0).getFeatureType());
            for (SimpleFeature simpleFeature : simpleFeatureList) {
                spatialIndexFeatureCollection.add(simpleFeature);
            }

            simpleFeatureList.get(0).getFeatureType().getGeometryDescriptor().getCoordinateReferenceSystem();
            return new SpatialIndexFeatureSource(spatialIndexFeatureCollection);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
