package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.model.cim.*;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.errors.RDFGenerationError;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import eu.h2020.symbiote.semantics.ontology.WGS84;
import eu.h2020.symbiote.utils.LocationInfo;
import eu.h2020.symbiote.utils.LocationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.*;

/**
 * Helper class for generating RDF models from BIM objects.
 * <p>
 * Created by Szymon Mueller on 12/04/2017.
 */
@Component
public class RDFGenerator {

    private static final Log log = LogFactory.getLog(RDFGenerator.class);

    private LocationManager locationManager;

    @Autowired
    public RDFGenerator( LocationManager locationManager ) {
        this.locationManager = locationManager;
    }

    /**
     * Generates and returns RDF for the resource in specified format.
     *
     * @param resource   Resource to be translated to RDF.
     * @param platformId
     * @return String containing resource description in RDF.
     */
    public GenerationResult generateRDFForResource(Resource resource, String platformId, boolean isSsp) throws PropertyNotFoundException, RDFGenerationError {
        log.debug("Generating model for resource " + resource.getId());
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");
        model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        model.setNsPrefix("qu", "http://purl.oclc.org/NET/ssnx/qu/quantity#");
        Map<String, CoreResource> resources = new HashMap<>();

        //Add general resource properties
        org.apache.jena.rdf.model.Resource modelResource = model.createResource(ModelHelper.getResourceURI(resource.getId()));
        modelResource.addProperty(CIM.id, resource.getId()); //TODO this needs to be changed to cim:ID type

        modelResource.addProperty(CIM.name, resource.getName());

        if (resource.getDescription() != null) {
            for (String description : resource.getDescription()) {
                modelResource.addProperty(CIM.description, description);
            }
        }
        if (resource instanceof MobileSensor) {
            modelResource.addProperty(RDF.type, CIM.MobileSensor);
            //Add observed properties and location
            List<String> observesProperty = ((MobileSensor) resource).getObservesProperty();
            Location locatedAt = ((MobileSensor) resource).getLocatedAt();
            if (observesProperty != null) {
                for (String property : observesProperty) {
                    modelResource.addProperty(CIM.observesProperty, model.createResource(findBIMPlatformPropertyUri(property)));
                }
            }

            addLocationToModelResource(model, modelResource, locatedAt, platformId, isSsp);
        }
        if (resource instanceof StationarySensor) {
            modelResource.addProperty(RDF.type, CIM.StationarySensor);
            //Add foi, observed properties and location
            FeatureOfInterest featureOfInterest = ((StationarySensor) resource).getFeatureOfInterest();
            List<String> observesProperty = ((StationarySensor) resource).getObservesProperty();
            Location locatedAt = ((StationarySensor) resource).getLocatedAt();

            if (observesProperty != null) {
                for (String property : observesProperty) {
                    modelResource.addProperty(CIM.observesProperty, model.createResource(findBIMPlatformPropertyUri(property)));
                }
            }

            addLocationToModelResource(model, modelResource, locatedAt, platformId, isSsp);
            addFoiToModelResource(model, modelResource, featureOfInterest);
        }
        if (resource instanceof Service) {
            modelResource.addProperty(RDF.type, CIM.Service);
            //Add name, output parameter and input parameters
            String name = ((Service) resource).getName();
            Datatype resultType = ((Service) resource).getResultType();
            List<Parameter> parameters = ((Service) resource).getParameters();

            modelResource.addProperty(CIM.name, name);
            addParametersToModelResource(model, modelResource, parameters);
            org.apache.jena.rdf.model.Resource resultTypeResource = createDatatypeModelResource(model, resultType);
            modelResource.addProperty(CIM.hasResultType, resultTypeResource);

        }

        if (resource instanceof Actuator) {
            modelResource.addProperty(RDF.type, CIM.Actuator);
            //Add location and capabilities, ie actuating services connected with this actuator
            Location locatedAt = ((Actuator) resource).getLocatedAt();
            List<Capability> capabilities = ((Actuator) resource).getCapabilities();

            addLocationToModelResource(model, modelResource, locatedAt, platformId, isSsp);
            addCapabilitiesToModelResource(model, modelResource, capabilities);
        }

        StringWriter writer = new StringWriter();
        model.write(writer, RDFFormat.Turtle.toString());
        String rdf = writer.toString();
        log.debug("Generated following RDF: " + rdf);

        GenerationResult result = new GenerationResult();
        result.setModel(model);
        result.setResources(resources);

        return result;
    }

