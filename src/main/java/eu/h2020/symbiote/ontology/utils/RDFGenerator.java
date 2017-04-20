package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.model.Platform;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.core.model.resources.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.*;
import sun.reflect.Reflection;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for generating RDF models from BIM objects.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class RDFGenerator {

    private static final Log log = LogFactory.getLog(RDFGenerator.class);

    /**
     * Generates and returns RDF for the resource in specified format.
     *
     * @param resource Resource to be translated to RDF.
     * @param platformId
     * @return String containing resource description in RDF.
     */
    public static Model generateRDFForResource(Resource resource, String platformId) {
        log.debug("Generating model for resource " + resource.getId());
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        //Add general resource properties
        org.apache.jena.rdf.model.Resource modelResource = model.createResource(OntologyHelper.getResourceGraphURI(resource.getId()));
        //modelResource.addProperty(CoreInformationModel.RDF_TYPE,CoreInformationModel.CIM_RESOURCE)
//        modelResource.addProperty(CoreInformationModel.CIM_ID,resource.getId()); //TODO this needs to be changed to cim:ID type
        for( String label: resource.getLabels() ) {
            modelResource.addProperty(CoreInformationModel.RDFS_LABEL,label);
        }
        for( String comment: resource.getComments() ) {
            modelResource.addProperty(CoreInformationModel.RDFS_COMMENT,comment);
        }
        if( resource instanceof MobileSensor) {
            //Add observed properties and location
            List<String> observesProperty = ((MobileSensor) resource).getObservesProperty();
            String locatedAt = ((MobileSensor) resource).getLocatedAt();

            for(String property: observesProperty ) {
                modelResource.addProperty(CoreInformationModel.CIM_OBSERVES,OntologyHelper.getBIMPropertyURI(property));
            }
            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
        }
        if( resource instanceof StationarySensor) {
            //Add foi, observed properties and location
            String featureOfInterest = ((StationarySensor) resource).getFeatureOfInterest();
            List<String> observesProperty = ((StationarySensor) resource).getObservesProperty();
            String locatedAt = ((StationarySensor) resource).getLocatedAt();

            modelResource.addProperty(CoreInformationModel.CIM_FOI, OntologyHelper.getFoiURI(platformId,featureOfInterest));
            for(String property: observesProperty ) {
                modelResource.addProperty(CoreInformationModel.CIM_OBSERVES,OntologyHelper.getBIMPropertyURI(property));
            }
            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
        }
        if( resource instanceof Service) {
            //Add name, output parameter and input parameters
            String name = ((Service) resource).getName();
            Parameter outputParameter = ((Service) resource).getOutputParameter();
            List<InputParameter> inputParameters = ((Service) resource).getInputParameter();

            modelResource.addProperty(CoreInformationModel.CIM_NAME, name );
            modelResource.addProperty(CoreInformationModel.CIM_HAS_OUTPUT,
                    model.createResource().addProperty(CoreInformationModel.CIM_IS_ARRAY, String.valueOf(outputParameter.isArray()))
                            .addProperty(CoreInformationModel.CIM_DATATYPE,outputParameter.getDatatype()));
            addInputParametersToModelResource(model,modelResource,inputParameters);

        }
        if( resource instanceof ActuatingService) {
            //Add name, output parameter, input parameters, foi it acts on as well as properties it affects
            String name = ((ActuatingService) resource).getName();
            Parameter outputParameter = ((ActuatingService) resource).getOutputParameter();
            List<InputParameter> inputParameters = ((ActuatingService) resource).getInputParameter();
            String actsOn = ((ActuatingService) resource).getActsOn();
            List<String> affects = ((ActuatingService) resource).getAffects();


            modelResource.addProperty(CoreInformationModel.CIM_NAME, name );
            modelResource.addProperty(CoreInformationModel.CIM_HAS_OUTPUT,
                    model.createResource().addProperty(CoreInformationModel.CIM_IS_ARRAY, String.valueOf(outputParameter.isArray()))
                            .addProperty(CoreInformationModel.CIM_DATATYPE,outputParameter.getDatatype()));
            addInputParametersToModelResource(model,modelResource,inputParameters);
            modelResource.addProperty(CoreInformationModel.CIM_ACTS_ON,OntologyHelper.getFoiURI(platformId,actsOn));
            for( String affectedProperty: affects ) {
                modelResource.addProperty(CoreInformationModel.CIM_AFFECTS,OntologyHelper.getBIMPropertyURI(affectedProperty));
            }
        }
        if( resource instanceof Actuator ) {
            //Add location and capabilities, ie actuating services connected with this actuator
            String locatedAt = ((Actuator) resource).getLocatedAt();
            List<String> capabilities = ((Actuator) resource).getCapabilities();

            modelResource.addProperty(CoreInformationModel.CIM_LOCATED_AT,OntologyHelper.getLocationURI(platformId,locatedAt));
            for(String capability: capabilities) {
                //TODO to be eventually changed if capability should be referenced in actuator by "name" field instead of "id"
                modelResource.addProperty(CoreInformationModel.CIM_HAS_CAPABILITY,OntologyHelper.getResourceGraphURI(capability));
            }
        }

        StringWriter writer = new StringWriter();
        model.write(writer, RDFFormat.Turtle.toString());
        String result = writer.toString();
        log.debug("Generated following RDF: " + result );

        return model;
    }

    private static void addInputParametersToModelResource(Model model, org.apache.jena.rdf.model.Resource modelResource, List<InputParameter> inputParameters ) {
        for( InputParameter input: inputParameters ) {
            List<org.apache.jena.rdf.model.Resource> restrictionResources = new ArrayList<>();
            for( Restriction restriction: input.getRestrictions() ) {
                org.apache.jena.rdf.model.Resource restrictionResource = model.createResource();
                if( restriction instanceof RangeRestriction ) {
                    restrictionResource.addProperty(CoreInformationModel.CIM_MIN, String.valueOf(((RangeRestriction) restriction).getMin()))
                            .addProperty(CoreInformationModel.CIM_MAX, String.valueOf(((RangeRestriction) restriction).getMax()));
                }
                if( restriction instanceof LengthRestriction ) {
                    restrictionResource.addProperty(CoreInformationModel.CIM_MIN, String.valueOf((( LengthRestriction) restriction).getMin()))
                            .addProperty(CoreInformationModel.CIM_MAX, String.valueOf(((LengthRestriction) restriction).getMax()));
                }
                if( restriction instanceof EnumRestriction ) {
                    for( String enumValue: ((EnumRestriction) restriction).getValues()) {
                        restrictionResource.addProperty(CoreInformationModel.RDF_VALUE, enumValue);
                    }
                }
                restrictionResources.add(restrictionResource);
            }

            org.apache.jena.rdf.model.Resource inputResource = model.createResource().addProperty(CoreInformationModel.CIM_IS_ARRAY, String.valueOf(input.isArray()))
                    .addProperty(CoreInformationModel.CIM_DATATYPE, input.getDatatype())
                    .addProperty(CoreInformationModel.CIM_NAME, input.getName())
                    .addProperty(CoreInformationModel.CIM_MANDATORY, String.valueOf(input.isMandatory()));
            for(org.apache.jena.rdf.model.Resource restriction: restrictionResources) {
                inputResource.addProperty(CoreInformationModel.CIM_HAS_RESTRICTION,restriction);
            }
            modelResource.addProperty(CoreInformationModel.CIM_HAS_INPUT, inputResource);
        }
    }

    public static Model generateRDFForPlatform( Platform platform ) {
        Model model = ModelFactory.createDefaultModel();
        return model;
    }



}
