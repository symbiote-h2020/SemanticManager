package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.PIMInstanceDescription;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.ontology.errors.RDFParsingError;
import eu.h2020.symbiote.ontology.utils.RDFReader;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static eu.h2020.symbiote.TestSetupConfig.*;

/**
 * Created by Szymon Mueller on 07/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RDFReaderTests {

    public static final String STATIONARY_SENSOR_TTL_FILE = "/rdf/stationary1.ttl";
    public static final String GROUP_REGISTER_TTL_FILE = "/rdf/groupRegister.ttl";
    public static final String PLATFORM_INSTANCE_TTL_FILE = "/rdf/platformInstance.ttl";

    @Test
    public void testResourceRDFReader() {
        String platformId = "Platform1";
        RDFInfo rdfInfo = new RDFInfo();

        String stationarySensorRdf = null;
        try {
            stationarySensorRdf = IOUtils.toString(this.getClass()
                    .getResource(STATIONARY_SENSOR_TTL_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        rdfInfo.setRdf(stationarySensorRdf);
        rdfInfo.setRdfFormat(RDFFormat.Turtle);
        List<CoreResource> coreResources = null;
        try {
            coreResources = RDFReader.readResourceInstances(rdfInfo, platformId);
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
        }
        assertNotNull(coreResources);
        assertEquals("Rdf contains 1 resource", 1, coreResources.size());
        CoreResource coreResource = coreResources.get(0);
        assertNotNull(coreResource.getId());
        assertNotNull(coreResource.getInterworkingServiceURL());
        assertNotNull(coreResource.getLabels());
        assertNotNull(coreResource.getComments());
        assertEquals(coreResource.getRdf(),stationarySensorRdf);
        assertEquals(coreResource.getRdfFormat(),RDFFormat.Turtle);
    }

    @Test
    public void testRegister() {
        String platformId = "Platform1";
        RDFInfo rdfInfo = new RDFInfo();

        String stationarySensorRdf = null;
        try {
            stationarySensorRdf = IOUtils.toString(this.getClass()
                    .getResource(GROUP_REGISTER_TTL_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        rdfInfo.setRdf(stationarySensorRdf);
        rdfInfo.setRdfFormat(RDFFormat.Turtle);
        List<CoreResource> coreResources = null;
        try {
            coreResources = RDFReader.readResourceInstances(rdfInfo, platformId);
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
        }
        assertNotNull(coreResources);
        assertEquals("Rdf contains 3 resources", 3, coreResources.size());
        for( CoreResource coreRes: coreResources ) {
            assertNotNull(coreRes.getId());
            assertNotNull(coreRes.getInterworkingServiceURL());
            assertNotNull(coreRes.getLabels());
            assertNotNull(coreRes.getComments());
            assertEquals(coreRes.getRdf(), stationarySensorRdf);
            assertEquals(coreRes.getRdfFormat(), RDFFormat.Turtle);
        }
    }

    @Test
    public void testPlatformInstanceRegister() {
//        String platformId = "Platform1";
        RDFInfo rdfInfo = new RDFInfo();

        String platformRdf = null;
        try {
            platformRdf = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_INSTANCE_TTL_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }

        rdfInfo.setRdf(platformRdf);
        rdfInfo.setRdfFormat(RDFFormat.Turtle);
        try {
            PIMInstanceDescription pimInstanceDescription = RDFReader.readPlatformInstance(rdfInfo);
            assertNotNull(pimInstanceDescription);
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
            fail();
        }

//        List<CoreResource> coreResources = null;
//        try {
//            coreResources = RDFReader.readResourceInstances(rdfInfo, platformId);
//        } catch (RDFParsingError rdfParsingError) {
//            rdfParsingError.printStackTrace();
//        }
//        assertNotNull(coreResources);
//        assertEquals("Rdf contains 1 resource", 1, coreResources.size());
//        CoreResource coreResource = coreResources.get(0);
//        assertNotNull(coreResource.getId());
//        assertNotNull(coreResource.getInterworkingServiceURL());
//        assertNotNull(coreResource.getLabels());
//        assertNotNull(coreResource.getComments());
//        assertEquals(coreResource.getRdf(),stationarySensorRdf);
//        assertEquals(coreResource.getRdfFormat(),RDFFormat.Turtle);
    }

}