    private void addParametersToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<Parameter> parameters) throws RDFGenerationError {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                List<org.apache.jena.rdf.model.Resource> restrictionResources = new ArrayList<>();
                if (parameter.getRestrictions() != null) {
                    for (Restriction restriction : parameter.getRestrictions()) {
                        org.apache.jena.rdf.model.Resource restrictionResource = model.createResource();
                        if (restriction instanceof RangeRestriction) {
                            restrictionResource.addProperty(RDF.type, CIM.RangeRestriction)
                                    .addProperty(CIM.min, String.valueOf(((RangeRestriction) restriction).getMin()))
                                    .addProperty(CIM.max, String.valueOf(((RangeRestriction) restriction).getMax()));
                        }
                        if (restriction instanceof LengthRestriction) {
                            restrictionResource.addProperty(RDF.type, CIM.LengthRestriction)
                                    .addProperty(CIM.min, String.valueOf(((LengthRestriction) restriction).getMin()))
                                    .addProperty(CIM.max, String.valueOf(((LengthRestriction) restriction).getMax()));
                        }
                        if (restriction instanceof EnumRestriction) {
                            restrictionResource.addProperty(RDF.type, CIM.EnumRestriction);
                            for (String enumValue : ((EnumRestriction) restriction).getValues()) {
                                restrictionResource.addProperty(RDF.value, enumValue);
                            }
                        }
                        if (restriction instanceof RegExRestriction) {
                            restrictionResource.addProperty(RDF.type, CIM.RegExRestriction)
                                    .addProperty(CIM.pattern, String.valueOf(((RegExRestriction) restriction).getPattern()));
                        }
                        if (restriction instanceof InstanceOfRestriction) {
                            restrictionResource.addProperty(RDF.type, CIM.InstanceOfRestriction)
                                    .addProperty(CIM.onlyInstancesOfClass, String.valueOf(((InstanceOfRestriction) restriction).getInstanceOfClass()))
                                    .addProperty(CIM.valueProperty, String.valueOf(((InstanceOfRestriction) restriction).getValueProperty()));
                        }
                        restrictionResources.add(restrictionResource);
                    }
                }

                org.apache.jena.rdf.model.Resource inputResource = model.createResource().addProperty(RDF.type, CIM.Parameter)
                        .addProperty(CIM.hasDatatype, createDatatypeModelResource(model, parameter.getDatatype()))
                        .addProperty(CIM.name, parameter.getName())
                        .addProperty(CIM.mandatory, String.valueOf(parameter.isMandatory()));
                for (org.apache.jena.rdf.model.Resource restriction : restrictionResources) {
                    inputResource.addProperty(CIM.hasRestriction, restriction);
                }
                modelResource.addProperty(CIM.hasParameter, inputResource);
            }
        }
    }

    //TODO add a lot of nullchecks - probably as seperate method
    private org.apache.jena.rdf.model.Resource createDatatypeModelResource(Model model, Datatype datatype) throws RDFGenerationError {
        org.apache.jena.rdf.model.Resource datatypeResource = model.createResource();

        if (datatype instanceof ComplexDatatype) {
            datatypeResource.addProperty(RDF.type, CIM.ComplexDatatype)
                    .addProperty(CIM.isArray, String.valueOf(datatype.isArray()));
            if (((ComplexDatatype) datatype).getBasedOnClass() != null) {
                datatypeResource.addProperty(CIM.basedOnClass, ((ComplexDatatype) datatype).getBasedOnClass());
            }
            if (((ComplexDatatype) datatype).getDataProperties() == null) {
                throw new RDFGenerationError("Complex data property must be provided for description to be valid");
            }
            if (((ComplexDatatype) datatype).getDataProperties().size() == 0) {
                throw new RDFGenerationError("Complex data property must not be empty for description to be valid");
            }
            for (DataProperty dataProperty : ((ComplexDatatype) datatype).getDataProperties()) {
                org.apache.jena.rdf.model.Resource dataPropertyResource = createDataPropertyModelResource(model, datatypeResource, dataProperty);
                datatypeResource.addProperty(CIM.hasDatatype, dataPropertyResource);
            }
        }
        if (datatype instanceof PrimitiveDatatype) {
            datatypeResource = model.createResource(((PrimitiveDatatype) datatype).getBaseDatatype());
            datatypeResource.addProperty(RDF.type, CIM.PrimitiveDatatype);

        }
        return datatypeResource;
    }

    //TODO refactor using interface
    private org.apache.jena.rdf.model.Resource createDataPropertyModelResource(Model model, org.apache.jena.rdf.model.Resource datatypeResource, DataProperty dataProperty) {
        org.apache.jena.rdf.model.Resource dataPropertyResource = model.createResource();
        String basedOn = "";
        if (dataProperty instanceof PrimitiveProperty) {
            basedOn = ((PrimitiveProperty) dataProperty).getBasedOnProperty();
        } else if (dataProperty instanceof ComplexProperty) {
            basedOn = ((ComplexProperty) dataProperty).getBasedOnProperty();
        }
        dataPropertyResource.addProperty(RDF.type, CIM.PrimitiveProperty)
                .addProperty(CIM.hasDatatype, datatypeResource);
        if( basedOn != null ) {
            dataPropertyResource.addProperty(CIM.basedOnProperty, basedOn);
        }

        return dataPropertyResource;
    }

    private void addLocationToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, Location location, String platformId, boolean isSsp) throws RDFGenerationError {

        verifyLocation(location);

        String locationURI = null;

//        LocationFinder locationFinder = LocationFinder.getSingleton();

        if (locationManager != null) {
//            try {
//                log.debug("Fetching for location URI... ");
//                locationURI = locationFinder.queryForLocationUri(location, platformId);
//                log.debug("Query returned following URI: " + locationURI);
//            } catch (Exception e) {
//                log.error("Could not contact search to retrieve location URI: " + e.getMessage(), e);
//            }
            if (location instanceof WGS84Location) {
                Optional<LocationInfo> foundLoc = locationManager.findExistingLocation(location.getName(),
                        platformId, ((WGS84Location) location).getLatitude(),
                        ((WGS84Location) location).getLongitude(), ((WGS84Location) location).getAltitude());
                if( foundLoc.isPresent() ) locationURI = foundLoc.get().getLocationUri();
            }
        }

        if (locationURI == null) {
            String locationId = ObjectId.get().toString();
            if (isSsp) {
                locationURI = ModelHelper.getSspURI(platformId) + "/location/" + locationId;
            } else {
                locationURI = ModelHelper.getPlatformURI(platformId) + "/location/" + locationId;
            }

            log.info("No existing locations have been found fulfilling criteria, created new location with ID: " + locationId + " and URI: <" + locationURI + ">");
        }
        org.apache.jena.rdf.model.Resource locationResource = model.createResource(locationURI);
//            locationResource.addProperty(RDF.type, CoreInformationModel.Location);
        if (location.getName() != null) {
            locationResource.addProperty(CIM.name, location.getName());
        }
        if (location.getDescription() != null) {
            for (String comment : location.getDescription()) {
                locationResource.addProperty(CIM.description, comment);
            }
        }
        if (location instanceof WGS84Location) {
            locationResource.addProperty(RDF.type, CIM.WGS84Location)
                    .addProperty(WGS84.lat, Double.valueOf(((WGS84Location) location).getLatitude()).toString())
                    .addProperty(WGS84.long_, Double.valueOf(((WGS84Location) location).getLongitude()).toString())
                    .addProperty(WGS84.alt, Double.valueOf(((WGS84Location) location).getAltitude()).toString());
        }
        if (location instanceof SymbolicLocation) {
            locationResource.addProperty(RDF.type, CIM.SymbolicLocation);
        }
        if (location instanceof WKTLocation) {
            locationResource.addProperty(RDF.type, CIM.WKTLocation)
                    .addProperty(RDF.value, ((WKTLocation) location).getValue());
        }

        modelResource.addProperty(CIM.locatedAt, locationResource);

    }

    private void addFoiToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, FeatureOfInterest featureOfInterest) throws PropertyNotFoundException {
        if (featureOfInterest != null) {
            org.apache.jena.rdf.model.Resource foiResource = model.createResource();
            foiResource.addProperty(RDF.type, CIM.FeatureOfInterest);

            if (featureOfInterest.getName() != null) {
                foiResource.addProperty(CIM.name, featureOfInterest.getName());
            } else {
                throw new IllegalArgumentException("Feature of interest must have not null name!");
            }
            if (featureOfInterest.getDescription() != null) {
                for (String foiComment : featureOfInterest.getDescription()) {
                    foiResource.addProperty(CIM.description, foiComment);
                }
            }

            if (featureOfInterest.getHasProperty() != null) {
                for (String foiProperty : featureOfInterest.getHasProperty()) {
                    foiResource.addProperty(CIM.hasProperty, model.createResource(findBIMPlatformPropertyUri(foiProperty)));
                }
            }

            modelResource.addProperty(CIM.hasFeatureOfInterest, foiResource);
        }
    }

    private void addCapabilitiesToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<Capability> capabilities) throws PropertyNotFoundException, RDFGenerationError {
//        Map<String,CoreResource> services = new HashMap<>();
        verifyCapabilities(capabilities);

        if (capabilities != null) {
            for (Capability capability : capabilities) {

                //1.0 - capablities dont have ids
//                if (capability.getId() == null || capability.getId().isEmpty()) {
//                    capability.setId(String.valueOf(ObjectId.get()));
//                }
                org.apache.jena.rdf.model.Resource capabilityResource = model.createResource();

                capabilityResource.addProperty(RDF.type, CIM.Capability);
                addParametersToModelResource(model, capabilityResource, capability.getParameters());
                addEffectToModelResource(model, capabilityResource, capability.getEffects());

                modelResource.addProperty(CIM.hasCapability, capabilityResource);

                //Additional, inner service with id - should be moved to handling device
//                CoreResource service = new CoreResource();
//                service.setType(CoreResourceType.ACTUATING_SERVICE);
//                service.setId(capability.getId());
//                service.setLabels(capability.getLabels());
//                service.setComments(capability.getComments());
//                service.setInterworkingServiceURL(capability.getInterworkingServiceURL());
//                services.put(capability.getId(),service);
            }
        }
//        return services;
    }


    private void addEffectToModelResource(Model model, org.apache.jena.rdf.model.Resource capabilityResource, List<Effect> effects) throws PropertyNotFoundException {
        if (effects != null) {
            for (Effect effect : effects) {
                org.apache.jena.rdf.model.Resource effectResource = model.createResource();
                effectResource.addProperty(RDF.type, CIM.Effect);
                addFoiToModelResource(model, effectResource, effect.getActsOn());
                for (String property : effect.getAffects()) {
                    //TODO add logic for different Information Models
                    effectResource.addProperty(CIM.affects, model.createResource(findBIMPlatformPropertyUri(property)));
                }
                capabilityResource.addProperty(CIM.hasEffect, effectResource);
            }
        }
    }

    //    private static Map<String,Service> addServicesToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<Service> services) throws PropertyNotFoundException, RDFGenerationError {
