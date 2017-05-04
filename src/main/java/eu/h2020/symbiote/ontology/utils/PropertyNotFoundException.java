package eu.h2020.symbiote.ontology.utils;

import java.util.List;

/**
 * Exception informing that property could not be found in the model.
 *
 * Created by Szymon Mueller on 03/05/2017.
 */
public class PropertyNotFoundException extends Exception {

    private final String searchedModel;

    private final String propertyName;

    public PropertyNotFoundException( String propertyName, String searchedModel ) {
        super(String.format("Could not find property with name: %s in model %s",propertyName,searchedModel));
        this.propertyName = propertyName;
        this.searchedModel = searchedModel;
    }

    public String getSearchedModel() {
        return searchedModel;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
