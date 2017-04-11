package eu.h2020.symbiote.ontology.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * Contains list of predicates used by symbIoTe Core Information Model
 *
 * Created by Szymon Mueller on 11/01/2017.
 */
public class CoreInformationModel {

    public static final String CIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/core#";

    public static final String MIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/meta#";

    public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String RDFS_PREFIX = "http://www.w3.org/2000/01/rdf-schema#";

    public static final String GEO_PREFIX = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String TIME_PREFIX = "http://www.w3.org/2006/time#";

    public static final String OWL_PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final Property RDF_TYPE;

    public static final Property RDFS_LABEL;

    public static final Property RDFS_COMMENT;

    public static final Property CIM_SENSOR;

    public static final Property CIM_ID;

    public static final Property CIM_LOCATED_AT;

    public static final Property GEO_LAT;

    public static final Property GEO_LONG;

    public static final Property GEO_ALT;

    public static final Property CIM_OBSERVES;

    static {
        Model m = ModelFactory.createDefaultModel();
        RDF_TYPE = m.createProperty( RDF_PREFIX + "type" );
        RDFS_LABEL = m.createProperty(RDFS_PREFIX + "label");
        RDFS_COMMENT = m.createProperty(RDFS_PREFIX + "comment");
        CIM_SENSOR = m.createProperty(CIM_PREFIX + "Sensor");
        CIM_ID = m.createProperty(CIM_PREFIX + "id");
        CIM_LOCATED_AT = m.createProperty(CIM_PREFIX + "locatedAt");
        GEO_LAT = m.createProperty(GEO_PREFIX + "lat");
        GEO_LONG = m.createProperty(GEO_PREFIX + "long");
        GEO_ALT = m.createProperty(GEO_PREFIX + "alt");
        CIM_OBSERVES = m.createProperty(CIM_PREFIX + "observes");
    }

}
