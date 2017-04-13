package eu.h2020.symbiote.ontology.utils;

import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.internal.PIMMetaModelDescription;
import eu.h2020.symbiote.core.model.InterworkingService;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class used to read generic information for symbIoTe objects: resource and platform from the RDF.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class RDFReader {

    /**
     * Reads the information about the platform from the specified RDF.
     * @param rdfInfo RDF containing platform information.
     * @return Platform meta-information read from RDF.
     */
    public static PIMInstanceDescription readPlatformInstance(RDFInfo rdfInfo ) {
        PIMInstanceDescription pimInstance = new PIMInstanceDescription();
        pimInstance.setId("1111");
        pimInstance.setLabels(Arrays.asList("PlatformA"));
        pimInstance.setComments(Arrays.asList("This is platform A"));
        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setInformationModelId("http://www.symbiote-h2020.eu/ontology/platformA"); //Same as a model it is validated agains
        interworkingService.setUrl("http://platforma.myhost.eu/myservice");

        pimInstance.setInterworkingServices(Arrays.asList(interworkingService));
        return pimInstance;
    }


    /**
     * Reads the information about the resources from the specified RDF.
     * @param rdfInfo RDF containing resources information.
     * @return Information about resources read from RDF.
     */
    public static List<CoreResource> readResourceInstances( RDFInfo rdfInfo ) {
        List<CoreResource> resources = new ArrayList<>();
        CoreResource resource1 = new CoreResource();
        resource1.setId(null);
        resource1.setLabels(Arrays.asList("Resource1"));
        resource1.setComments(Arrays.asList("This is resource 1"));
        resource1.setInterworkingServiceURL("http://platforma.myhost.eu/myservice");
        resource1.setRdf("<>"); //Set RDF representing only this particular resource
        resource1.setRdfFormat(RDFFormat.JSONLD);

        resources.add(resource1);
        return resources;
    }


}
