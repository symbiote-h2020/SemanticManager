package eu.h2020.symbiote.ontology;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.model.Platform;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.errors.RDFGenerationError;
import eu.h2020.symbiote.ontology.errors.RDFParsingError;
import eu.h2020.symbiote.ontology.utils.GenerationResult;
import eu.h2020.symbiote.ontology.utils.RDFGenerator;
import eu.h2020.symbiote.ontology.utils.RDFReader;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class for handling validation and translation. All RDF-related tasks are redirected to this class for
 * execution and implementation.
 * <p>
 * Created by Szymon Mueller on 21/03/2017.
 */
public class SemanticManager {

    private static final Log log = LogFactory.getLog(SemanticManager.class);

    private static final RDFFormat DEFAULT_RDF_FORMAT = RDFFormat.JSONLD;

    private static SemanticManager manager = null;

    private SemanticManager() {

    }

    public static SemanticManager getManager() {
        synchronized (SemanticManager.class) {
            if (manager == null) {
                log.info("Creating Semantic Manager");
                manager = new SemanticManager();
                //Trying to initialize semantic parts: load models into in-memory jena etc
                manager.init();
            }
            return manager;
        }
    }

    /**
     * Initialize semantic database, used for validations and translations
     */
    private void init() {
        log.info("Initializing Semantic Manager");
    }

    /**
     * Validates PIM meta model against symbIoTe core models: CIM & MIM.
     * <p>
     * Returns object representing validation result as well as POJO representing basic info about PIM meta model.
     *
     * @param request Request containing RDF.
     * @return result of the meta model validation.
     */
    public PIMMetaModelValidationResult validatePIMMetaModel(RDFInfo request) {
        log.info("Validating PIM meta model " + request.getRdf().substring(0, 30) + " ... ");
        PIMMetaModelValidationResult result = new PIMMetaModelValidationResult();

        //TODO change with proper implementation
        result.setSuccess(true);
        result.setMessage("Validation successful");
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/core");
        result.setModelValidated("http://www.symbiote-h2020.eu/ontology/myModel1"); //URI of the model validated
        PIMMetaModelDescription modelInfo = new PIMMetaModelDescription();
        modelInfo.setUri("http://www.symbiote-h2020.eu/ontology/myModel1");
        modelInfo.setRdf(request.getRdf()); //Original one or modified one if there is some updates needed (unique id?)
        modelInfo.setRdfFormat(request.getRdfFormat());

        result.setObjectDescription(modelInfo);

        log.info("Validation ended with status: " + (result.isSuccess() ? "valid" : "not valid: " + result.getMessage()));

        return result;
    }

    /**
     * Registers new PIM meta model in rdf store of Semantic Manager. This model is gonna be used to validate instances of platforms and it's resources.
     *
     * @param pimMetaModel Information about the PIM meta model, including model containing RDF
     */
    public void registerNewPIMMetaModel(PIMMetaModelDescription pimMetaModel) {
        log.info("Registering new PIM meta model " + pimMetaModel.getUri());

        Model model = ModelFactory.createDefaultModel();
        try( StringReader reader = new StringReader( pimMetaModel.getRdf() ) ) {
            model.read(reader, null, pimMetaModel.getRdfFormat().toString());
        }
        //TODO save meta model in rdf store see jira SYM-339

    }

