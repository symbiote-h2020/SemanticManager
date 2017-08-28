/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbIoTe.ontology.MetaInformationModel;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author jab
 */
public class OntologyHelper {

    private static final Log log = LogFactory.getLog(OntologyHelper.class);

    public static final String ROOT_URI = "http://www.symbiote-h2020.eu/ontology/";

    public static final String BIM_URI = ROOT_URI + "bim";

    public static final int CORE_MODEL_ID = -1;

    protected static final OntDocumentManager DOC_MANAGER = new OntDocumentManager();
    protected static final OntModelSpec MODEL_SPEC_OWL = OntModelSpec.OWL_DL_MEM;
    protected static final OntModelSpec MODEL_SPEC_OWL_INF = OntModelSpec.OWL_DL_MEM_RDFS_INF;

    static {
        DOC_MANAGER.setProcessImports(false);
        DOC_MANAGER.addAltEntry(eu.h2020.symbIoTe.ontology.CoreInformationModel.NS, eu.h2020.symbIoTe.ontology.CoreInformationModel.SOURCE_PATH);
        MODEL_SPEC_OWL.setDocumentManager(DOC_MANAGER);
        MODEL_SPEC_OWL_INF.setDocumentManager(DOC_MANAGER);
    }

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

//    public static String getModelGraphURI(BigInteger modelId) {
//        return MODELS_GRAPH + "/" + modelId;
//    }
    public static String getPlatformGraphURI(String platformId) {
        return PLATFORMS_GRAPH + "/" + platformId;
    }

    public static String getResourceGraphURI(String resourceId) {
        return RESOURCES_GRAPH + "/" + resourceId;
    }

    public static String getInformationModelUri(String modelId) {
        return INFORMATION_MODEL_GRAPH + "/" + modelId;
    }

    public static String getFoiURI(String platformId, String foi) {
        return getPlatformGraphURI(platformId) + "/foi/" + foi;
    }

    public static String getLocationURI(String platformId, String location) {
        return getPlatformGraphURI(platformId) + "/location/" + location;
    }

    public static String findBIMPlatformPropertyUri(String property) throws PropertyNotFoundException {
        String uri = SymbioteModelsUtil.findInSymbioteCoreModels(property);
        log.debug("Found property in symbIoTe models: " + uri);
        return uri;
    }

//    public static String getMappingGraphURI(BigInteger mappingId) {
//        return MAPPING_GRAPH + "/" + mappingId;
//    }
    public static String getPlatformMetadata(String platformId, String modelId) {
        return "<" + getPlatformGraphURI(platformId) + "> <" + RDF.type + "> <" + PLATFORM + "> ." + "\n" //For now remove model reference
                //                + "<" + getPlatformGraphURI(platformId) + "> <" + USES + "> <" + getModelGraphURI(modelId) + "> ."
                ;
    }

    public static String getResourceMetadata(String serviceURI, String resourceUri) {
        return "<" + serviceURI + "> <" + MetaInformationModel.hasResource + "> <" + resourceUri + "> .";
    }

//    public static String getMappingMetadata(BigInteger modelId1, BigInteger modelId2, BigInteger mappingId) {
//        return "<" + getMappingGraphURI(mappingId) + "> <" + IS_A + "> <" + MAPPING + "> ." + "\n"
//                + "<" + getMappingGraphURI(mappingId) + "> <" + FROM + "> <" + getModelGraphURI(modelId1) + "> . \n"
//                + "<" + getMappingGraphURI(mappingId) + "> <" + TO + "> <" + getModelGraphURI(modelId2) + "> .";
//    }
    public static String modelAsString(Model model, RDFFormat format) {
        StringWriter writer = new StringWriter();
        model.write(writer, format.name());
        return writer.toString();
    }

    public static OntModel create(boolean withInference) {
        OntModel result = ModelFactory.createOntologyModel(
                withInference
                        ? MODEL_SPEC_OWL_INF
                        : MODEL_SPEC_OWL,
                ModelFactory.createDefaultModel());
        return result;
    }

    public static OntModel create(Model model, boolean includeImport, boolean withInference) {
        OntModel result = create(withInference);
        result.add(model);
        if (includeImport) {
            result.loadImports();
        }
        return result;
    }

    public static OntModel read(RDFInfo rdfInfo, boolean includeImport, boolean withInference) throws IOException {
        OntModel model = create(withInference);
        try (InputStream is = new ByteArrayInputStream(rdfInfo.getRdf().getBytes())) {
            model.read(is, null, rdfInfo.getRdfFormat().name());
        }
        if (includeImport) {
            model.loadImports();
        }
        return model;
    }

    public static OntModel withInf(OntModel model) {
        return ModelFactory.createOntologyModel(MODEL_SPEC_OWL_INF, model);
    }

    public static void loadImports(OntModel model) {
        DOC_MANAGER.loadImports(model);
    }

    public static void unloadImports(OntModel model) {
        model.listImportedOntologyURIs().forEach(x -> DOC_MANAGER.unloadImport(model, x));
    }

    public static Set<String> getOntologyDefinitions(OntModel model) {
        return model.listSubjectsWithProperty(RDF.type, OWL.Ontology)
                .toSet().stream()
                .map(x -> x.getURI())
                .collect(Collectors.toSet());
    }

    public static List<String> executeSelectWithResults(OntModel model, String query, String message) {
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            return StreamHelper.stream(qexec.execSelect())
                    .map(qs
                            -> message + StreamHelper.stream(qs.varNames())
                            .map(x -> x + ": " + qs.get(x))
                            .collect(Collectors.joining(",")))
                    .collect(Collectors.toList());
        }
    }
}
