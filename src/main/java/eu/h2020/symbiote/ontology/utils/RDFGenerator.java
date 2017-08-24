package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbIoTe.ontology.CoreInformationModel;
import eu.h2020.symbIoTe.ontology.MetaInformationModel;
import eu.h2020.symbIoTe.ontology.WGS84;
import eu.h2020.symbiote.cloud.model.data.parameter.InputParameter;
import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.model.*;
import eu.h2020.symbiote.core.model.Property;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.core.model.resources.Resource;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.errors.RDFGenerationError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.bson.types.ObjectId;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for generating RDF models from BIM objects.
 * <p>
 * Created by Szymon Mueller on 12/04/2017.
 */
public class RDFGenerator {

    private static final Log log = LogFactory.getLog(RDFGenerator.class);

    /**
     * Generates and returns RDF for the resource in specified format.
     *
     * @param resource   Resource to be translated to RDF.
     * @param platformId
     * @return String containing resource description in RDF.
     */
    public static GenerationResult generateRDFForResource(Resource resource, String platformId) throws PropertyNotFoundException, RDFGenerationError {
        log.debug("Generating model for resource " + resource.getId());
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        Map<String,CoreResource> resources = new HashMap<>();

        //Add general resource properties
        org.apache.jena.rdf.model.Resource modelResource = model.createResource(OntologyHelper.getResourceGraphURI(resource.getId()));
        modelResource.addProperty(RDF.type, CoreInformationModel.Resource);
        modelResource.addProperty(CoreInformationModel.id, resource.getId()); //TODO this needs to be changed to cim:ID type
        for (String label : resource.getLabels()) {
            modelResource.addProperty(RDFS.label, label);
        }
        for (String comment : resource.getComments()) {
            modelResource.addProperty(RDFS.comment, comment);
        }
        if (resource instanceof MobileSensor) {
            modelResource.addProperty(RDF.type, CoreInformationModel.MobileSensor);
            //Add observed properties and location
            List<String> observesProperty = ((MobileSensor) resource).getObservesProperty();
            Location locatedAt = ((MobileSensor) resource).getLocatedAt();
            if (observesProperty != null) {
                for (String property : observesProperty) {
                    modelResource.addProperty(CoreInformationModel.observesProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(property)));
                }
            }
//            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
            addLocationToModelResource(model, modelResource, locatedAt,platformId);
        }
        if (resource instanceof StationarySensor) {
            modelResource.addProperty(RDF.type, CoreInformationModel.StationarySensor);
            //Add foi, observed properties and location
            FeatureOfInterest featureOfInterest = ((StationarySensor) resource).getFeatureOfInterest();
            List<String> observesProperty = ((StationarySensor) resource).getObservesProperty();
            Location locatedAt = ((StationarySensor) resource).getLocatedAt();

            if (observesProperty != null) {
                for (String property : observesProperty) {
                    modelResource.addProperty(CoreInformationModel.observesProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(property)));
                }
            }
//            modelResource.addProperty(CoreInformationModel.CIM_HAS_FOI, OntologyHelper.getFoiURI(platformId,featureOfInterest));
//            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
            addLocationToModelResource(model, modelResource, locatedAt,platformId);
            addFoiToModelResource(model, modelResource, featureOfInterest);
        }
