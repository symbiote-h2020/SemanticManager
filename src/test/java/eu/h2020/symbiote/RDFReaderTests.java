package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.cci.RDFResourceRegistryRequest;
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
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 07/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RDFReaderTests {

    public static final String STATIONARY_SENSOR_TTL_FILE = "/rdf/stationary1.ttl";
    public static final String STATIONARY_SENSOR_TTL_URI = "http://www.symbiote-h2020.eu/ontology/resources/stationary1";
    public static final String STATIONARY_SENSOR_TTL_FILE2 = "/rdf/stationary2.ttl";
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
        Map<String,CoreResource> coreResources = null;
        try {
            coreResources = RDFReader.readResourceInstances(rdfInfo);
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
        }
        assertNotNull(coreResources);
        assertEquals("Rdf contains 1 resource", 1, coreResources.size());
        CoreResource coreResource = coreResources.get(STATIONARY_SENSOR_TTL_URI);
        assertNotNull(coreResource);
        assertNotNull(coreResource.getId());
        // Removed becaue want said to get rid of this
//        assertNotNull(coreResource.getInterworkingServiceURL());
        assertNotNull(coreResource.getLabels());
        assertNotNull(coreResource.getComments());
//        assertEquals(coreResource.getRdf(),stationarySensorRdf);
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
        Map<String,CoreResource> coreResources = null;
        try {
            coreResources = RDFReader.readResourceInstances(rdfInfo);
        } catch (RDFParsingError rdfParsingError) {
            rdfParsingError.printStackTrace();
        }
        assertNotNull(coreResources);
        assertEquals("Rdf contains 3 resources", 3, coreResources.size());
        for( String coreResPairingId : coreResources.keySet() ) {
            CoreResource coreRes = coreResources.get(coreResPairingId);
            assertNotNull(coreRes.getId());
            // Removed becaue want said to get rid of this
//            assertNotNull(coreRes.getInterworkingServiceURL());
            assertNotNull(coreRes.getLabels());
            assertNotNull(coreRes.getComments());
//            assertEquals(coreRes.getRdf(), stationarySensorRdf);
            assertEquals(coreRes.getRdfFormat(), RDFFormat.Turtle);
        }
    }

    public void generateTTLRDFRegistrationReq() {
        try {
            String rdf = IOUtils.toString(this.getClass()
                    .getResource(STATIONARY_SENSOR_TTL_FILE2));
            generateRDFRegistrationReq(rdf,RDFFormat.Turtle);

        } catch (IOException e) {
            e.printStackTrace();
        }
            }


    public void generateJSONLDRDFRegistratioNReq() {
        String rdf = "{\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"http://www.testPlatform1.eu/example/ontology/mobile1/location\",\n" +
                "    \"@type\" : [ \"http://www.symbiote-h2020.eu/ontology/core#WGS84Location\", \"http://www.symbiote-h2020.eu/ontology/core#Location\" ],\n" +
                "    \"comment\" : \"This is paris\",\n" +
                "    \"label\" : \"Paris\",\n" +
                "    \"alt\" : \"15.0\",\n" +
                "    \"lat\" : \"48.864716\",\n" +
                "    \"long\" : \"2.349014\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.testPlatform1.eu/example/ontology/mobile1\",\n" +
                "    \"@type\" : [ \"http://www.symbiote-h2020.eu/ontology/core#MobileSensor\", \"http://www.symbiote-h2020.eu/ontology/core#Resource\" ],\n" +
                "    \"locatedAt\" : \"http://www.testPlatform1.eu/example/ontology/mobile1/location\",\n" +
                "    \"observes\" : \"http://purl.oclc.org/NET/ssnx/qu/quantity#temperature\",\n" +
                "    \"comment\" : \"RDF mobile sensor 1\",\n" +
                "    \"label\" : \"RDFMobile1\"\n" +
                "  } ],\n" +
                "  \"@context\" : {\n" +
                "    \"locatedAt\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#locatedAt\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"observes\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#observes\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"comment\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2000/01/rdf-schema#comment\"\n" +
                "    },\n" +
                "    \"label\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2000/01/rdf-schema#label\"\n" +
                "    },\n" +
                "    \"id\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#id\"\n" +
                "    },\n" +
                "    \"alt\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#alt\"\n" +
                "    },\n" +
                "    \"long\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#long\"\n" +
                "    },\n" +
                "    \"lat\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#lat\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        generateRDFRegistrationReq(rdf, RDFFormat.JSONLD);
    }

    private void generateRDFRegistrationReq( String rdf, RDFFormat format ) {
        RDFResourceRegistryRequest req = new RDFResourceRegistryRequest();
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdfFormat(format);
        rdfInfo.setRdf(rdf);
        req.setRdfInfo(rdfInfo);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String reqJson = mapper.writeValueAsString(req);
            System.out.println(reqJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
