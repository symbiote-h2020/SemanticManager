package eu.h2020.symbiote.ontology.validation;

import eu.h2020.symbiote.core.internal.ResourceDescription;

import java.util.List;

/**
 * Result of the resource instance validation.
 *
 * Created by Szymon Mueller on 16/03/2017.
 */
public class ResourceInstanceValidationResult extends ValidationResult<List<ResourceDescription>> {

    public ResourceInstanceValidationResult() {

    }

    public ResourceInstanceValidationResult(boolean success, String description, String modelToBeValidated, String modelValidatedAgainst, List<ResourceDescription> objectDescription) {
        super(success, description, modelToBeValidated, modelValidatedAgainst, objectDescription);
    }
}
