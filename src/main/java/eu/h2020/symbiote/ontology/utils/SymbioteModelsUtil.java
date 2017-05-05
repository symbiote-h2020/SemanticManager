package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.model.RDFFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to handle symbIoTe-defined models.
 *
 * Created by Szymon Mueller on 02/05/2017.
 */
public class SymbioteModelsUtil {

    private static final Log log = LogFactory.getLog(SymbioteModelsUtil.class);

    private static final String CIM_FILE = "/core-v0.6.owl";
    private static final String CIM_BASE_NAME = "http://www.symbiote-h2020.eu/ontology/core#";
    private static final String BIM_FILE = "/bim-0.3.owl";
    private static final String BIM_BASE_NAME = "http://www.symbiote-h2020.eu/ontology/bim#";
    private static final String MIM_FILE = "/meta-v0.3.owl";
    private static final String MIM_BASE_NAME = "http://www.symbiote-h2020.eu/ontology/meta#";
    private static final String QU_FILE = "/qu-rec20.owl";
//    private static final String QU_DIM_BASE_NAME = "http://purl.oclc.org/NET/ssnx/qu/dim#";
    private static final String QU_DIM_BASE_NAME = "http://purl.oclc.org/NET/ssnx/qu/quantity#";

    private static Dataset cimDataset;
    private static Dataset bimDataset;
    private static Dataset mimDataset;
    private static Dataset quRecDataset;

    static {
        //Loads models
        try {
            String cimRdf = IOUtils.toString(SymbioteModelsUtil.class
                    .getResourceAsStream(CIM_FILE));

            String bimRdf = IOUtils.toString(SymbioteModelsUtil.class
                    .getResourceAsStream(BIM_FILE));

            String mimRdf = IOUtils.toString(SymbioteModelsUtil.class
                    .getResourceAsStream(MIM_FILE));

            String quRecRdf = IOUtils.toString(SymbioteModelsUtil.class
                    .getResourceAsStream(QU_FILE));

            cimDataset = DatasetFactory.create();
            bimDataset = DatasetFactory.create();
            mimDataset = DatasetFactory.create();
            quRecDataset = DatasetFactory.create();

            insertGraph(cimDataset, "", cimRdf, RDFFormat.Turtle);
            insertGraph(bimDataset, "", bimRdf, RDFFormat.Turtle);
            insertGraph(mimDataset, "", mimRdf, RDFFormat.Turtle);
            insertGraph(quRecDataset, "", quRecRdf, RDFFormat.RDFXML);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if specified name is used in one of the symbIoTe models.
     *
     * @param name Name to search for
     * @return URI of the resource or <code>null</code> in case it couldn't be found in any of the models.
     */
    public static String findInSymbioteModels( String name ) throws eu.h2020.symbiote.ontology.errors.PropertyNotFoundException {
        String uri = null;
        List<eu.h2020.symbiote.ontology.errors.PropertyNotFoundException> propertyNotFoundExceptions = new ArrayList<>();
        log.debug("Checking for " + name + " in symbIoTe models");
        //Check in BIM
        try {
            uri = findUriForNameInModel(name, bimDataset, BIM_BASE_NAME);
        } catch (eu.h2020.symbiote.ontology.errors.PropertyNotFoundException e ) {
            log.error(e);
            propertyNotFoundExceptions.add(e);
        }

        if( uri == null ) {
            try {
                uri = findUriForNameInModel(name, quRecDataset, QU_DIM_BASE_NAME);
            } catch (eu.h2020.symbiote.ontology.errors.PropertyNotFoundException e ) {
                log.error(e);
                propertyNotFoundExceptions.add(e);
            }
        }

        if( uri == null ) {
            //Could not find it in any models, creating and returning aggregated error
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for( eu.h2020.symbiote.ontology.errors.PropertyNotFoundException exc: propertyNotFoundExceptions) {
                sb.append( "["+i++ + " " + exc.getSearchedModel() + "] ");
            }
            throw new eu.h2020.symbiote.ontology.errors.PropertyNotFoundException(name, sb.toString());
        }

        return uri;
    }

    private static String findUriForNameInModel( String name, Dataset modelDataset, String modelBasename ) throws eu.h2020.symbiote.ontology.errors.PropertyNotFoundException {
        String uri = null;

        Resource resourceToSearch = ResourceFactory.createResource(modelBasename + name);
        if( modelDataset.getDefaultModel().containsResource(resourceToSearch) ) {
            uri = modelBasename + name;
        } else {
            throw new eu.h2020.symbiote.ontology.errors.PropertyNotFoundException(name,modelBasename);
        }

        return uri;
    }


    private static void insertGraph(Dataset dataset, String uri, String rdf, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, format.toString());
        insertGraph(dataset, uri, model);
    }

    private static void insertGraph(Dataset dataset, String uri, Model model) {
//        dataset.begin(ReadWrite.WRITE);
//        if (!dataset.containsNamedModel(uri)) {
//            dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
//        }
//        dataset.getNamedModel(uri).add(model);
        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().add(model);
        dataset.commit();
        dataset.end();
    }

}
