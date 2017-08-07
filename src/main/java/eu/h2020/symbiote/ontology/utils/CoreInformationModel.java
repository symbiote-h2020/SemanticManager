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

    public static final String CIM_1_0_2_FILE = "core-v1.0.2.owl";
    
    public static final String CIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/core#";

    public static final String MIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/meta#";

    public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String RDFS_PREFIX = "http://www.w3.org/2000/01/rdf-schema#";

    public static final String GEO_PREFIX = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    public static final String TIME_PREFIX = "http://www.w3.org/2006/time#";

    public static final String OWL_PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final Property RDF_TYPE;

    public static final Property RDF_VALUE;

    public static final Property RDFS_LABEL;

    public static final Property RDFS_COMMENT;

    public static final Property CIM_RESOURCE;

    public static final Property CIM_STATIONARY;

    public static final Property CIM_MOBILE;

    public static final Property CIM_ACTUATOR;

    public static final Property CIM_SERVICE;

    public static final Property CIM_ACTUATING_SERVICE;

    public static final Property CIM_LOCATION;

    public static final Property CIM_WKT_LOCATION;

    public static final Property CIM_SYMBOLIC_LOCATION;

    public static final Property CIM_WGS84_LOCATION;

    public static final Property CIM_PARAMETER;

    public static final Property CIM_INPUT_PARAMETER;

    public static final Property CIM_RANGE_RESTRICTION;

    public static final Property CIM_LENGTH_RESTRICTION;

    public static final Property CIM_ENUM_RESTRICTION;

    public static final Property CIM_ID;

    public static final Property CIM_LOCATED_AT;

    public static final Property CIM_FOI;

    public static final Property CIM_HAS_FOI;

    public static final Property CIM_HAS_PROPERTY;

    public static final Property CIM_HAS_OUTPUT;

    public static final Property CIM_HAS_INPUT;

    public static final Property CIM_HAS_CAPABILITY;

    public static final Property CIM_NAME;

    public static final Property CIM_AFFECTS;

    public static final Property CIM_ACTS_ON;

    public static final Property CIM_DATATYPE;

    public static final Property CIM_IS_ARRAY;

    public static final Property CIM_MANDATORY;

    public static final Property CIM_HAS_RESTRICTION;

    public static final Property CIM_MIN;

    public static final Property CIM_MAX;

    public static final Property GEO_LAT;

    public static final Property GEO_LONG;

    public static final Property GEO_ALT;

    public static final Property CIM_OBSERVES;

    static {
        Model m = ModelFactory.createDefaultModel();
        RDF_TYPE = m.createProperty( RDF_PREFIX + "type" );
        RDF_VALUE = m.createProperty( RDF_PREFIX + "value" );
        RDFS_LABEL = m.createProperty(RDFS_PREFIX + "label");
        RDFS_COMMENT = m.createProperty(RDFS_PREFIX + "comment");
        CIM_RESOURCE = m.createProperty(CIM_PREFIX + "Resource");
        CIM_STATIONARY = m.createProperty(CIM_PREFIX + "StationarySensor");
        CIM_MOBILE = m.createProperty(CIM_PREFIX + "MobileSensor");
        CIM_SERVICE = m.createProperty(CIM_PREFIX + "Service");
        CIM_ACTUATOR = m.createProperty(CIM_PREFIX + "Actuator");
        CIM_ACTUATING_SERVICE =  m.createProperty(CIM_PREFIX + "ActuatingService");
        CIM_LOCATION = m.createProperty(CIM_PREFIX + "Location");
        CIM_WKT_LOCATION = m.createProperty(CIM_PREFIX + "WKTLocation");
        CIM_SYMBOLIC_LOCATION = m.createProperty(CIM_PREFIX + "SymbolicLocation");
        CIM_WGS84_LOCATION = m.createProperty(CIM_PREFIX + "WGS84Location");
        CIM_PARAMETER = m.createProperty(CIM_PREFIX + "Parameter");
        CIM_INPUT_PARAMETER = m.createProperty(CIM_PREFIX + "InputParameter");
        CIM_RANGE_RESTRICTION = m.createProperty(CIM_PREFIX + "RangeRestriction");
        CIM_LENGTH_RESTRICTION= m.createProperty(CIM_PREFIX + "LengthRestriction");
        CIM_ENUM_RESTRICTION = m.createProperty(CIM_PREFIX + "EnumRestriction");
        CIM_ID = m.createProperty(CIM_PREFIX + "id");
        CIM_LOCATED_AT = m.createProperty(CIM_PREFIX + "locatedAt");
        CIM_FOI = m.createProperty(CIM_PREFIX + "FeatureOfInterest");
        CIM_HAS_FOI = m.createProperty(CIM_PREFIX + "hasFeatureOfInterest");
        CIM_HAS_PROPERTY = m.createProperty(CIM_PREFIX + "hasProperty");
        CIM_NAME = m.createProperty(CIM_PREFIX + "name");
        CIM_ACTS_ON = m.createProperty(CIM_PREFIX + "actsOn");
        CIM_AFFECTS = m.createProperty(CIM_PREFIX + "affects");
        CIM_HAS_INPUT = m.createProperty(CIM_PREFIX + "hasInputParameter");
        CIM_HAS_OUTPUT = m.createProperty(CIM_PREFIX + "hasOutputParameter");
        CIM_HAS_CAPABILITY = m.createProperty(CIM_PREFIX + "hasCapability");
        CIM_DATATYPE = m.createProperty(CIM_PREFIX + "datatype");
        CIM_IS_ARRAY = m.createProperty(CIM_PREFIX + "isArray");
        CIM_MANDATORY = m.createProperty(CIM_PREFIX + "mandatory");
        CIM_HAS_RESTRICTION = m.createProperty(CIM_PREFIX + "hasRestriction");
        CIM_MIN = m.createProperty(CIM_PREFIX + "min");
        CIM_MAX = m.createProperty(CIM_PREFIX + "max");
        GEO_LAT = m.createProperty(GEO_PREFIX + "lat");
        GEO_LONG = m.createProperty(GEO_PREFIX + "long");
        GEO_ALT = m.createProperty(GEO_PREFIX + "alt");
        CIM_OBSERVES = m.createProperty(CIM_PREFIX + "observes");
    }

}
