package eu.h2020.symbiote.ontology.validation;

import eu.h2020.symbiote.core.internal.PIMMetaModelDescription;

/**
 * Result of the validation of PIM meta model.
 *
 * Created by Szymon Mueller on 16/03/2017.
 */
public class PIMMetaModelValidationResult extends ValidationResult<PIMMetaModelDescription> {

    public PIMMetaModelValidationResult() {

    }

    public PIMMetaModelValidationResult(boolean success, String description, String modelToBeValidated, String modelValidatedAgainst, PIMMetaModelDescription objectDescription) {
        super(success, description, modelToBeValidated, modelValidatedAgainst, objectDescription);
    }

}