//        if (resource instanceof ActuatingService) {
//            addActuatingServiceToModelResource(model, modelResource, (ActuatingService) resource);
        if (resource instanceof Service) {
            modelResource.addProperty(RDF.type, CoreInformationModel.Service);
            //Add name, output parameter and input parameters
            String name = ((Service) resource).getName();
            Datatype resultType = ((Service) resource).getResultType();
            List<Parameter> parameters = ((Service) resource).getParameters();

            modelResource.addProperty(CoreInformationModel.name, name);
            addParametersToModelResource(model,modelResource,parameters);
            org.apache.jena.rdf.model.Resource resultTypeResource = createDatatypeModelResource(model, resultType);

//            modelResource.addProperty(CoreInformationModel.CIM_HAS_RESULT_TYPE, resultTypeResource)
//                    .addProperty()
//                    model.createResource().addProperty(RDF.type, CoreInformationModel.CIM_PARAMETER)
//                            .addProperty(CoreInformationModel.CIM_IS_ARRAY, String.valueOf(outputParameter.isArray()))
//                            .addProperty(CoreInformationModel.CIM_DATATYPE, outputParameter.getDatatype()));
//            addInputParametersToModelResource(model, modelResource, inputParameters);
        }

        if (resource instanceof Actuator) {
            modelResource.addProperty(RDF.type, CoreInformationModel.Actuator);
            //Add location and capabilities, ie actuating services connected with this actuator
            Location locatedAt = ((Actuator) resource).getLocatedAt();
            List<Capability> capabilities = ((Actuator) resource).getCapabilities();

//            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
            addLocationToModelResource(model, modelResource, locatedAt,platformId);
            addCapabilitiesToModelResource(model, modelResource, capabilities);
//            log.debug("Found " + newRes.size() + " subresources (services) of actuator");
//            resources.putAll(newRes);
        }
