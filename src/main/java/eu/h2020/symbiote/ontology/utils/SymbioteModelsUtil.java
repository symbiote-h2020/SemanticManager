package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.internal.CoreResourceType;
import eu.h2020.symbiote.model.cim.*;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.semantics.GraphHelper;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.BIM;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import eu.h2020.symbiote.semantics.ontology.QU;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class to handle symbIoTe-defined models.
 *
 * Created by Szymon Mueller on 02/05/2017.
 */
public class SymbioteModelsUtil {

    private static final Log log = LogFactory.getLog(SymbioteModelsUtil.class);

    private static final String CIM_ID = "CIM";
    private static final String BIM_ID = "BIM";
    private static final String MIM_ID = "MIM";
    public static final String MODEL_BASE_NAME = "http://www.symbiote-h2020.eu/ontology/model#";
    private static final String QU_QUANTITY_BASE_NAME = "http://purl.oclc.org/NET/ssnx/qu/quantity#";
    private static final String BIM_PROPERTY_NAME = "http://www.symbiote-h2020.eu/ontology/bim/property#";
    private static final String QU_ID = "QU";

    private static Dataset cimDataset;
    private static Dataset bimDataset;
    private static Dataset mimDataset;
    private static Dataset quRecDataset;
    private static Dataset pimDataset;

    private SymbioteModelsUtil() {

    }

    private static void loadBaseModel(String loadUri, String insertUri, Dataset dataset) {
        try {
            Model model = ModelHelper.readModel(loadUri);
            GraphHelper.insertGraph(dataset, insertUri, model);
        } catch (IOException ex) {
            log.error("could not load model '" + loadUri + "'. Reason: " + ex.getMessage());
        }
    }

    private static void initDatasets() {
        cimDataset = DatasetFactory.create();
        bimDataset = DatasetFactory.create();
        mimDataset = DatasetFactory.create();
        quRecDataset = DatasetFactory.create();
        pimDataset = DatasetFactory.create();
    }

    static {
        initDatasets();

        //Loads models
        loadBaseModel(CIM.getURI(), ModelHelper.getInformationModelURI(CIM_ID), cimDataset);
        loadBaseModel(MIM.getURI(), ModelHelper.getInformationModelURI(MIM_ID), mimDataset);
        loadBaseModel(BIM.getURI(), ModelHelper.getInformationModelURI(BIM_ID), pimDataset);
        // should not be neccesarry if BIM is loaded with imports
        loadBaseModel(QU.getURI(), ModelHelper.getInformationModelURI(QU_ID), quRecDataset);
    }

    public static void addModels(List<InformationModel> informationModels) {
        if (informationModels != null) {
            log.info("Adding " + informationModels.size() + " information models to Semantic Manager cache");
            for (InformationModel model : informationModels) {
                GraphHelper.insertGraph(pimDataset, ModelHelper.getInformationModelURI(model.getId()), model.getRdf(), model.getRdfFormat());
            }
            log.debug("Adding finished");
        } else {
            log.fatal("Information models adding, but received null information models");
        }
    }

    public static void modifyModels(List<InformationModel> informationModels) {
        if (informationModels != null) {
            log.info("Modifying " + informationModels.size() + " information models in Semantic Manager cache");
            for (InformationModel model : informationModels) {
                GraphHelper.removeGraph(pimDataset, ModelHelper.getInformationModelURI(model.getId()));
                GraphHelper.insertGraph(pimDataset, ModelHelper.getInformationModelURI(model.getId()), model.getRdf(), model.getRdfFormat());
            }
            log.debug("Modifying finished");
        } else {
            log.fatal("Information models modifying, but received null information models");
        }
    }

    public static void removeModels(List<InformationModel> informationModels) {
        if (informationModels != null) {
            log.info("Removing " + informationModels.size() + " information models from Semantic Manager cache");
            for (InformationModel model : informationModels) {
                GraphHelper.removeGraph(pimDataset, ModelHelper.getInformationModelURI(model.getId()));
            }
            log.debug("Removing finished");
        } else {
            log.fatal("Information models to delete, but received null information models");
        }
    }

