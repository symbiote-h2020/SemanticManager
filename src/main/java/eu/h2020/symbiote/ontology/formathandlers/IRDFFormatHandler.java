package eu.h2020.symbiote.ontology.formathandlers;

import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.ontology.validation.PIMInstanceValidationResult;
import org.apache.jena.rdf.model.Model;

/**
 * Interface which must be implemented by handlers of RDFs in specific format.
 *
 * Created by Mael on 16/03/2017.
 */
public interface IRDFFormatHandler {

    /**
     * Returns which RDF format is handler by this implementation.
     * @return RDFFormat
     */
    public RDFFormat getSupportedRDFFormat();

    /**
     * Method for generating Model.
     */
    public Model generateModel(String rdf);

    public PIMInstanceValidationResult validatePIMInstanceModel(String pimInstance);


}
