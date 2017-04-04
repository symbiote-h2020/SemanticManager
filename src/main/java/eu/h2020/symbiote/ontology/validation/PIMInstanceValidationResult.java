package eu.h2020.symbiote.ontology.validation;

import eu.h2020.symbiote.model.PIMInstanceDescription;
import eu.h2020.symbiote.model.PIMMetaModelDescription;

/**
 *
 * Created by Mael on 16/03/2017.
 */
public class PIMInstanceValidationResult extends ValidationResult<PIMInstanceDescription> {

    public PIMInstanceValidationResult() {

    }

    public PIMInstanceValidationResult(boolean success, String description, String modelToBeValidated, String modelValidatedAgainst, PIMInstanceDescription objectDescription) {
        super(success, description, modelToBeValidated, modelValidatedAgainst, objectDescription);
    }
}
