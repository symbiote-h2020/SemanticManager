package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.model.internal.CoreResource;
import org.apache.jena.rdf.model.Model;

import java.util.List;

/**
 * Created by Szymon Mueller on 24/05/2017.
 */
public class GenerationResult {

    private Model model;

    //Contains list of additional sub-resources found for this resource.
    private List<CoreResource> resources;

    public GenerationResult() {
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public List<CoreResource> getResources() {
        return resources;
    }

    public void setResources(List<CoreResource> resources) {
        this.resources = resources;
    }
}