    /**
     * Validates if the RDF passed as a parameter in the request is valid as an instance of a PIM.
     * Algorithm checks which PIM meta model is being used by the RDF and validates against that model.
     * Validation status, as well as information about the platform being validated, is returned.
     *
     * @param request Request containing RDF to be validated
     * @return Validation result as well as POJO representing the platform which was validated.
     */
    @Deprecated
    public PIMInstanceValidationResult validatePIMInstance(RDFInfo request) {
        // NOT USED ANYMORE - just PIM meta model is validated (it contains also "PIM instance" part)
        return null;

//        log.info("Validating PIM instance " + request.getRdf().substring(0, 30) + " ... ");
//        PIMInstanceValidationResult result = new PIMInstanceValidationResult();
//
//        result.setModelValidated(request.getRdf());
//
//        try {
//            PIMInstanceDescription pimInstance = RDFReader.readPlatformInstance(request);
//            result.setSuccess(true);
//            result.setMessage("Validation successful");
//            result.setModelValidatedAgainst(""); //Read from RDF what kind of model is being used, insert it here
//            result.setObjectDescription(pimInstance);
//        } catch (RDFParsingError rdfParsingError) {
//            rdfParsingError.printStackTrace();
//            result.setSuccess(false);
//            result.setMessage("Validation failed. Detailed message: " + rdfParsingError.getMessage());
//            result.setModelValidatedAgainst(""); //Read from RDF what kind of model is being used, insert it here
//            result.setObjectDescription(null);
//        }
//
//        return result;
    }

//    /**
//     * Registers new PIM instance model in rdf store of Semantic Manager.This model is gonna be used to validate resources being registered for the platform.
//     *
//     * @param pimInstanceModel Information about platform, including RDF representing the platform.
//     */
//    public void registerNewPIMInstanceModel(PIMInstanceDescription pimInstanceModel) {
//        log.info("Registering new PIM instance " + pimInstanceModel.toString());
//        String pimLabel = pimInstanceModel.getLabels() != null && pimInstanceModel.getLabels().size() > 1 ? pimInstanceModel.getLabels().get(0) : null;
//        if (pimLabel != null) {
//            log.info("Registering new PIM instance " + pimLabel);
//            if (pimInstanceModel.getRdf() != null && pimInstanceModel.getRdf().trim().length() > 0 && pimInstanceModel.getRdfFormat() != null) {
//                Model model = ModelFactory.createDefaultModel();
//                try( StringReader reader = new StringReader( pimInstanceModel.getRdf() ) ) {
//                    model.read(reader, null, pimInstanceModel.getRdfFormat().toString());
//                }
//            } else {
//                log.error("Could not register PIM instance with empty ");
//            }
//        } else {
//            log.error("Could not find platform's label");
//        }
//    }


     /** Registers new PIM instance model in rdf store of Semantic Manager.This model is gonna be used to validate resources being registered for the platform.
            *
            * @param pimInstanceModel Information about platform, including RDF representing the platform.
            */
    public void registerNewPIMInstanceModel(Platform pimInstanceModel) {
        log.info("Registering new PIM instance " + pimInstanceModel.toString());
        String pimLabel = pimInstanceModel.getName();
        if (pimLabel != null) {
            log.info("[NYI] Model for platform " + pimLabel + " will be implemented in R3");
//            log.info("Registering new PIM instance " + pimLabel);
//            if (pimInstanceModel.getRdf() != null && pimInstanceModel.getRdf().trim().length() > 0 && pimInstanceModel.getRdfFormat() != null) {
//                Model model = ModelFactory.createDefaultModel();
//                try( StringReader reader = new StringReader( pimInstanceModel.getRdf() ) ) {
//                    model.read(reader, null, pimInstanceModel.getRdfFormat().toString());
//                }
//            } else {
//                log.error("Could not register PIM instance with empty ");
//            }
        } else {
            log.error("Could not find platform's label");
        }
    }

