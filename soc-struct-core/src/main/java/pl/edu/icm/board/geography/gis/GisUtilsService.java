package pl.edu.icm.board.geography.gis;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.annotation.WithFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.Envelope2D;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import pl.edu.icm.board.geography.KilometerGridCell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class GisUtilsService {

    public GisUtilsService(FilterFactory2 factory) {
        this.factory = factory;
    }

    @WithFactory
    public GisUtilsService() {
        this(CommonFactoryFinder.getFilterFactory2());
    }

    private final FilterFactory2 factory;
    private final Map<FeatureId, Geometry> fixedGeometry = new WeakHashMap<>();

    /**
     * Given a source of features and a KilometerGridCell, returns all the features
     * present in the cell along with the area of their intersection with the cell.
     *
     * This is useful for rasterization of features and might easily be extended
     * to other areas.
     *
     * Comments:
     * <ul>
     *     <li>the CRS of the features is assumed to have (N, E) coordinates, in meters, this is, to some degree, verified</li>
     *     <li>the WTS for EPSG:2180 in gt-epsg-wkt is broken!</li>
     * </ul>
     *
     *
     * @param features, sharing CRS
     * @param kilometerGridCell
     * @return value object associating features present in the cell along with their area of intersection
     * @throws IOException
     */
    public FeaturesInEnvelope findFeaturesInCell(SimpleFeatureSource features, KilometerGridCell kilometerGridCell) throws IOException {
        CoordinateReferenceSystem cs = features.getSchema().getCoordinateReferenceSystem();
        CoordinateSystem crs = cs.getCoordinateSystem();
        // there are EPSG:2180 implementations with N, E mixed up as E, N
        // we prefer to fail fast
        checkPreconditions(crs);

        // we are using several different APIs, so we need several objects to represent
        // a single cell: KilometerGridCell, an Envelope2d and a Rectangle

        // Envelope2D represents the cell and is needed to build the spatial query
        Envelope2D envelope = createEnvelope(cs, kilometerGridCell);
        FeaturesInEnvelope result = new FeaturesInEnvelope();
        double envelopeArea = 1_000_000;

        var geometryDescriptor = features.getSchema().getGeometryDescriptor();
        Filter filter = factory.bbox(factory.property(geometryDescriptor.getName()), envelope);

        // candidates are all features present in the envelope
        List<SimpleFeature> candidates = featuresToList(features.getFeatures(filter));

        // if there's only one, we don't calculate intersection
        // and assume that the feature takes all the space within the envelope
        if (candidates.size() == 1) {
            result.addFeature(candidates.get(0), envelopeArea);
        } else  {
            for (SimpleFeature feature : candidates) {
                // In practice some features have broken geometry; this can be
                // fixed by the JTS's built-in fixer, but is costly, so we
                // use the fixedGeometry map as cache for valid geometry
                Geometry featureGeometry = fixedGeometry.computeIfAbsent(
                        feature.getIdentifier(),
                        (id) -> GeometryFixer.fix((Geometry) feature.getDefaultGeometryProperty().getValue()));
                // rectangle (in proper CRS) is needed by the topoly API to calculate
                // the area of intersection - it's the same cell as KilometerGridCell and the Envelope
                Geometry targetRectangle = featureGeometry.getFactory().toGeometry(new Envelope(
                        envelope.x,
                        envelope.x + envelope.width,
                        envelope.y,
                        envelope.y + envelope.height
                ));

                // each feature within the envelope is added to the results, along with
                // its area of intersection with the cell
                result.addFeature(
                        feature,
                        featureGeometry.intersection(targetRectangle).getArea());
            }
        }
        return result;
    }

    private List<SimpleFeature> featuresToList(SimpleFeatureCollection features) {
        List<SimpleFeature> results = new ArrayList<>();
        try (SimpleFeatureIterator filterResults = features.features()) {
            while (filterResults.hasNext()) {
                results.add(filterResults.next());
            }
        }
        return results;
    }

    private Envelope2D createEnvelope(CoordinateReferenceSystem crs, KilometerGridCell kilometerGridCell) {
        return new Envelope2D(crs,
                kilometerGridCell.getN() * 1000, kilometerGridCell.getE() * 1000,
                1000, 1000);
    }

    private void checkPreconditions(CoordinateSystem crs) {
        Preconditions.checkState(crs.getAxis(0).getDirection() == AxisDirection.NORTH, "Broken assumption that X is NORTH");
        Preconditions.checkState(crs.getAxis(1).getDirection() == AxisDirection.EAST, "Broken assumption that Y is EAST");
    }
}
