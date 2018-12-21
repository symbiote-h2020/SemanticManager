package eu.h2020.symbiote.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.errors.RDFGenerationError;
import eu.h2020.symbiote.ontology.errors.RDFParsingError;
import eu.h2020.symbiote.ontology.utils.GenerationResult;
import eu.h2020.symbiote.ontology.utils.RDFGenerator;
import eu.h2020.symbiote.ontology.utils.RDFReader;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import eu.h2020.symbiote.ontology.validation.ValidationHelper;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Main class for handling validation and translation. All RDF-related tasks are
 * redirected to this class for execution and implementation.
 * <p>
 * Created by Szymon Mueller on 21/03/2017.
 */
@Component
public class SemanticManager {

    private static final Log log = LogFactory.getLog(SemanticManager.class);

    private static final RDFFormat DEFAULT_RDF_FORMAT = RDFFormat.JSONLD;

    private final RDFGenerator rdfGenerator;

    @Autowired
    public SemanticManager( RDFGenerator rdfGenerator ) {
        this.rdfGenerator = rdfGenerator;
    }

//    private static SemanticManager manager = null;
//
//    private SemanticManager() {
//
//    }
//
//    public static SemanticManager getManager() {
//        synchronized (SemanticManager.class) {
//            if (manager == null) {
//                log.info("Creating Semantic Manager");
//                manager = new SemanticManager();
//                //Trying to initialize semantic parts: load models into in-memory jena etc
//                manager.init();
//            }
//            return manager;
//        }
//    }
//
//    /**
//     * Initialize semantic database, used for validations and translations
//     */
//    private void init() {
//        log.info("Initializing Semantic Manager");
//    }

    /**
     * Validates PIM meta model against symbIoTe core models: CIM & MIM.
     * <p>
     * Returns object representing validation result as well as POJO
     * representing basic info about PIM meta model.
     *
     * @param request Request containing RDF.
     * @return result of the meta model validation.
     */
    public InformationModelValidationResult validatePIMMetaModel(InformationModel request) {
        log.info("Validating PIM meta model " + request.getRdf().substring(0, 30) + " ... ");
        InformationModelValidationResult result = new InformationModelValidationResult();

        /*                                    
            - check namespace not already in use? --> can not be done yet because no access to model storage
            - check all definitions are within namespace?
         */
        // 1. check for valid RDF
        OntModel pim;
        try {
            pim = ModelHelper.readModel(request, false, false);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("PIM could not be parsed! Reason: " + ex);
            return result;
        }
        // 2. check exactly one owl:Ontology
        Set<String> ontologyDefinitions = ModelHelper.getOntologyDefinitionsURI(pim);
        if (ontologyDefinitions.size() != 1) {
            result.setSuccess(false);
            result.setMessage("PIM must contain exactly one owl:Ontology");
            return result;
        }
        String ontologyURI = ontologyDefinitions.iterator().next();
        // 3. check imports core
        if (!ValidationHelper.checkImportsCIM(pim)) {
            result.setSuccess(false);
            result.setMessage("PIM must import CIM directly (using owl:imports)");
            return result;
        }
        // 4. check if any definitions were made in CIM namespace
        Set<String> resourcesDefinedInCIMNamespace = ValidationHelper.getDefinedResourcesInNamespace(pim, CIM.NS);
        if (!resourcesDefinedInCIMNamespace.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("PIM is not allowed to define Resources within the CIM namespace! Found resources: "
                    + System.lineSeparator()
                    + StringUtils.join(resourcesDefinedInCIMNamespace, System.lineSeparator()));
            return result;
        }

        // for all further steps we need inference and the imports to be loaded
        pim = ModelHelper.withInf(pim);
        try {
            ModelHelper.loadImports(pim);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("enabling inference on PIM failed! Reason: " + ex.getMessage());
            return result;
        }
        // 5. check only declared classes used
        Set<String> undefinedButUsedClasses = ValidationHelper.getUndefinedButUsedClasses(pim);
        if (undefinedButUsedClasses.size() > 0) {
            result.setSuccess(false);
            result.setMessage(String.format("PIM uses %d undefined classes: %s%s",
                    undefinedButUsedClasses.size(),
                    System.lineSeparator(),
                    undefinedButUsedClasses));
            return result;
        }
        // 6. use OWL profile validation provided by Jena
        // TODO fix CIM/BIM to pass OWL validation - to be done in R4
//        ValidityReport report = pim.validate();
//        if (!report.isClean()) {
//            result.setSuccess(false);
//            result.setMessage("PIM has OWL validation conflicts: "
//                    + StreamHelper.stream(report.getReports())
//                            .map(x -> x.toString())
//                            .collect(Collectors.joining(System.lineSeparator())));
//            return result;
//        }
        System.out.println("---> PIM has no validation errors");

        result.setSuccess(true);
        result.setMessage("Validation successful");
        result.setModelValidatedAgainst(CIM.getURI());
        result.setModelValidated(ontologyURI);
        InformationModel modelInfo = new InformationModel();

        modelInfo.setRdf(request.getRdf()); //Original one or modified one if there is some updates needed (unique id?)
        modelInfo.setRdfFormat(request.getRdfFormat());
        String modelId = String.valueOf(ObjectId.get());
        log.debug("Generating id for the ontology model: " + modelId);
//        modelInfo.setUri("http://www.symbiote-h2020.eu/ontology/pim/" + modelId);
        log.debug("Using passed uri of the model: " + request.getUri());
        modelInfo.setUri(request.getUri());
        modelInfo.setId(modelId);
        modelInfo.setOwner(request.getOwner());
        modelInfo.setName(request.getName());

        result.setObjectDescription(modelInfo);

        log.info("Validation ended with status: " + (result.isSuccess() ? "valid" : "not valid: " + result.getMessage()));

        return result;
    }

