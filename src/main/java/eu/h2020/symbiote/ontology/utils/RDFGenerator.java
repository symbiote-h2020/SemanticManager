package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.model.Platform;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.resources.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.StringWriter;

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
     * @return String containing resource description in RDF.
     */
    public static Model generateRDFForResource(Resource resource ) {
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
        //TODO provide translation for specific types of sensors
        if( resource instanceof MobileSensor) {
            //Add observed properties and location
        }
        if( resource instanceof StationarySensor) {
            //Add foi, observed properties and location
        }
        if( resource instanceof Service) {
            //Add name, output parameter and input parameters
        }
        if( resource instanceof ActuatingService) {
            //Add name, output parameter, input parameters, foi it acts on as well as properties it affects
        }
        if( resource instanceof Actuator ) {
            //Add location and capabilities, ie actuating services connected with this actuator
        }

        StringWriter writer = new StringWriter();
        model.write(writer, RDFFormat.Turtle.toString());
        String result = writer.toString();
        log.debug("Generated following RDF: " + result );

        return model;
    }

    public static Model generateRDFForPlatform( Platform platform ) {
        Model model = ModelFactory.createDefaultModel();



        return model;
    }
}
