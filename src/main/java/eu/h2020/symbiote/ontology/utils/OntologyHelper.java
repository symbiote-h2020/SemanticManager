/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.utils;


import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.semantics.ModelHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;

/**
 *
 * @author jab
 */
public class OntologyHelper {

    private static final Log log = LogFactory.getLog(OntologyHelper.class);

    public static final String ROOT_URI = "http://www.symbiote-h2020.eu/ontology/";

    private static final String ONT_DOC_MANAGER_CONFIG = "OntDocumentManagerConfig.ttl";

    public static final String BIM_URI = ROOT_URI + "bim";

    public static final int CORE_MODEL_ID = -1;


    /**
     * Graphs
     */
    public static final String MIM_GRAPH = ROOT_URI + "meta.owl";
    public static final String CORE_GRAPH = ROOT_URI + "core";
    public static final String MAPPING_GRAPH = ROOT_URI + "mappings";
    public static final String PLATFORMS_GRAPH = ROOT_URI + "platforms";
    public static final String INFORMATION_MODEL_GRAPH = ROOT_URI + "informationModel";
    public static final String RESOURCES_GRAPH = ROOT_URI + "resources";
//    public static final String MODELS_GRAPH = ROOT_URI + "models";

    /**
     * Classes
     */
    public static final String PLATFORM = MIM_GRAPH + "#Platform";
    public static final String MAPPING = MIM_GRAPH + "#Mapping";

    /**
     * Predicates
     */
    public static final String FROM = ROOT_URI + "from";
    public static final String TO = ROOT_URI + "to";
    public static final String USES = ROOT_URI + "uses";

    private OntologyHelper() {

    }

//    public static String getFoiURI(String platformId, String foi) {
//        return getPlatformGraphURI(platformId) + "/foi/" + foi;
//    }
//
    public static String getLocationURI(String platformId, String location) {
        return ModelHelper.getPlatformURI(platformId) + "/location/" + location;
    }

    public static String findBIMPlatformPropertyUri(String property) throws PropertyNotFoundException {
        String uri = SymbioteModelsUtil.findInSymbioteCoreModels(property);
        log.debug("Found property in symbIoTe models: " + uri);
        return uri;
    }

}
