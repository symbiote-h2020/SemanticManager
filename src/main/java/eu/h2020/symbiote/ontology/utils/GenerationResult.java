package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.model.internal.CoreResource;
import org.apache.jena.rdf.model.Model;

import java.util.List;
import java.util.Map;

/**
 * Created by Szymon Mueller on 24/05/2017.
 */
public class GenerationResult {

    private Model model;

    //Contains list of additional sub-resources found for this resource.
    private Map<String,CoreResource> resources;

    public GenerationResult() {
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Map<String,CoreResource> getResources() {
        return resources;
    }

    public void setResources(Map<String,CoreResource> resources) {
        this.resources = resources;
    }
}
