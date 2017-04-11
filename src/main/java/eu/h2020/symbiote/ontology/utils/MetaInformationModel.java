package eu.h2020.symbiote.ontology.utils;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

/**
 * Contains list of predicates used by symbIoTe Metainformation Model
 *
 * Created by Szymon Mueller on 11/01/2017.
 */
public class MetaInformationModel {

    public static final String CIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/core#";

    public static final String MIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/meta#";

    public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String OWL_PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final Property RDF_TYPE;

    public static final Property OWL_ONTOLOGY;

    public static final Property MIM_HASDESCRIPTION;

    public static final Property MIM_HASNAME;

    public static final Property MIM_HASSERVICE;

    public static final Property MIM_HASINFORMATIONMODEL;

    public static final Property CIM_HASID;

    public static final Property MIM_HASURL;

    public static final Property MIM_INTERWORKINGSERVICE;

    static {
        Model m = ModelFactory.createDefaultModel();
        RDF_TYPE = m.createProperty( RDF_PREFIX + "type" );
        OWL_ONTOLOGY = m.createProperty( OWL_PREFIX + "OntologyHelper" );
        MIM_HASDESCRIPTION = m.createProperty( MIM_PREFIX + "hasDescription");
        MIM_HASNAME = m.createProperty( MIM_PREFIX + "hasName");
        MIM_HASSERVICE = m.createProperty( MIM_PREFIX + "hasService");
        MIM_HASINFORMATIONMODEL = m.createProperty( MIM_PREFIX + "hasInformationModel");
        CIM_HASID = m.createProperty( CIM_PREFIX + "hasID");
        MIM_HASURL = m.createProperty( MIM_PREFIX + "hasURL");
        MIM_INTERWORKINGSERVICE = m.createProperty( MIM_PREFIX + "InterworkingService");
    }

}