    /**
     * Checks if specified name is used in one of the symbIoTe Core models: BIM
     * and CIM.
     *
     * @param name Name to search for
     * @return URI of the resource or <code>null</code> in case it couldn't be
     * found in any of the models.
     */
    public static String findInSymbioteCoreModels(String name) throws eu.h2020.symbiote.ontology.errors.PropertyNotFoundException {
        String uri = null;
        List<eu.h2020.symbiote.ontology.errors.PropertyNotFoundException> propertyNotFoundExceptions = new ArrayList<>();
        log.debug("Checking for " + name + " in symbIoTe models");
        //Check in BIM
        try {
//            Optional<Resource> result = ModelHelper.findResource(CIM.Property, name, BIM_ID, bimDataset);
//            if (!result.isPresent()) {
//                throw new PropertyNotFoundException(name, BIM.getURI());
//            }
            uri = findUriForNameInModel(name, ModelHelper.getInformationModelURI(BIM_ID), pimDataset, BIM_PROPERTY_NAME);
        } catch (eu.h2020.symbiote.ontology.errors.PropertyNotFoundException e) {
            log.error(e);
            propertyNotFoundExceptions.add(e);
        }

        if (uri == null) {
            try {
                uri = findUriForNameInModel(name, ModelHelper.getInformationModelURI(QU_ID), quRecDataset, QU_QUANTITY_BASE_NAME);
            } catch (eu.h2020.symbiote.ontology.errors.PropertyNotFoundException e) {
                log.error(e);
                propertyNotFoundExceptions.add(e);
            }
        }

        if (uri == null) {
            //Could not find it in any models, creating and returning aggregated error
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (PropertyNotFoundException exc : propertyNotFoundExceptions) {
                sb.append("[" + i++ + " " + exc.getSearchedModel() + "] ");
            }
            throw new PropertyNotFoundException(name, sb.toString());
        }

        return uri;
    }

    private static String findUriForNameInModel(String name, String modelUri, Dataset modelDataset, String modelBasename) throws eu.h2020.symbiote.ontology.errors.PropertyNotFoundException {
        String uri;

        Resource resourceToSearch = ResourceFactory.createResource(modelBasename + name);
        if (modelDataset.getNamedModel(modelUri).containsResource(resourceToSearch)) {
            uri = modelBasename + name;
        } else {
            throw new PropertyNotFoundException(name, modelBasename);
        }
        return uri;
    }

    public static Model findInformationModelById(String id) {
        return pimDataset.getNamedModel(ModelHelper.getInformationModelURI(id));
    }

    public static CoreResourceType getTypeForResource(eu.h2020.symbiote.model.cim.Resource resource) {
        CoreResourceType type = null;
        if (resource instanceof Device) {
            type = CoreResourceType.DEVICE;
        } else if (resource instanceof MobileSensor) {
            type = CoreResourceType.MOBILE_SENSOR;
        } else if (resource instanceof StationarySensor) {
            type = CoreResourceType.STATIONARY_SENSOR;
        } else if (resource instanceof Actuator) {
            type = CoreResourceType.ACTUATOR;
        } else if (resource instanceof Service) {
            type = CoreResourceType.SERVICE;
        }
        return type;
    }

    public static CoreResourceType getTypeForResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("resource must be non-null");
        }
        // TODO add proper implementation or remove property from CoreResource completely when not needed
        return CoreResourceType.DEVICE;
//        
//        
//        Set<Resource> resourceClasses = resource.listProperties(RDF.type).mapWith(x -> x.getObject().asResource()).toSet();
//        // TODO change to allow for multiple types
//        if (resourceClasses.size() != 1) {
//            throw new IllegalArgumentException("resource must have exactly one rdf type");
//        }
//        Resource resourceClass = resourceClasses.iterator().next();
//        CoreResourceType type = null;
//        if (resourceClass.equals(CoreInformationModel.Device)) {
//            type = CoreResourceType.DEVICE;
//        } else if (resourceClass.equals(CoreInformationModel.MobileSensor)) {
//            type = CoreResourceType.MOBILE_SENSOR;
//        } else if (resourceClass.equals(CoreInformationModel.StationarySensor)) {
//            type = CoreResourceType.STATIONARY_SENSOR;
//        } else if (resourceClass.equals(CoreInformationModel.Actuator)) {
//            type = CoreResourceType.ACTUATOR;
//        } else if (resourceClass.equals(CoreInformationModel.Service)) {
//            type = CoreResourceType.SERVICE;
//        }
//        return type;
    }

}
