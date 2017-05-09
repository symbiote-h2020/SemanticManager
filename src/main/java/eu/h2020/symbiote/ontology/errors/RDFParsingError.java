package eu.h2020.symbiote.ontology.errors;

/**
 * General error thrown in case of RDF generation error.
 *
 * Created by Szymon Mueller on 03/05/2017.
 */
public class RDFParsingError extends Exception {

    public RDFParsingError(String message) {
        super(message);
    }
}
