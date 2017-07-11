package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.ontology.SemanticManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static eu.h2020.symbiote.TestSetupConfig.*;

@RunWith(MockitoJUnitRunner.class)
public class SemanticManagerTests {

    @Test
    public void testStationarySensorValidateAndCreate() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setId(STATIONARY1_ID);
        stationarySensor.setLabels(STATIONARY1_LABELS);
        stationarySensor.setComments(STATIONARY1_COMMENTS);
        stationarySensor.setLocatedAt(STATIONARY1_LOCATION);
        stationarySensor.setInterworkingServiceURL(STATIONARY1_URL);
        stationarySensor.setFeatureOfInterest(STATIONARY1_FOI);
        stationarySensor.setObservesProperty(STATIONARY1_PROPERTIES);

        resourceValidateAndTranslate(stationarySensor,1);
    }

    @Test
    public void testMobileSensorValidateAndCreate() {
        MobileSensor mobileSensor = new MobileSensor();
        mobileSensor.setId(MOBILE1_ID);
        mobileSensor.setLabels(MOBILE1_LABELS);
        mobileSensor.setComments(MOBILE1_COMMENTS);
        mobileSensor.setLocatedAt(MOBILE1_LOCATION);
        mobileSensor.setInterworkingServiceURL(MOBILE1_URL);
        mobileSensor.setObservesProperty(MOBILE1_PROPERTIES);

        resourceValidateAndTranslate(mobileSensor,1);
    }

    @Test
    public void testServiceValidateAndCreate() {
        Service service = new Service();
        service.setId(SERVICE1_ID);
        service.setLabels(SERVICE1_LABELS);
        service.setComments(SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(SERVICE1_URL);
        service.setName(SERVICE1_NAME);

        InputParameter inParam = new InputParameter();
        inParam.setName(SERVICE1_INPUT_NAME);
        inParam.setMandatory(SERVICE1_INPUT_MANDATORY);
        inParam.setArray(SERVICE1_INPUT_ARRAY);
        inParam.setDatatype(SERVICE1_INPUT_DATATYPE);

        RangeRestriction restriction1 = new RangeRestriction();
        restriction1.setMin(SERVICE1_INPUT_RESTRICTION_MIN);
        restriction1.setMax(SERVICE1_INPUT_RESTRICTION_MAX);
        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setInputParameter(Arrays.asList(inParam));

        Parameter outputParameter = new Parameter();
        outputParameter.setDatatype(SERVICE1_OUTPUT_DATATYPE);
        outputParameter.setArray(SERVICE1_OUTPUT_ARRAY);
        service.setOutputParameter(outputParameter);

        resourceValidateAndTranslate(service,1);
    }

    @Test
    public void testActuatingServiceValidateAndCreate() {
        ActuatingService service = new ActuatingService();
        service.setId(ACTUATING_SERVICE1_ID);
        service.setLabels(ACTUATING_SERVICE1_LABELS);
        service.setComments(ACTUATING_SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(ACTUATING_SERVICE1_URL);
        service.setName(ACTUATING_SERVICE1_NAME);
        service.setActsOn(ACTUATING_SERVICE1_ACTSON);
        service.setAffects(ACTUATING_SERVICE1_AFFECTS);

        InputParameter inParam = new InputParameter();
        inParam.setName(ACTUATING_SERVICE1_INPUT_NAME);
        inParam.setMandatory(ACTUATING_SERVICE1_INPUT_MANDATORY);
        inParam.setArray(ACTUATING_SERVICE1_INPUT_ARRAY);
        inParam.setDatatype(ACTUATING_SERVICE1_INPUT_DATATYPE);

        RangeRestriction restriction1 = new RangeRestriction();
        restriction1.setMin(ACTUATING_SERVICE1_INPUT_RESTRICTION_MIN);
        restriction1.setMax(ACTUATING_SERVICE1_INPUT_RESTRICTION_MAX);
        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setInputParameter(Arrays.asList(inParam));

        Parameter outputParameter = new Parameter();
        outputParameter.setDatatype(ACTUATING_SERVICE1_OUTPUT_DATATYPE);
        outputParameter.setArray(ACTUATING_SERVICE1_OUTPUT_ARRAY);
        service.setOutputParameter(outputParameter);

        resourceValidateAndTranslate(service,1);
    }


    @Test
    public void testActuatorValidateAndCreate() {
        Actuator actuator = new Actuator();
        actuator.setId(ACTUATOR1_ID);
        actuator.setLabels(ACTUATOR1_LABELS);
        actuator.setComments(ACTUATOR1_COMMENTS);
        actuator.setLocatedAt(ACTUATOR1_LOCATION);
        actuator.setInterworkingServiceURL(ACTUATOR1_URL);

        actuator.setCapabilities(ACTUATOR1_CAPABILITIES);

        resourceValidateAndTranslate(actuator,2);
    }

    @Test
    public void testActuatorWithoutId() {
        Actuator actuator = new Actuator();
        actuator.setLabels(Arrays.asList("Inner actuating service 1"));
        actuator.setComments(ACTUATOR1_COMMENTS);
        actuator.setLocatedAt(ACTUATOR1_LOCATION);
        actuator.setInterworkingServiceURL(ACTUATOR1_URL);

        ActuatingService service = new ActuatingService();
        service.setLabels(ACTUATING_SERVICE1_LABELS);
        service.setComments(ACTUATING_SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(ACTUATING_SERVICE1_URL);
        service.setName(ACTUATING_SERVICE1_NAME);
        service.setActsOn(ACTUATING_SERVICE1_ACTSON);
        service.setAffects(ACTUATING_SERVICE1_AFFECTS);

        InputParameter inParam = new InputParameter();
        inParam.setName(ACTUATING_SERVICE1_INPUT_NAME);
        inParam.setMandatory(ACTUATING_SERVICE1_INPUT_MANDATORY);
        inParam.setArray(ACTUATING_SERVICE1_INPUT_ARRAY);
        inParam.setDatatype(ACTUATING_SERVICE1_INPUT_DATATYPE);

        Parameter outputParameter = new Parameter();
        outputParameter.setDatatype(ACTUATING_SERVICE1_OUTPUT_DATATYPE);
        outputParameter.setArray(ACTUATING_SERVICE1_OUTPUT_ARRAY);
        service.setOutputParameter(outputParameter);

        RangeRestriction restriction1 = new RangeRestriction();
        restriction1.setMin(ACTUATING_SERVICE1_INPUT_RESTRICTION_MIN);
        restriction1.setMax(ACTUATING_SERVICE1_INPUT_RESTRICTION_MAX);
        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setInputParameter(Arrays.asList(inParam));

        actuator.setCapabilities(Arrays.asList(service));

        resourceValidateAndTranslate(actuator,2);
    }

    @Test
    public void testMobileDeviceValdiateAndCreate() {
        MobileDevice mobileDevice = new MobileDevice();
        mobileDevice.setId(MOBILEDEVICE1_ID);
        mobileDevice.setLabels(MOBILEDEVICE1_LABELS);
        mobileDevice.setComments(MOBILEDEVICE1_COMMENTS);
        mobileDevice.setLocatedAt(MOBILEDEVICE1_LOCATION);
        mobileDevice.setInterworkingServiceURL(MOBILEDEVICE1_URL);
        mobileDevice.setObservesProperty(MOBILEDEVICE1_PROPERTIES);
        mobileDevice.setCapabilities(MOBILEDEVICE1_CAPABILITIES);

        resourceValidateAndTranslate(mobileDevice,2);
    }

    @Test
    public void testStationaryDeviceValidateAndCreate() {
        StationaryDevice stationaryDevice = new StationaryDevice();
        stationaryDevice.setId(STATIONARYDEVICE1_ID);
        stationaryDevice.setLabels(STATIONARYDEVICE1_LABELS);
        stationaryDevice.setComments(STATIONARYDEVICE1_COMMENTS);
        stationaryDevice.setLocatedAt(STATIONARYDEVICE1_LOCATION);
        stationaryDevice.setInterworkingServiceURL(STATIONARYDEVICE1_URL);
        stationaryDevice.setFeatureOfInterest(STATIONARYDEVICE1_FOI);
        stationaryDevice.setObservesProperty(STATIONARYDEVICE1_PROPERTIES);
        stationaryDevice.setCapabilities(STATIONARYDEVICE1_CAPABILITIES);

        resourceValidateAndTranslate(stationaryDevice,2);
    }

    @Test
    public void testPlatformValidateAndCreate() {
        PIMInstanceDescription pimInstance = new PIMInstanceDescription();
        pimInstance.setId(PLATFORM_ID);
        pimInstance.setComments(Arrays.asList(PLATFORM_COMMENT));
        pimInstance.setLabels(Arrays.asList(PLATFORM_NAME));
        pimInstance.setInterworkingServices(PLATFORM_INTERWORKING_SERVICES);

        SemanticManager manager = SemanticManager.getManager();
        PIMInstanceValidationResult pimInstanceValidationResult = manager.validateAndCreateBIMPlatformToRDF(pimInstance);
        assertNotNull("Result must not be null", pimInstanceValidationResult );
        assertNotNull("Object description of returned result must not be null", pimInstanceValidationResult.getObjectDescription());
        String rdf = pimInstanceValidationResult.getObjectDescription().getRdf();
        assertNotNull("RDF returned must not be null", rdf);

        System.out.println("Generated rdf: ");
        System.out.println(rdf);
        assertFalse(rdf.isEmpty());

    }

    private void resourceValidateAndTranslate( Resource resource, int expectedSize ) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String resString = mapper.writeValueAsString(resource);
            System.out.println("============================");
            System.out.println(resString);
            System.out.println("============================");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        SemanticManager manager = SemanticManager.getManager();


        CoreResourceRegistryRequest request = new CoreResourceRegistryRequest();
        request.setPlatformId(PLATFORM_ID);
        try {
            String resourcesJson = mapper.writerFor(new TypeReference<List<Resource>>() {
            }).writeValueAsString(Arrays.asList(resource));
            request.setBody(resourcesJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail("Error occured when using Json mapper" + e.getMessage());
        }
        request.setDescriptionType(DescriptionType.BASIC);
        request.setToken("Token");

        ResourceInstanceValidationResult validationResult = null;
        try {
            validationResult = manager.validateAndCreateBIMResourceToRDF(request);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error during handling of the validate and create resource request");
        }

        assertNotNull(validationResult);
        assertNotNull(validationResult.getObjectDescription());
        assertEquals("Validation result should contain " + expectedSize + " resource", expectedSize, validationResult.getObjectDescription().size());
        CoreResource resultResource = validationResult.getObjectDescription().get(0);
        assertNotNull("Result should not be null", resultResource);
        if (resource.getId() != null) {
            assertEquals("Id of the result must be the same", resource.getId(), resultResource.getId());
        } else {
            assertNotNull("If resource had null id it must be generated", resultResource.getId());
        }
        assertEquals("Labels of the result must be the same", resource.getLabels(),resultResource.getLabels());
        assertEquals("Comments of the result must be the same", resource.getComments(),resultResource.getComments());
        assertEquals("Interworking service URL of the result must be the same", resource.getInterworkingServiceURL(),resultResource.getInterworkingServiceURL());
        assertNotNull("Created RDF must not be null", resultResource.getRdf());
        assertFalse("Created RDF must not be empty", resultResource.getRdf().isEmpty());


        System.out.println(" >>>> Received RDF: ");
        System.out.println(resultResource.getRdf() );
        System.out.println(" <<<<<<<<<<<<<<<<<< ");
    }

}