//        Map<String,CoreResource> result = new HashMap<>();
//        if( services != null ) {
//            for( Service service: services ) {
//                checkService(service);
//                if( service.getId() == null || service.getId().isEmpty() ) {
//                    service.setId(String.valueOf(ObjectId.get()));
//                }
//                org.apache.jena.rdf.model.Resource serviceResource = model.createResource(OntologyHelper.getResourceGraphURI(service.getId()));
//                serviceResource.addProperty(RDF.type, CoreInformationModel.CIM_SERVICE)
//                        .addProperty(CoreInformationModel.CIM_NAME, service.getName())
//                        .addProperty(CoreInformationModel.CIM_ID, service.getId())
//
//                        //
//                        .addProperty()
//
//                String name = service.getName();
//                Datatype outputDatatype = service.getResultType();
////        List<InputParameter> inputParameters = service.getInputParameter();
//                List<Parameter> parameters = service.getParameters();
////        FeatureOfInterest actsOn = service.getActsOn();
////        List<String> affects = service.getAffects();
//
//
//                modelResource.addProperty(CoreInformationModel.CIM_NAME, name);
//                modelResource.addProperty(CoreInformationModel.CIM_HAS_OUTPUT,
//                        model.createResource().addProperty(RDF.type, CoreInformationModel.CIM_PARAMETER)
//                                .addProperty(CoreInformationModel.CIM_IS_ARRAY, String.valueOf(outputParameter.isArray()))
//                                .addProperty(CoreInformationModel.CIM_DATATYPE, outputParameter.getDatatype()));
//                addInputParametersToModelResource(model, modelResource, inputParameters);
////            modelResource.addProperty(CoreInformationModel.CIM_ACTS_ON,OntologyHelper.getFoiURI(platformId,actsOn));
//                addFoiToModelResource(model, modelResource, actsOn);
//                for (String affectedProperty : affects) {
//                    modelResource.addProperty(CoreInformationModel.CIM_AFFECTS, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(affectedProperty)));
//                }
//            }
//        }
//        return result;
//    }
    public Model generateRDFForPlatform(PIMInstanceDescription platform) {
        log.debug("Generating model from platform");
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");
        model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        model.setNsPrefix("qu", "http://purl.oclc.org/NET/ssnx/qu/quantity#");
        // construct proper Platform entry
        org.apache.jena.rdf.model.Resource platformRes = model.createResource(ModelHelper.getPlatformURI(platform.getId()))
                .addProperty(RDF.type, OWL.Ontology)
                .addProperty(CIM.id, platform.getId());

        for (String comment : platform.getComments()) {
            platformRes.addProperty(CIM.description, comment);
        }
        for (String name : platform.getLabels()) {
            platformRes.addProperty(CIM.name, name);
        }

        for (InterworkingService service : platform.getInterworkingServices()) {
            Model serviceModel = ModelFactory.createDefaultModel();
            String serviceUri = generateInterworkingServiceUri(ModelHelper.getPlatformURI(platform.getId()), service.getUrl());

            serviceModel.createResource(serviceUri)
                    .addProperty(RDF.type, MIM.InterworkingService)
                    .addProperty(MIM.url, service.getUrl())
                    .addProperty(MIM.usesInformationModel, model.createResource()
                            .addProperty(CIM.id, service.getInformationModelId()));
            platformRes.addProperty(MIM.hasService, model.createResource(serviceUri));
            model.add(serviceModel);
        }

        model.write(System.out, "TURTLE");
        return model;
    }

    public String generateInterworkingServiceUri(String platformUri, String serviceUrl) {
        String cutServiceUrl = "";
        if (serviceUrl.startsWith("http://")) {
            cutServiceUrl = serviceUrl.substring(7);
        } else if (serviceUrl.startsWith("https://")) {
            cutServiceUrl = serviceUrl.substring(8);
        }
        return platformUri + "/service/" + cutServiceUrl;
    }

    private void verifyLocation(Location location) throws RDFGenerationError {
        if (location == null) {
            throw new RDFGenerationError("Location must not be null");
        }
        if (location.getName() == null) {
            throw new RDFGenerationError("Location must have not null name");
        }
    }

    private void verifyCapabilities(List<Capability> capabilities) throws RDFGenerationError {
        if (capabilities != null && capabilities.size() > 0) {
            for (Capability cap : capabilities) {
                if (cap == null) throw new RDFGenerationError("Capability must not be null");
                if (cap.getName() == null) throw new RDFGenerationError("Capability name must not be null");
                if (cap.getName().trim().length() == 0)
                    throw new RDFGenerationError("Capability name must not be empty");
            }
        }
    }


    private void checkService(Service service) throws RDFGenerationError {
        if (service == null) {
            throw new RDFGenerationError("Actuating service must not be null");
        }
        if (service.getResultType() == null) {
            throw new RDFGenerationError("Actuating service must have an output parameter");
        }
        if (service.getName() == null) {
            throw new RDFGenerationError("Actuating must have not null name");
        }
        if (service.getName() == null || service.getName().isEmpty()) {
            throw new RDFGenerationError("Actuating service must have a name property");
        }
    }

    private String findBIMPlatformPropertyUri(String property) throws PropertyNotFoundException {
        String uri = SymbioteModelsUtil.findInSymbioteCoreModels(property);
        log.debug("Found property in symbIoTe models: " + uri);
        return uri;
    }

}