//        if (resource instanceof MobileDevice) {
//            modelResource.addProperty(RDF.type, CoreInformationModel.CIM_ACTUATOR);
//            modelResource.addProperty(RDF.type, CoreInformationModel.CIM_MOBILE);
//            Location locatedAt = ((MobileDevice) resource).getLocatedAt();
//            List<ActuatingService> capabilities = ((MobileDevice) resource).getCapabilities();
//
//            addLocationToModelResource(model, modelResource, locatedAt,platformId);
//            Map<String,CoreResource> newRes = addCapabilitiesToModelResource(model, modelResource, capabilities);
//            log.debug("Found " + newRes.size() + " subresources (services) of actuator");
//            resources.putAll(newRes);
//
//            //Add observed properties and location
//            List<String> observesProperty = ((MobileDevice) resource).getObservesProperty();
//
//            if (observesProperty != null) {
//                for (String property : observesProperty) {
//                    modelResource.addProperty(CoreInformationModel.observesProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(property)));
//                }
//            }
//        }
//        if (resource instanceof StationaryDevice) {
//            modelResource.addProperty(RDF.type, CoreInformationModel.CIM_ACTUATOR);
//            modelResource.addProperty(RDF.type, CoreInformationModel.CIM_STATIONARY);
//            //Add location and capabilities, ie actuating services connected with this actuator
//            Location locatedAt = ((StationaryDevice) resource).getLocatedAt();
//            List<ActuatingService> capabilities = ((StationaryDevice) resource).getCapabilities();
//
//            addLocationToModelResource(model, modelResource, locatedAt,platformId);
//            Map<String,CoreResource> newRes = addCapabilitiesToModelResource(model, modelResource, capabilities);
//            log.debug("Found " + newRes.size() + " subresources (services) of actuator");
//            resources.putAll(newRes);
//
//            //Add foi, observed properties and location
//            FeatureOfInterest featureOfInterest = ((StationaryDevice) resource).getFeatureOfInterest();
//            List<String> observesProperty = ((StationaryDevice) resource).getObservesProperty();
//
//
//            if (observesProperty != null) {
//                for (String property : observesProperty) {
//                    modelResource.addProperty(CoreInformationModel.observesProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(property)));
//                }
//            }
//            addFoiToModelResource(model, modelResource, featureOfInterest);
//        }

        StringWriter writer = new StringWriter();
        model.write(writer, RDFFormat.Turtle.toString());
        String rdf = writer.toString();
        log.debug("Generated following RDF: " + rdf);

        GenerationResult result = new GenerationResult();
        result.setModel(model);
        result.setResources(resources);

        return result;
    }

    private static void addParametersToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<Parameter> parameters) {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                List<org.apache.jena.rdf.model.Resource> restrictionResources = new ArrayList<>();
                for (Restriction restriction : parameter.getRestrictions()) {
                    org.apache.jena.rdf.model.Resource restrictionResource = model.createResource();
                    if (restriction instanceof RangeRestriction) {
                        restrictionResource.addProperty(RDF.type, CoreInformationModel.RangeRestriction)
                                .addProperty(CoreInformationModel.min, String.valueOf(((RangeRestriction) restriction).getMin()))
                                .addProperty(CoreInformationModel.max, String.valueOf(((RangeRestriction) restriction).getMax()));
                    }
                    if (restriction instanceof LengthRestriction) {
                        restrictionResource.addProperty(RDF.type, CoreInformationModel.LengthRestriction)
                                .addProperty(CoreInformationModel.min, String.valueOf(((LengthRestriction) restriction).getMin()))
                                .addProperty(CoreInformationModel.max, String.valueOf(((LengthRestriction) restriction).getMax()));
                    }
                    if (restriction instanceof EnumRestriction) {
                        restrictionResource.addProperty(RDF.type, CoreInformationModel.EnumRestriction);
                        for (String enumValue : ((EnumRestriction) restriction).getValues()) {
                            restrictionResource.addProperty(RDF.value, enumValue);
                        }
                    }
                    if (restriction instanceof RegExRestriction) {
                        restrictionResource.addProperty(RDF.type, CoreInformationModel.RegExRestriction)
                                .addProperty(CoreInformationModel.pattern,String.valueOf(((RegExRestriction) restriction).getPattern()));
                    }
                    if (restriction instanceof InstanceOfRestriction) {
                        restrictionResource.addProperty(RDF.type, CoreInformationModel.InstanceOfRestriction)
                                .addProperty(CoreInformationModel.onlyInstancesOfClass,String.valueOf(((InstanceOfRestriction) restriction).getInstanceOfClass()))
                                .addProperty(CoreInformationModel.valueProperty,String.valueOf(((InstanceOfRestriction) restriction).getValueProperty()));
                    }
                    restrictionResources.add(restrictionResource);
                }

                org.apache.jena.rdf.model.Resource inputResource = model.createResource().addProperty(RDF.type, eu.h2020.symbIoTe.ontology.CoreInformationModel.Parameter)
                        .addProperty(CoreInformationModel.hasDatatype, createDatatypeModelResource(model,parameter.getDatatype()))
                        .addProperty(CoreInformationModel.name, parameter.getName())
                        .addProperty(CoreInformationModel.mandatory, String.valueOf(parameter.isMandatory()));
                for (org.apache.jena.rdf.model.Resource restriction : restrictionResources) {
                    inputResource.addProperty(CoreInformationModel.hasRestriction, restriction);
                }
                modelResource.addProperty(CoreInformationModel.hasParameter, inputResource);
            }
        }
    }

    private static org.apache.jena.rdf.model.Resource createDatatypeModelResource( Model model, Datatype datatype ) {
        org.apache.jena.rdf.model.Resource datatypeResource = model.createResource();

        if( datatype instanceof ComplexDatatype ) {
            datatypeResource.addProperty(RDF.type, CoreInformationModel.ComplexDatatype)
                    .addProperty(CoreInformationModel.isArray, String.valueOf(datatype.isArray()))
                    .addProperty(CoreInformationModel.basedOnClass, ((ComplexDatatype) datatype).getBasedOnClass() );
            for( DataProperty dataProperty: ((ComplexDatatype) datatype).getDataProperties() ) {
                org.apache.jena.rdf.model.Resource dataPropertyResource = createDataPropertyModelResource(model, datatypeResource, dataProperty);
                datatypeResource.addProperty(CoreInformationModel.hasDatatype, dataPropertyResource);
            }
        }
        if( datatype instanceof RdfsDatatype ) {
            datatypeResource = model.createResource(((RdfsDatatype) datatype).getDatatypeName());
            datatypeResource.addProperty(RDF.type, RDFS.Datatype);
        }
        return datatypeResource;
    }


    //TODO refactor using interface
    private static org.apache.jena.rdf.model.Resource createDataPropertyModelResource(Model model, org.apache.jena.rdf.model.Resource datatypeResource, DataProperty dataProperty ) {
        org.apache.jena.rdf.model.Resource dataPropertyResource = model.createResource();
        String basedOn = "";
        if( dataProperty instanceof PrimitiveProperty ) {
            basedOn = ((PrimitiveProperty) dataProperty).getBasedOnProperty();
        } else if ( dataProperty instanceof ComplexProperty ) {
            basedOn = ((ComplexProperty) dataProperty).getBasedOnProperty();
        }
            dataPropertyResource.addProperty(RDF.type, CoreInformationModel.PrimitiveProperty)
                    .addProperty(CoreInformationModel.hasDatatype, datatypeResource)
                    .addProperty(CoreInformationModel.basedOnProperty, basedOn);

        return dataPropertyResource;
    }

    private static void addLocationToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, Location location, String platformId) {
        if (location != null) {
            String locationURI = null;

            LocationFinder locationFinder = LocationFinder.getSingleton();
            if( locationFinder != null ) {
                try {
                    log.debug("Fetching for location URI... ");
                    locationURI = locationFinder.queryForLocationUri(location, platformId);
                    log.debug("Query returned following URI: " + locationURI);
                } catch( Exception e ) {
                    log.error("Could not contact search to retrieve location URI: " + e.getMessage(), e);
                }
            }

            if( locationURI == null ) {
                String locationId = ObjectId.get().toString();
                locationURI = OntologyHelper.getLocationURI(platformId, locationId);
                log.info("No existing locations have been found fulfilling criteria, created new location with ID: " + locationId + " and URI: <" + locationURI + ">" );
            }
            org.apache.jena.rdf.model.Resource locationResource = model.createResource(locationURI);
            locationResource.addProperty(RDF.type, CoreInformationModel.Location);
            for( String label: location.getLabels() ) {
                locationResource.addProperty(RDFS.label, label);
            }
            for( String comment: location.getComments() ) {
                locationResource.addProperty(RDFS.comment, comment);
            }
            if (location instanceof WGS84Location) {
                locationResource.addProperty(RDF.type, CoreInformationModel.WGS84Location)
                        .addProperty(WGS84.lat, Double.valueOf(((WGS84Location) location).getLatitude()).toString())
                        .addProperty(WGS84.long_, Double.valueOf(((WGS84Location) location).getLongitude()).toString())
                        .addProperty(WGS84.alt, Double.valueOf(((WGS84Location) location).getAltitude()).toString());
            }
            if (location instanceof SymbolicLocation) {
                locationResource.addProperty(RDF.type, CoreInformationModel.SymbolicLocation);
            }
            if (location instanceof WKTLocation) {
                locationResource.addProperty(RDF.type, CoreInformationModel.WKTLocation)
                        .addProperty(RDF.value, ((WKTLocation) location).getValue());
            }

            modelResource.addProperty(CoreInformationModel.locatedAt, locationResource);
        }
    }

    private static void addFoiToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, FeatureOfInterest featureOfInterest) throws PropertyNotFoundException {
        if (featureOfInterest != null) {
            org.apache.jena.rdf.model.Resource foiResource = model.createResource();
            foiResource.addProperty(RDF.type, CoreInformationModel.FeatureOfInterest);

            if (featureOfInterest.getLabels() != null && featureOfInterest.getLabels().size() > 0) {
                for (String foiLabel : featureOfInterest.getLabels()) {
                    foiResource.addProperty(RDFS.label, foiLabel);
                }
            } else {
                throw new IllegalArgumentException(featureOfInterest.getLabels() == null ? "Feature of interest must have not null labels!" : "Feature of interest must have at least one label!");
            }
            if (featureOfInterest.getComments() != null) {
                for (String foiComment : featureOfInterest.getComments()) {
                    foiResource.addProperty(RDFS.comment, foiComment);
                }
            }

            if (featureOfInterest.getHasProperty() != null) {
                for (String foiProperty : featureOfInterest.getHasProperty()) {
                    foiResource.addProperty(CoreInformationModel.hasProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(foiProperty)));
                }
            }

            modelResource.addProperty(CoreInformationModel.hasFeatureOfInterest, foiResource);
        }
    }

    private static void addCapabilitiesToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<Capability> capabilities) throws PropertyNotFoundException, RDFGenerationError {
//        Map<String,CoreResource> services = new HashMap<>();
        if (capabilities != null) {
            for (Capability capability : capabilities) {

                //1.0 - capablities dont have ids
//                if (capability.getId() == null || capability.getId().isEmpty()) {
//                    capability.setId(String.valueOf(ObjectId.get()));
//                }

                org.apache.jena.rdf.model.Resource capabilityResource = model.createResource();

                capabilityResource.addProperty(RDF.type, CoreInformationModel.Capability);
                addParametersToModelResource(model, capabilityResource,capability.getParameters());
                addEffectToModelResource(model,capabilityResource,capability.getEffects());

                modelResource.addProperty(CoreInformationModel.hasCapability, capabilityResource);

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

    private static void addEffectToModelResource(Model model, org.apache.jena.rdf.model.Resource capabilityResource, List<Effect> effects) throws PropertyNotFoundException {
        if( effects != null ) {
            for( Effect effect: effects ) {
                org.apache.jena.rdf.model.Resource effectResource = model.createResource();
                effectResource.addProperty(RDF.type, CoreInformationModel.Effect);
                addFoiToModelResource(model,effectResource,effect.getActsOn());
                for( String property: effect.getAffects()) {
                    //TODO add logic for different Information Models
                    effectResource.addProperty(CoreInformationModel.hasProperty, model.createResource(OntologyHelper.findBIMPlatformPropertyUri(property)));
                }
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


    public static Model generateRDFForPlatform(PIMInstanceDescription platform) {
        log.debug("Generating model from platform");
        Model model = ModelFactory.createDefaultModel();
        // construct proper Platform entry
        org.apache.jena.rdf.model.Resource platformRes = model.createResource(OntologyHelper.getPlatformGraphURI(platform.getId()))
                .addProperty(RDF.type, OWL.Ontology)
                .addProperty(CoreInformationModel.id, platform.getId());

        for (String comment : platform.getComments()) {
            platformRes.addProperty(RDFS.comment, comment);
        }
        for (String name : platform.getLabels()) {
            platformRes.addProperty(RDFS.label, name);
        }

        for (InterworkingService service : platform.getInterworkingServices()) {
            Model serviceModel = ModelFactory.createDefaultModel();
            String serviceUri = generateInterworkingServiceUri(OntologyHelper.getPlatformGraphURI(platform.getId()), service.getUrl());

            serviceModel.createResource(serviceUri)
                    .addProperty(RDF.type, MetaInformationModel.InterworkingService)
                    .addProperty(MetaInformationModel.url, service.getUrl())
                    .addProperty(MetaInformationModel.usesInformationModel, model.createResource()
                            .addProperty(CoreInformationModel.id, service.getInformationModelId()));
            platformRes.addProperty(MetaInformationModel.hasService, model.createResource(serviceUri));
            model.add(serviceModel);
        }

        model.write(System.out, "TURTLE");
        return model;
    }


    public static String generateInterworkingServiceUri(String platformUri, String serviceUrl) {
        String cutServiceUrl = "";
        if (serviceUrl.startsWith("http://")) {
            cutServiceUrl = serviceUrl.substring(7);
        } else if (serviceUrl.startsWith("https://")) {
            cutServiceUrl = serviceUrl.substring(8);
        }
        return platformUri + "/service/" + cutServiceUrl;
    }


    private static void checkService(Service service) throws RDFGenerationError {
        if (service == null) {
            throw new RDFGenerationError("Actuating service must not be null");
        }
        if (service.getResultType() == null) {
            throw new RDFGenerationError("Actuating service must have an output parameter");
        }
        if (service.getLabels() == null || service.getLabels().size() == 0) {
            throw new RDFGenerationError("Actuating must have at least one label");
        }
        if (service.getName() == null || service.getName().isEmpty()) {
            throw new RDFGenerationError("Actuating service must have a name property");
        }
    }

}