    /**
     * Registers new PIM meta model in rdf store of Semantic Manager. This model
     * is gonna be used to validate instances of platforms and it's resources.
     *
     * @param pimMetaModel Information about the PIM meta model, including model
     * containing RDF
     */
    public void registerNewPIMMetaModel(InformationModel pimMetaModel) {
        log.info("Registering new PIM meta model " + pimMetaModel.getUri());
        SymbioteModelsUtil.addModels(Arrays.asList(pimMetaModel));

    }

    /**
     * Delete PIM meta model in rdf store of Semantic Manager.
     *
     * @param pimMetaModel Information about the PIM meta model, including model
     * containing RDF
     */
    public void deletePIMMetaModel(InformationModel pimMetaModel) {
        log.info("Deleting PIM meta model " + pimMetaModel.getUri());
        SymbioteModelsUtil.removeModels(Arrays.asList(pimMetaModel));

    }

    /**
     * Modifies PIM meta model in rdf store of Semantic Manager.
     *
     * @param pimMetaModel Information about the PIM meta model, including model
     * containing RDF
     */
    public void modifyPIMMetaModel(InformationModel pimMetaModel) {
        log.info("Modifying PIM meta model " + pimMetaModel.getUri());
        SymbioteModelsUtil.modifyModels(Arrays.asList(pimMetaModel));

    }

//    /**
//     * Validates if the RDF passed as a parameter in the request is valid as an
//     * instance of a PIM. Algorithm checks which PIM meta model is being used by
//     * the RDF and validates against that model. Validation status, as well as
//     * information about the platform being validated, is returned.
//     *
//     * @param request Request containing RDF to be validated
//     * @return Validation result as well as POJO representing the platform which
//     * was validated.
//     */
//    @Deprecated
//    public PIMInstanceValidationResult validatePIMInstance(RDFInfo request) {
//        // NOT USED ANYMORE - just PIM meta model is validated (it contains also "PIM instance" part)
//        return null;
//
////        log.info("Validating PIM instance " + request.getRdf().substring(0, 30) + " ... ");
////        PIMInstanceValidationResult result = new PIMInstanceValidationResult();
////
////        result.setModelValidated(request.getRdf());
////
////        try {
////            PIMInstanceDescription pimInstance = RDFReader.readPlatformInstance(request);
////            result.setSuccess(true);
////            result.setMessage("Validation successful");
////            result.setModelValidatedAgainst(""); //Read from RDF what kind of model is being used, insert it here
////            result.setObjectDescription(pimInstance);
////        } catch (RDFParsingError rdfParsingError) {
////            rdfParsingError.printStackTrace();
////            result.setSuccess(false);
////            result.setMessage("Validation failed. Detailed message: " + rdfParsingError.getMessage());
////            result.setModelValidatedAgainst(""); //Read from RDF what kind of model is being used, insert it here
////            result.setObjectDescription(null);
////        }
////
////        return result;
//    }
//
////    /**
////     * Registers new PIM instance model in rdf store of Semantic Manager.This model is gonna be used to validate resources being registered for the platform.
////     *
////     * @param pimInstanceModel Information about platform, including RDF representing the platform.
////     */
////    public void registerNewPIMInstanceModel(PIMInstanceDescription pimInstanceModel) {
////        log.info("Registering new PIM instance " + pimInstanceModel.toString());
////        String pimLabel = pimInstanceModel.getLabels() != null && pimInstanceModel.getLabels().size() > 1 ? pimInstanceModel.getLabels().get(0) : null;
////        if (pimLabel != null) {
////            log.info("Registering new PIM instance " + pimLabel);
////            if (pimInstanceModel.getRdf() != null && pimInstanceModel.getRdf().trim().length() > 0 && pimInstanceModel.getRdfFormat() != null) {
////                Model model = ModelFactory.createDefaultModel();
////                try( StringReader reader = new StringReader( pimInstanceModel.getRdf() ) ) {
////                    model.read(reader, null, pimInstanceModel.getRdfFormat().toString());
////                }
////            } else {
////                log.error("Could not register PIM instance with empty ");
////            }
////        } else {
////            log.error("Could not find platform's label");
////        }
////    }
//    /**
//     * Registers new PIM instance model in rdf store of Semantic Manager.This
//     * model is gonna be used to validate resources being registered for the
//     * platform.
//     *
//     * @param pimInstanceModel Information about platform, including RDF
//     * representing the platform.
//     */
//    public void registerNewPIMInstanceModel(Platform pimInstanceModel) {
//        log.info("Registering new PIM instance " + pimInstanceModel.toString());
//        String pimLabels = pimInstanceModel.getName();
//        if (pimLabels != null) {
//            log.info("[NYI] Model for platform " + pimLabels + " will be implemented in R3");
////            log.info("Registering new PIM instance " + pimLabel);
////            if (pimInstanceModel.getRdf() != null && pimInstanceModel.getRdf().trim().length() > 0 && pimInstanceModel.getRdfFormat() != null) {
////                Model model = ModelFactory.createDefaultModel();
////                try( StringReader reader = new StringReader( pimInstanceModel.getRdf() ) ) {
////                    model.read(reader, null, pimInstanceModel.getRdfFormat().toString());
////                }
////            } else {
////                log.error("Could not register PIM instance with empty ");
////            }
//        } else {
//            log.error("Could not find platform's label");
//        }
//    }

