package eu.h2020.symbiote.ontology.formathandlers;

import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.ontology.validation.PIMInstanceValidationResult;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.ByteArrayInputStream;

/**
 * Created by Mael on 16/03/2017.
 */
public class TurtleFormatHandler implements IRDFFormatHandler {
    @Override
    public RDFFormat getSupportedRDFFormat() {
        return RDFFormat.Turtle;
    }

    @Override
    public Model generateModel(String rdf) {

        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, getSupportedRDFFormat().toString());
        return model;
    }

    @Override
    public PIMInstanceValidationResult validatePIMInstanceModel(String pimInstance) {
        return null;
    }
}