    /**
     * Validates if the description provided as the parameter is enough to create a BIM-compliant platform instance.
     * Creates RDF description of the BIM-compliant platform. RDF is created by using information in POJO object passed as a parameter.
     * Returns the validation result containing newly
     *
     * @param pimInstanceDescription
     * @return
     */
    public PIMInstanceValidationResult validateAndCreateBIMPlatformToRDF(PIMInstanceDescription pimInstanceDescription) throws IllegalArgumentException {
        log.info("Validating and translating BIM platform to RDF " + pimInstanceDescription.toString());
        PIMInstanceValidationResult result = new PIMInstanceValidationResult();
        PIMInstanceDescription translatedDescription = new PIMInstanceDescription();

        //Verify that instance translatedDescription has all fields to create RDF
        verifyCompleteBIMInstanceDescription(pimInstanceDescription);

        //Copy all meta-information about the platform to the response
        translatedDescription.setLabels(pimInstanceDescription.getLabels());
        translatedDescription.setComments(pimInstanceDescription.getComments());
        translatedDescription.setId(pimInstanceDescription.getId());
        translatedDescription.setInterworkingServices(pimInstanceDescription.getInterworkingServices());

        //Create RDF from meta-information about the platform
        //TODO create RDF representing BIM compliant platform
        Model rdf = RDFGenerator.generateRDFForPlatform(pimInstanceDescription);

        StringWriter stringWriter = new StringWriter();
        rdf.write(stringWriter, DEFAULT_RDF_FORMAT.toString());

        translatedDescription.setRdf(stringWriter.toString());
        translatedDescription.setRdfFormat(DEFAULT_RDF_FORMAT);

        result.setSuccess(true);
        result.setMessage("Validation and translation successful");
        result.setObjectDescription(translatedDescription);
        result.setModelValidated(translatedDescription.getRdf());
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/bim");

        return result;
    }

    /**
     * Validates if the RDF passed as a parameter in the request is valid RDF representing resources of the platform.
     * Algorithm checks which PIM instance is being used by the RDF and validates against that model.
     * Validation status, as well as information about the resources read from the model, is returned.
     *
     * @param request Request containing RDF to be validated
     * @return Validation result as well as list of resources which were found in the rdf model.
     */
    public ResourceInstanceValidationResult validateResourcesInstance(CoreResourceRegistryRequest request) throws IOException {
        log.info("Validating Resource instance ... ");

        if( !request.getDescriptionType().equals(DescriptionType.RDF) ) {
            log.fatal("Validate resource instance should only be used by RDF type of description");
            throw new IllegalArgumentException("Validate resource instance should only be used by RDF type of description");
        }

        ObjectMapper mapper = new ObjectMapper();
        RDFInfo rdfInfo = mapper.readValue(request.getBody(), RDFInfo.class);

        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        result.setModelValidated(rdfInfo.getRdf());

        //TODO perform general validation of the RDF

        Map<String,CoreResource> resources = null;
        try {
            resources = RDFReader.readResourceInstances(rdfInfo, request.getPlatformId());
            if (resources != null && resources.size() > 0) {
                result.setSuccess(true);
                result.setMessage("Validation successful");
                result.setModelValidatedAgainst("");
            } else {
                result.setSuccess(false);
                result.setMessage("RDF does not contain any resource information");
                result.setModelValidatedAgainst("");
            }
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
            result.setSuccess(false);
            result.setMessage("Validation failed: " + rdfParsingError.getMessage());
            result.setModelValidatedAgainst("");
        }

        result.setObjectDescription(resources);

        return result;
    }