    /**
     * Validates if the description provided as the parameter is enough to
     * create a BIM-compliant platform instance. Creates RDF description of the
     * BIM-compliant platform. RDF is created by using information in POJO
     * object passed as a parameter. Returns the validation result containing
     * newly
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
        Model rdf = rdfGenerator.generateRDFForPlatform(pimInstanceDescription);

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

    private static void checkAndCreateId(org.apache.jena.rdf.model.Resource resource) {
        if (resource.hasProperty(CIM.id)) {
            log.debug("Checking id existing in the resource : " + resource.getProperty(CIM.id).getObject().toString());
            RDFNode idNode = resource.getProperty(CIM.id).getObject();
            if (idNode.isLiteral()) {
                return;
            }
        }
        log.debug( "Id not present - generating id");
        resource.addLiteral(
                CIM.id,
                resource.getModel().createTypedLiteral(
                        String.valueOf(ObjectId.get()),
                        XSDDatatype.XSDstring));
    }

    /**
     * Validates if the RDF passed as a parameter in the request is valid RDF
     * representing resources of the platform. Algorithm checks which PIM
     * instance is being used by the RDF and validates against that model.
     * Validation status, as well as information about the resources read from
     * the model, is returned.
     *
     * @param request Request containing RDF to be validated
     * @return Validation result as well as list of resources which were found
     * in the rdf model.
     */
    public ResourceInstanceValidationResult validateResourcesInstance(ResourceInstanceValidationRequest request) {
        log.info("Validating Resource instance ... ");

//        if (!request.getDescriptionType().equals(DescriptionType.RDF)) {
//            log.fatal("Validate resource instance should only be used by RDF type of description");
//            throw new IllegalArgumentException("Validate resource instance should only be used by RDF type of description");
//        }
        /*
         need PIM to validate against!
        1. check valid RDF
        2. check only instances
        3. check classes of instances are present
        4. check multiplicity constraints of core predicates
         */
        ObjectMapper mapper = new ObjectMapper();
//        ResourceInstanceValidationRequest rdfRequest;
//        try {
//            rdfRequest = mapper.readValue(request.getBody(), ResourceInstanceValidationRequest.class);
//        } catch (IOException ex) {
//            String message = "error parsing request! Request body must contain object of type " + ResourceInstanceValidationRequest.class.getName();
//            log.fatal(message);
//            throw new IllegalArgumentException(message);
//        }

        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        result.setModelValidated(request.getRdf());

        OntModel instances = null;
        try {
            instances = ModelHelper.readModel(request.getRdf(), request.getRdfFormat(), false, false);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("instances could not be parsed! Reason: " + ex);
            return result;
        }

        // check contains classes
        if (instances.listClasses().hasNext()) {
            result.setSuccess(false);
            result.setMessage("instance data is not allowed to contain class definitions!");
            return result;
        }
        // check contains datatype properties
        if (instances.listDatatypeProperties().hasNext()) {
            result.setSuccess(false);
            result.setMessage("instance data is not allowed to contain datatype properties!");
            return result;
        }
        // check contains object properties
        if (instances.listObjectProperties().hasNext()) {
            result.setSuccess(false);
            result.setMessage("instance data is not allowed to contain datatype properties!");
            return result;
        }

        // check contains datatype properties
        if (!instances.listImportedOntologyURIs().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("instance data is not allowed to contain imports!");
            return result;
        }

        // load PIM
        Model loadedPIM = SymbioteModelsUtil.findInformationModelById(request.getInformationModelId());
        if (loadedPIM == null || loadedPIM.isEmpty()) {
            String message = "PIM with id '" + request.getInformationModelId() + "' could not be loaded";
            log.info(message);
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
        // from now on we need inference
        OntModel pim;
        try {
            pim = ModelHelper.asOntModel(loadedPIM, true, true);
        } catch (IOException ex) {
            result.setSuccess(false);
            result.setMessage("enabling inference on PIM failed! Reason: " + ex.getMessage());
            return result;
        }

        instances = ModelHelper.withInf(instances);

        Map<org.apache.jena.rdf.model.Resource, Model> rdfResources = ValidationHelper.sepearteResources(instances, pim);
        if (rdfResources.isEmpty()) {
            result.setSuccess(false);
            result.setMessage("provided RDF does not contain any resource information");
            return result;
        }
        Map<String, CoreResource> resources = new HashMap<>();
        StringBuilder instanceResults = new StringBuilder();
        log.debug("Parsing resource map, size: " + rdfResources.size());
        for (Map.Entry<org.apache.jena.rdf.model.Resource, Model> entry : rdfResources.entrySet()) {
            checkAndCreateId(entry.getKey());

            StringBuilder instanceResult = new StringBuilder();
            pim.addSubModel(entry.getValue());
//            ValidityReport report = pim.validate();
//            if (!report.isClean()) {
//                instanceResult.append("errors during owl validation" + System.lineSeparator());
//                instanceResult.append(
//                        StreamHelper.stream(report.getReports())
//                                .map(x -> x.toString())
//                                .collect(Collectors.joining(System.lineSeparator())));
//            }
            List<String> cardinalityViolations = ValidationHelper.checkCardinalityViolations(entry.getKey(), pim, entry.getValue());
            if (!cardinalityViolations.isEmpty()) {
                log.debug("Found cardinality validation errors: " + String.join(System.lineSeparator(), cardinalityViolations));
                instanceResult.append("errors during cardinality validation").append(System.lineSeparator());
                instanceResult.append(String.join(System.lineSeparator(), cardinalityViolations));
            } else {
                log.debug("Cardinality validation fine");
            }
            pim.removeSubModel(entry.getValue());
            // if resource has no error - create java representation for it
            if (instanceResult.length() == 0) {
                try {
                    log.debug("Creating core resource object");
                    CoreResource coreResource = RDFReader.createCoreResource(entry.getKey(), entry.getValue(), request.getRdfFormat());
                    if( coreResource != null ) {
                        log.debug("Core resource created: " + coreResource.getName() + " | id " + coreResource.getId());
                        resources.put(entry.getKey().getURI(), coreResource);
                    } else {
                        log.debug("Returned core resource is null");
                        instanceResult.append("Error creating resource object - resource is null").append(System.lineSeparator());
                    }
                } catch (RDFParsingError ex) {
                    instanceResult.append("error creating CoreResource: ").append(ex);
                }
            }
            if (instanceResult.length() > 0) {
                instanceResults.append(String.format("errors validating RDF for resource '%s':%s", entry.getKey().getURI(), System.lineSeparator()));
                instanceResults.append(instanceResult.toString()).append(System.lineSeparator());
            }
        }
        if (instanceResults.length() > 0) {
            result.setSuccess(false);
            result.setMessage("errors validating RDF for resources: " + System.lineSeparator() + String.join(System.lineSeparator(), instanceResults));
            return result;
        }
        result.setModelValidatedAgainst(ModelHelper.writeModel(pim, request.getRdfFormat()));
        result.setSuccess(true);
        log.debug("Resource parsing finished, found " + resources.size() + " resources");
        resources.values().stream().forEach( res -> res.setInterworkingServiceURL(request.getInterworkingServiceURL()));
        result.setObjectDescription(resources);
        return result;
    }

    /**
     * Validates description of resources of BIM-compliant platform and
     * translates them into RDF.
     *
     * @param resources Map of resources, for which RDF will be created.
     * @param cloudId Id of the cloud entity registering the resources - id of the platform or id of the ssp.
     * @param cloudIsSsp <code>true</code> if the cloud entity is a ssp, <code>false</code> if it is a platform
     *
     * @return Validation result, containing information about the resources and
     * created RDF.
     */
    public ResourceInstanceValidationResult validateAndCreateBIMResourceToRDF(Map<String, Resource> resources, String cloudId, boolean cloudIsSsp) throws IOException, PropertyNotFoundException {
        log.info("Validating and creating RDF for resources");
        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        if (resources != null) {
//            log.info("Validating and creating RDF for platform " + request.getPlatformId());

            Model completeModel = ModelFactory.createDefaultModel();

            boolean success = true;
            StringBuilder errorMessage = new StringBuilder();

            Map<String, CoreResource> resourceList = new HashMap<>();


            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Resource> resources = null;
//            resources = mapper.readValue(request.getBody(), new TypeReference<Map<String, Resource>>() {
//            });
            for (String resourcePairingId : resources.keySet()) {
                Resource resource = resources.get(resourcePairingId);

                //Verify that instance translatedDescription has all fields to create RDF
                try {
                    verifyCompleteBIMResourceDescription(resource);

                    //Copy all meta-information about the platform to the response
                    CoreResource translatedResource = new CoreResource();
                    translatedResource.setName(resource.getName());
                    translatedResource.setDescription(resource.getDescription());
                    translatedResource.setInterworkingServiceURL(resource.getInterworkingServiceURL());
                    translatedResource.setType(SymbioteModelsUtil.getTypeForResource(resource));

                    //TODO refactor generating IDs
                    if (resource.getId() == null || resource.getId().isEmpty()) {
                        translatedResource.setId(String.valueOf(ObjectId.get()));
                        resource.setId(translatedResource.getId());
                    } else {
                        translatedResource.setId(resource.getId());
                    }

                    //Generate the rdf for the resource and save it into CoreResource
                    GenerationResult generationResult = rdfGenerator.generateRDFForResource(resource, cloudId, cloudIsSsp );
                    completeModel.add(generationResult.getModel());
                    StringWriter stringWriter = new StringWriter();
                    generationResult.getModel().write(stringWriter, DEFAULT_RDF_FORMAT.toString());

                    translatedResource.setRdf(stringWriter.toString());
                    translatedResource.setRdfFormat(DEFAULT_RDF_FORMAT);

                    //Add rdfs to all subresources found during generation
                    for (String subresPairingId : generationResult.getResources().keySet()) {
                        CoreResource subres = generationResult.getResources().get(subresPairingId);
                        subres.setRdf(translatedResource.getRdf());
                        subres.setRdfFormat(translatedResource.getRdfFormat());
                        resourceList.put(subresPairingId, subres);
                    }

                    resourceList.put(resourcePairingId, translatedResource);

                } catch (IllegalArgumentException e) {
                    log.error("Error occurred during verifying resource: " + resource.getName(), e);
                    success = false;
                    errorMessage.append(e.getMessage() + "\n");
                } catch (RDFGenerationError e) {
                    log.error("Error occurred during rdf generation: " + resource.getName(), e);
                    success = false;
                    errorMessage.append(e.getMessage() + "\n");
                }
            }

            result.setSuccess(success);
            if (success) {
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
        if (resource.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
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
