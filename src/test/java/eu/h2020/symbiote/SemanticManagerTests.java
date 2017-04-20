package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.validation.ResourceInstanceValidationResult;
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

        resourceValidateAndTranslate(stationarySensor);
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

        resourceValidateAndTranslate(mobileSensor);
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

        resourceValidateAndTranslate(service);
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

        resourceValidateAndTranslate(service);
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

        resourceValidateAndTranslate(actuator);
    }


    private void resourceValidateAndTranslate( Resource resource ) {
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
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error during handling of the validate and create resource request");
        }

        assertNotNull(validationResult);
        assertNotNull(validationResult.getObjectDescription());
        assertEquals("Validation result should contain single resource",1,validationResult.getObjectDescription().size());
        CoreResource resultResource = validationResult.getObjectDescription().get(0);
        assertNotNull("Result should not be null",resultResource);
        assertEquals("Id of the result must be the same", resource.getId(),resultResource.getId());
        assertEquals("Labels of the result must be the same", resource.getLabels(),resultResource.getLabels());
        assertEquals("Comments of the result must be the same", resource.getComments(),resultResource.getComments());
        assertEquals("Interworking service URL of the result must be the same", resource.getInterworkingServiceURL(),resultResource.getInterworkingServiceURL());
        assertNotNull("Created RDF must not be null", resultResource.getRdf());
        assertFalse("Created RDF must not be empty", resultResource.getRdf().isEmpty());
    }

}