    /**
     * Validates description of resources of BIM-compliant platform and translates them into RDF.
     *
     * @param request Request containing list of resources, for which RDF will be created.
     * @return Validation result, containing information about the resources and created RDF.
     */
    public ResourceInstanceValidationResult validateAndCreateBIMResourceToRDF(CoreResourceRegistryRequest request) throws IOException, PropertyNotFoundException {
        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        if (request != null) {
            log.info("Validating and creating RDF for platform " + request.getPlatformId());

            Model completeModel = ModelFactory.createDefaultModel();

            boolean success = true;
            StringBuilder errorMessage = new StringBuilder();

            Map<String,CoreResource> resourceList = new HashMap<>();
            if( !request.getDescriptionType().equals(DescriptionType.BASIC) ) {
                log.fatal("Validate and create should only be used by BASIC (JSON) type of description");
                throw new IllegalArgumentException("Validate and create should only be used by BASIC (JSON) type of description");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String,Resource> resources = null;
            resources = mapper.readValue(request.getBody(), new TypeReference<Map<String,Resource>>(){});
            for (String resourcePairingId : resources.keySet()) {
                Resource resource = resources.get(resourcePairingId);

                //Verify that instance translatedDescription has all fields to create RDF
                try {
                    verifyCompleteBIMResourceDescription(resource);

                    //Copy all meta-information about the platform to the response
                    CoreResource translatedResource = new CoreResource();
                    translatedResource.setLabels(resource.getLabels());
                    translatedResource.setComments(resource.getComments());
                    translatedResource.setInterworkingServiceURL(resource.getInterworkingServiceURL());
                    translatedResource.setType(SymbioteModelsUtil.getTypeForResource(resource));

                    //TODO refactor generating IDs
                    if( resource.getId() == null || resource.getId().isEmpty() ) {
                        translatedResource.setId(String.valueOf(ObjectId.get()));
                        resource.setId(translatedResource.getId());
                    } else {
                        translatedResource.setId(resource.getId());
                    }

                    //Generate the rdf for the resource and save it into CoreResource
                    GenerationResult generationResult = RDFGenerator.generateRDFForResource(resource,request.getPlatformId());
                    completeModel.add(generationResult.getModel());
                    StringWriter stringWriter = new StringWriter();
                    generationResult.getModel().write(stringWriter, DEFAULT_RDF_FORMAT.toString());

                    translatedResource.setRdf(stringWriter.toString());
                    translatedResource.setRdfFormat(DEFAULT_RDF_FORMAT);

                    //Add rdfs to all subresources found during generation
                    for( String subresPairingId: generationResult.getResources().keySet() ) {
                        CoreResource subres = generationResult.getResources().get(subresPairingId);
                        subres.setRdf(translatedResource.getRdf());
                        subres.setRdfFormat(translatedResource.getRdfFormat());
                        resourceList.put(subresPairingId, subres);
                    }

                    resourceList.put(resourcePairingId,translatedResource);

                } catch ( IllegalArgumentException e ) {
                    log.error("Error occurred during verifying resource: " + resource.getLabels(), e);
                    success = false;
                    errorMessage.append(e.getMessage()+"\n");
                } catch( RDFGenerationError e ) {
                    log.error("Error occurred during rdf generation: " + resource.getLabels(), e);
                    success = false;
                    errorMessage.append(e.getMessage()+"\n");
                }
            }

            result.setSuccess(success);
            if( success ) {
                result.setMessage("Validation and translation successful");
                result.setObjectDescription(resourceList);

                StringWriter stringWriter = new StringWriter();
                completeModel.write(stringWriter, DEFAULT_RDF_FORMAT.toString());
                result.setModelValidated(stringWriter.toString());
            } else {
                result.setMessage(errorMessage.toString());
            }

            result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/bim");
        }
        return result;
    }

    private void verifyCompleteBIMInstanceDescription(PIMInstanceDescription pimInstanceDescription) throws IllegalArgumentException {
        if (pimInstanceDescription.getLabels() == null) {
            throw new IllegalArgumentException("Label must not be null");
        }
        if (pimInstanceDescription.getLabels().size() == 0) {
            throw new IllegalArgumentException("Label must not be empty list");
        }
    }

    private void verifyCompleteBIMResourceDescription(Resource resource) throws IllegalArgumentException {
        if (resource.getLabels() == null) {
            throw new IllegalArgumentException("Label must not be null");
        }
        if (resource.getLabels().size() == 0) {
            throw new IllegalArgumentException("Label must not be empty list");
        }
        if (resource.getInterworkingServiceURL() == null) {
            throw new IllegalArgumentException("Interworking service URL of the resource must not be null");
        }
        if (resource.getInterworkingServiceURL().trim().isEmpty()) {
            throw new IllegalArgumentException("Interworking service URL must not be empty");
        }
        //TODO add more checks for each type of resource
    }

}
