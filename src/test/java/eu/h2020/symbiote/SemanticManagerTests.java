package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationResult;
import eu.h2020.symbiote.model.cim.*;
import eu.h2020.symbiote.ontology.SemanticManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SemanticManagerTests {

    @Test
    public void testStationarySensorValidateAndCreate() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setId(STATIONARY1_ID);
        stationarySensor.setName(STATIONARY1_LABEL);
        stationarySensor.setDescription(STATIONARY1_COMMENTS);
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
        mobileSensor.setName(MOBILE1_LABEL);
        mobileSensor.setDescription(MOBILE1_COMMENTS);
        mobileSensor.setLocatedAt(MOBILE1_LOCATION);
        mobileSensor.setInterworkingServiceURL(MOBILE1_URL);
        mobileSensor.setObservesProperty(MOBILE1_PROPERTIES);

        resourceValidateAndTranslate(mobileSensor,1);
    }

    @Test
    public void testServiceValidateAndCreate() {
        Service service = new Service();
        service.setId(SERVICE1_ID);
        service.setName(SERVICE1_LABEL);
        service.setDescription(SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(SERVICE1_URL);
        service.setName(SERVICE1_NAME);

        service.setParameters(Arrays.asList(createService1Param()));

//        Parameter outputParameter = new Parameter();
//        outputParameter.setDatatype(SERVICE1_OUTPUT_DATATYPE);
//        outputParameter.setArray(SERVICE1_OUTPUT_ARRAY);
//        service.setOutputParameter(outputParameter);

//        Datatype resultType = new Datatype();

        service.setResultType(createDatatypeString());

        resourceValidateAndTranslate(service,1);
    }

//    @Test
//    public void testDeviceValidateAndCreate() {
//        Device device = new Device();
//        Service service = new Service();
//        service.setId(SERVICE1_ID);
//        service.setName(SERVICE1_LABEL);
//        service.setDescription(SERVICE1_COMMENTS);
//        service.setInterworkingServiceURL(SERVICE1_URL);
//        service.setName(SERVICE1_NAME);
//
//        service.setParameters(Arrays.asList(createService1Param()));
//
////        Parameter outputParameter = new Parameter();
////        outputParameter.setDatatype(SERVICE1_OUTPUT_DATATYPE);
////        outputParameter.setArray(SERVICE1_OUTPUT_ARRAY);
////        service.setOutputParameter(outputParameter);
//
////        Datatype resultType = new Datatype();
//
//        service.setResultType(createDatatypeString());
//
//        resourceValidateAndTranslate(service,1);
//    }

    @Test
    public void testServiceWithComplexDatatypeValidateAndCreate() {
        Service service = new Service();
        service.setId(SERVICE1_ID);
        service.setName(SERVICE1_LABEL);
        service.setDescription(SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(SERVICE1_URL);
        service.setName(SERVICE1_NAME);

        service.setParameters(Arrays.asList(createService1Param()));

        ComplexDatatype complexDatatype = new ComplexDatatype();
        complexDatatype.setArray(false);
        complexDatatype.setBasedOnClass("bim:Light");
        PrimitiveProperty dp = new PrimitiveProperty();
        PrimitiveDatatype propertyDatatype = new PrimitiveDatatype();
        propertyDatatype.setBaseDatatype("xsd:string");
        propertyDatatype.setArray(false);
        dp.setPrimitiveDatatype( propertyDatatype );
        dp.setBasedOnProperty("owl:property");
        complexDatatype.setDataProperties(Arrays.asList(dp));
        service.setResultType(complexDatatype);


        resourceValidateAndTranslate(service,1);
    }

//
//    @Test
//    public void testServiceValidateAndCreate() {
//        Service service = new Service();
//        service.setId(ACTUATING_SERVICE1_ID);
//        service.setLabels(ACTUATING_SERVICE1_LABELS);
//        service.setComments(ACTUATING_SERVICE1_COMMENTS);
//        service.setInterworkingServiceURL(ACTUATING_SERVICE1_URL);
//        service.setName(ACTUATING_SERVICE1_NAME);
////        service.setActsOn(ACTUATING_SERVICE1_ACTSON);
////        service.setAffects(ACTUATING_SERVICE1_AFFECTS);
//
//        Parameter inParam = new Parameter();
////        inParam.setName(ACTUATING_SERVICE1_INPUT_NAME);
//        inParam.setMandatory(ACTUATING_SERVICE1_INPUT_MANDATORY);
////        inParam.setArray(ACTUATING_SERVICE1_INPUT_ARRAY);
////        inParam.setDatatype(ACTUATING_SERVICE1_INPUT_DATATYPE);
//
//        RangeRestriction restriction1 = new RangeRestriction();
//        restriction1.setMin(ACTUATING_SERVICE1_INPUT_RESTRICTION_MIN);
//        restriction1.setMax(ACTUATING_SERVICE1_INPUT_RESTRICTION_MAX);
//        inParam.setRestrictions(Arrays.asList(restriction1));
//        service.setParameters(Arrays.asList(inParam));
//
//
////        Parameter outputParameter = new Parameter();
////        outputParameter.setDatatype(ACTUATING_SERVICE1_OUTPUT_DATATYPE);
////        outputParameter.setArray(ACTUATING_SERVICE1_OUTPUT_ARRAY);
////        service.setOutputParameter(outputParameter);
//
//        RdfsDatatype resultType = new RdfsDatatype();
//        resultType.setArray(SERVICE1_OUTPUT_ARRAY);
//        resultType.setDatatypeName("http://www.w3.org/2001/XMLSchema#string");
//        service.setResultType(resultType);
//
//        resourceValidateAndTranslate(service,1);
//    }


    @Test
    public void testActuatorValidateAndCreate() {
        Actuator actuator = new Actuator();
        actuator.setId(ACTUATOR1_ID);
        actuator.setName(ACTUATOR1_LABEL);
        actuator.setDescription(ACTUATOR1_COMMENTS);
        actuator.setLocatedAt(ACTUATOR1_LOCATION);
        actuator.setInterworkingServiceURL(ACTUATOR1_URL);

        Capability capability = new Capability();
        capability.setName("Cap1");

        capability.setParameters(Arrays.asList(createService1Param()));

        Effect effect = new Effect();
        effect.setActsOn(GENERAL_FOI);
        effect.setAffects(STATIONARY1_PROPERTIES);

        capability.setEffects(Arrays.asList(effect));

        actuator.setCapabilities(Arrays.asList(capability));

        resourceValidateAndTranslate(actuator,1);
    }

    @Test
    public void testServiceWithoutId() {
        Actuator actuator = new Actuator();
        actuator.setName("Inner actuating service 1");
        actuator.setDescription(ACTUATOR1_COMMENTS);
        actuator.setLocatedAt(ACTUATOR1_LOCATION);
        actuator.setInterworkingServiceURL(ACTUATOR1_URL);

        Service service = new Service();
        service.setName(ACTUATING_SERVICE1_LABEL);
        service.setDescription(ACTUATING_SERVICE1_COMMENTS);
        service.setInterworkingServiceURL(ACTUATING_SERVICE1_URL);
        service.setName(ACTUATING_SERVICE1_NAME);
//        service.setActsOn(ACTUATING_SERVICE1_ACTSON);
//        service.setAffects(ACTUATING_SERVICE1_AFFECTS);

//        Parameter inParam = new Parameter(ACTUATING_SERVICE1_INPUT_NAME);
//        inParam.setMandatory(ACTUATING_SERVICE1_INPUT_MANDATORY);
//        inParam.setArray(ACTUATING_SERVICE1_INPUT_ARRAY);
//        inParam.setDatatype(ACTUATING_SERVICE1_INPUT_DATATYPE);
//
//        Parameter outputParameter = new Parameter();
//        outputParameter.setDatatype(ACTUATING_SERVICE1_OUTPUT_DATATYPE);
//        outputParameter.setArray(ACTUATING_SERVICE1_OUTPUT_ARRAY);
//        service.setOutputParameter(outputParameter);
//
//        RangeRestriction restriction1 = new RangeRestriction();
//        restriction1.setMin(ACTUATING_SERVICE1_INPUT_RESTRICTION_MIN);
//        restriction1.setMax(ACTUATING_SERVICE1_INPUT_RESTRICTION_MAX);
//        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setParameters(Arrays.asList(createService1Param()));

        actuator.setServices(Arrays.asList(service));

        resourceValidateAndTranslate(actuator,1);
    }

//    @Test
//    public void testMobileDeviceValdiateAndCreate() {
//        MobileDevice mobileDevice = new MobileDevice();
//        mobileDevice.setId(MOBILEDEVICE1_ID);
//        mobileDevice.setLabels(MOBILEDEVICE1_LABELS);
//        mobileDevice.setComments(MOBILEDEVICE1_COMMENTS);
//        mobileDevice.setLocatedAt(MOBILEDEVICE1_LOCATION);
//        mobileDevice.setInterworkingServiceURL(MOBILEDEVICE1_URL);
//        mobileDevice.setObservesProperty(MOBILEDEVICE1_PROPERTIES);
//        mobileDevice.setCapabilities(MOBILEDEVICE1_CAPABILITIES);
//
//        resourceValidateAndTranslate(mobileDevice,2);
//    }

//    @Test
//    public void testStationaryDeviceValidateAndCreate() {
//        StationaryDevice stationaryDevice = new StationaryDevice();
//        stationaryDevice.setId(STATIONARYDEVICE1_ID);
//        stationaryDevice.setLabels(STATIONARYDEVICE1_LABELS);
//        stationaryDevice.setComments(STATIONARYDEVICE1_COMMENTS);
//        stationaryDevice.setLocatedAt(STATIONARYDEVICE1_LOCATION);
//        stationaryDevice.setInterworkingServiceURL(STATIONARYDEVICE1_URL);
//        stationaryDevice.setFeatureOfInterest(STATIONARYDEVICE1_FOI);
//        stationaryDevice.setObservesProperty(STATIONARYDEVICE1_PROPERTIES);
//        stationaryDevice.setCapabilities(STATIONARYDEVICE1_CAPABILITIES);
//
//        resourceValidateAndTranslate(stationaryDevice,2);
//    }

//    @Test
//    public void testPlatformValidateAndCreate() {
//        PIMInstanceDescription pimInstance = new PIMInstanceDescription();
//        pimInstance.setId(PLATFORM_ID);
//        pimInstance.setComments(Arrays.asList(PLATFORM_COMMENT));
//        pimInstance.setLabels(Arrays.asList(PLATFORM_NAME));
//        pimInstance.setInterworkingServices(PLATFORM_INTERWORKING_SERVICES);
//
//        SemanticManager manager = SemanticManager.getManager();
//        PIMInstanceValidationResult pimInstanceValidationResult = manager.validateAndCreateBIMPlatformToRDF(pimInstance);
//        assertNotNull("Result must not be null", pimInstanceValidationResult );
//        assertNotNull("Object description of returned result must not be null", pimInstanceValidationResult.getObjectDescription());
//        String rdf = pimInstanceValidationResult.getObjectDescription().getRdf();
//        assertNotNull("RDF returned must not be null", rdf);
//
//        System.out.println("Generated rdf: ");
//        System.out.println(rdf);
//        assertFalse(rdf.isEmpty());
//
//    }

    private void resourceValidateAndTranslate( Resource resource, int expectedSize ) {
        String resourcePairingId = "111";
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
        Map<String,Resource> map = new HashMap<>();
        map.put(resourcePairingId,resource);
        try {
            String resourcesJson = mapper.writerFor(new TypeReference<Map<String,Resource>>() {
            }).writeValueAsString(map);
            request.setBody(resourcesJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail("Error occured when using Json mapper" + e.getMessage());
        }
        request.setDescriptionType(DescriptionType.BASIC);
        request.setSecurityRequest(null);

        ResourceInstanceValidationResult validationResult = null;
        try {
            validationResult = manager.validateAndCreateBIMResourceToRDF(map,request.getPlatformId(),false);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error during handling of the validate and create resource request");
        }

        assertNotNull(validationResult);
        assertNotNull(validationResult.getObjectDescription());
        assertEquals("Validation result should contain " + expectedSize + " resource", expectedSize, validationResult.getObjectDescription().size());
        CoreResource resultResource = validationResult.getObjectDescription().get(resourcePairingId);
        assertNotNull("Result should not be null", resultResource);
        if (resource.getId() != null) {
            assertEquals("Id of the result must be the same", resource.getId(), resultResource.getId());
        } else {
            assertNotNull("If resource had null id it must be generated", resultResource.getId());
        }
        assertEquals("Labels of the result must be the same", resource.getName(),resultResource.getName());
        assertEquals("Comments of the result must be the same", resource.getDescription(),resultResource.getDescription());
        assertEquals("Interworking service URL of the result must be the same", resource.getInterworkingServiceURL(),resultResource.getInterworkingServiceURL());
        assertNotNull("Created RDF must not be null", resultResource.getRdf());
        assertFalse("Created RDF must not be empty", resultResource.getRdf().isEmpty());


        System.out.println(" >>>> Received RDF: ");
        System.out.println(resultResource.getRdf() );
        System.out.println(" <<<<<<<<<<<<<<<<<< ");
    }


}