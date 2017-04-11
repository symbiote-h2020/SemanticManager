package eu.h2020.symbiote.ontology;

import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.internal.PIMMetaModelDescription;
import eu.h2020.symbiote.core.model.InterworkingService;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.core.model.resources.MobileSensor;
import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.core.model.resources.StationarySensor;
import eu.h2020.symbiote.ontology.utils.CoreInformationModel;
import eu.h2020.symbiote.ontology.utils.OntologyHelper;
import eu.h2020.symbiote.ontology.validation.PIMInstanceValidationResult;
import eu.h2020.symbiote.ontology.validation.PIMMetaModelValidationResult;
import eu.h2020.symbiote.ontology.validation.ResourceInstanceValidationResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ext.com.google.common.reflect.Reflection;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class for handling validation and translation. All RDF-related tasks are redirected to this class for
 * execution and implementation.
 * <p>
 * Created by Szymon Mueller on 21/03/2017.
 */
public class SemanticManager {

    private static final Log log = LogFactory.getLog(SemanticManager.class);


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
        //TODO initialize
    }

    /**
     * Validates PIM meta model against symbIoTe core models: CIM & MIM.
     * <p>
     * Returns object representing validation result as well as POJO representing basic PIM meta model information.
     *
     * @param request Request containing RDF.
     * @return result of the meta model validation.
     */
    public PIMMetaModelValidationResult validatePIMMetaModel(RDFInfo request) {
        log.info("Validating PIM meta model " + request.getRdf().substring(0, 30) + " ... ");
        PIMMetaModelValidationResult result = new PIMMetaModelValidationResult();

        result.setModelValidated(request.getRdf());

        //TODO change with proper implementation
        result.setSuccess(true);
        result.setMessage("Validation successful");
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/core");
        PIMMetaModelDescription modelInfo = new PIMMetaModelDescription();
        modelInfo.setUri("http://www.symbiote-h2020.eu/ontology/platforms/1111");
        modelInfo.setRdf(request.getRdf());
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
        model.read(new ByteArrayInputStream(pimMetaModel.getRdf().getBytes()), null, pimMetaModel.getRdfFormat().toString());
        //TODO save meta model in rdf store

    }

    /**
     * Validates if the RDF passed as a parameter in the request is valid as an instance of a PIM.
     * Algorithm checks which PIM meta model is being used by the RDF and validates against that model.
     * Validation status, as well as information about the platform being validated, is returned.
     *
     * @param request Request containing RDF to be validated
     * @return Validation result as well as POJO representing the platform which was validated.
     */
    public PIMInstanceValidationResult validatePIMInstance(RDFInfo request) {
        log.info("Validating PIM instance " + request.getRdf().substring(0, 30) + " ... ");
        PIMInstanceValidationResult result = new PIMInstanceValidationResult();

        result.setModelValidated(request.getRdf());

        //TODO change with proper implementation
        result.setSuccess(true);
        result.setMessage("Validation successful");
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/platformA"); //Read from RDF what kind of model is being used, insert it here
        PIMInstanceDescription pimInstance = new PIMInstanceDescription();
        pimInstance.setId("1111");
        pimInstance.setLabels(Arrays.asList("PlatformA"));
        pimInstance.setComments(Arrays.asList("This is platform A"));
        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setInformationModelId("http://www.symbiote-h2020.eu/ontology/platformA"); //Same as a model it is validated agains
        interworkingService.setUrl("http://platforma.myhost.eu/myservice");

        pimInstance.setInterworkingServices(Arrays.asList(interworkingService));
        result.setObjectDescription(pimInstance);


        return result;
    }

    /**
     * Registers new PIM instance model in rdf store of Semantic Manager.This model is gonna be used to validate resources being registered for the platform.
     *
     * @param pimInstanceModel Information about platform, including RDF representing the platform.
     */
    public void registerNewPIMInstanceModel(PIMInstanceDescription pimInstanceModel) {
        log.info("Registering new PIM instance " + pimInstanceModel.toString());
        String pimLabel = pimInstanceModel.getLabels() != null && pimInstanceModel.getLabels().size() > 1 ? pimInstanceModel.getLabels().get(0) : null;
        if (pimLabel != null) {
            log.info("Registering new PIM instance " + pimLabel);
            if (pimInstanceModel.getRdf() != null && pimInstanceModel.getRdf().trim().length() > 0 && pimInstanceModel.getRdfFormat() != null) {
                Model model = ModelFactory.createDefaultModel();
                model.read(new ByteArrayInputStream(pimInstanceModel.getRdf().getBytes()), null, pimInstanceModel.getRdfFormat().toString());
            } else {
                log.error("Could not register PIM instance with empty ");
            }
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
        translatedDescription.setRdf("{}");
        translatedDescription.setRdfFormat(RDFFormat.JSONLD);

        result.setSuccess(true);
        result.setMessage("Validation and translation successful");
        result.setObjectDescription(translatedDescription);
        result.setModelValidated(translatedDescription.getRdf());
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/bim");

        return result;
    }

    /**
     * Validates if the RDF passed as a parameter in the request is valid RDF representing a resource of the platform.
     * Algorithm checks which PIM instance is being used by the RDF and validates against that model.
     * Validation status, as well as information about the platform being validated, is returned.
     *
     * @param request Request containing RDF to be validated
     * @return Validation result as well as POJO representing the platform which was validated.
     */
    public ResourceInstanceValidationResult validateResourcesInstance(RDFInfo request) {
        log.info("Validating Resource instance " + request.getRdf().substring(0, 30) + " ... ");
        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        result.setModelValidated(request.getRdf());

        //TODO change with proper implementation
        result.setSuccess(true);
        result.setMessage("Validation successful");
        result.setModelValidatedAgainst("http://www.symbiote-h2020.eu/ontology/platforms/1111"); //Set URI of the platform instance this resources are being registered to
        List<CoreResource> resources = new ArrayList<>();
        CoreResource resource1 = new CoreResource();
        resource1.setId("12345");
        resource1.setLabels(Arrays.asList("Resource1"));
        resource1.setComments(Arrays.asList("This is resource 1"));
        resource1.setInterworkingServiceURL("http://platforma.myhost.eu/myservice");
        resource1.setRdf("<>"); //Set RDF representing only this particular resource
        resource1.setRdfFormat(RDFFormat.JSONLD);

        resources.add(resource1);
        result.setObjectDescription(resources);

        return result;
    }

    /**
     * Validates description of resources of BIM-compliant platform and translates them into RDF.
     *
     * @param resources List of resources, for which RDF will be created.
     * @return Validation result, containing information about the resources and created RDF.
     */
    public ResourceInstanceValidationResult validateAndCreateBIMResourceToRDF(List<Resource> resources) {
        ResourceInstanceValidationResult result = new ResourceInstanceValidationResult();
        if (resources != null) {
            log.info("Validating and creating RDF for " + resources.size() + " resources");


            List<CoreResource> resourceList = new ArrayList<>();
            for (Resource resource : resources) {
                //Verify that instance translatedDescription has all fields to create RDF
                verifyCompleteBIMResourceDescription(resource);

                //Copy all meta-information about the platform to the response
                CoreResource translatedResource = new CoreResource();
                translatedResource.setLabels(resource.getLabels());
                translatedResource.setComments(resource.getComments());
                translatedResource.setInterworkingServiceURL(resource.getInterworkingServiceURL());
                translatedResource.setId(resource.getId());

                //TODO create RDF based on the description
                translatedResource.setRdf("{}");
                translatedResource.setRdfFormat(RDFFormat.JSONLD);

                resourceList.add(translatedResource);
            }

            result.setSuccess(true);
            result.setMessage("Validation and translation successful");
//            result.setObjectDescription(resourceList);
//            result.setModelValidated(translatedResource.getRdf());
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

    /**
     * Generates and returns RDF for the resource in specified format.
     *
     * @param resource Resource to be translated to RDF.
     * @param format Format of the output RDF.
     * @return String containing resource description in RDF.
     */
    private String generateRDFForResource( Resource resource, RDFFormat format ) {
        String result = null;
        log.debug("Generating model for resource " + resource.getId());
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        //Add general resource properties
        org.apache.jena.rdf.model.Resource modelResource = model.createResource(OntologyHelper.getResourceGraphURI(resource.getId()));
        //modelResource.addProperty(CoreInformationModel.RDF_TYPE,CoreInformationModel.CIM_RESOURCE)
        modelResource.addProperty(CoreInformationModel.CIM_ID,resource.getId()); //TODO this needs to be changed to cim:ID type
        for( String label: resource.getLabels() ) {
            modelResource.addProperty(CoreInformationModel.RDFS_LABEL,label);
        }
        for( String comment: resource.getComments() ) {
            modelResource.addProperty(CoreInformationModel.RDFS_COMMENT,comment);
        }
        if( resource instanceof MobileSensor ) {
            //Add observesProperties and locatedAt
        }
        if( resource instanceof StationarySensor ) {

        }




//        List<String> properties = resource.getObservedProperties();
//        org.apache.jena.rdf.model.Resource res = model.createResource();
//        for( String prop: properties ) {
//            res.addProperty(CoreInformationModel.RDFS_LABEL,prop);
//            res.addProperty(CoreInformationModel.RDFS_COMMENT,"");
//        }
//
//
//                .addProperty(MetaInformationModel.RDF_TYPE,CoreInformationModel.CIM_SENSOR)
//
//                .addProperty(CoreInformationModel.RDFS_LABEL,resource.getName())
//                .addProperty(CoreInformationModel.RDFS_COMMENT,resource.getDescription()!=null?resource.getDescription():"")
//                .addProperty(CoreInformationModel.CIM_LOCATED_AT,model.createResource(Ontology.getResourceGraphURI(resource.getId())+"/location"))
//                .addProperty(CoreInformationModel.CIM_OBSERVES,res);
//
//        Model locationModel = generateLocation(resource);
//        model.add(locationModel);
//
//        model.write(System.out,"TURTLE");


        return result;
    }

}
