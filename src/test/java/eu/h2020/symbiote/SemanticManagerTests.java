package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.core.model.resources.*;
import eu.h2020.symbiote.ontology.SemanticManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SemanticManagerTests {

    @Test
    public void test() {
        Actuator actuator = new Actuator();
        actuator.setId("actuator1");
        actuator.setLabels(Arrays.asList("Actuator 1"));
        actuator.setComments(Arrays.asList("This is actuator 1"));
        actuator.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");

    }

    @Test
    public void testStationarySensorValidateAndCreate() {
        StationarySensor stationarySensor = new StationarySensor();
        stationarySensor.setId("stationary1");
        stationarySensor.setLabels(Arrays.asList("Stationary 1"));
        stationarySensor.setComments(Arrays.asList("This is stationary 1"));
        stationarySensor.setLocatedAt("Paris");
        stationarySensor.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");
        stationarySensor.setFeatureOfInterest("Home1");
        stationarySensor.setObservesProperty(Arrays.asList("Temperature","Humidity"));

        resourceValidateAndTranslate(stationarySensor);
    }

    @Test
    public void testMobileSensorValidateAndCreate() {
        MobileSensor mobileSensor = new MobileSensor();
        mobileSensor.setId("mobile1");
        mobileSensor.setLabels(Arrays.asList("Mobile 1"));
        mobileSensor.setComments(Arrays.asList("This is mobile 1"));
        mobileSensor.setLocatedAt("Paris");
        mobileSensor.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");
        mobileSensor.setObservesProperty(Arrays.asList("Temperature"));

        resourceValidateAndTranslate(mobileSensor);
    }

    @Test
    public void testServiceValidateAndCreate() {
        Service service = new Service();
        service.setId("service1");
        service.setLabels(Arrays.asList("Service 1"));
        service.setComments(Arrays.asList("This is service 1"));
        service.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");
        service.setName("service1Name");

        InputParameter inParam = new InputParameter();
        inParam.setName("inputParam1");
        inParam.setMandatory(true);
        inParam.setArray(false);
        inParam.setDatatype("xsd:string");

        RangeRestriction restriction1 = new RangeRestriction();
        restriction1.setMin(Double.valueOf(2.0));
        restriction1.setMax(Double.valueOf(10.0));
        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setInputParameter(Arrays.asList(inParam));

        Parameter outputParameter = new Parameter();
        outputParameter.setDatatype("xsd:string");
        outputParameter.setArray(false);
        service.setOutputParameter(outputParameter);

        resourceValidateAndTranslate(service);
    }

    @Test
    public void testActuatingServiceValidateAndCreate() {
        ActuatingService service = new ActuatingService();
        service.setId("actuatingService1");
        service.setLabels(Arrays.asList("Actuating service 1"));
        service.setComments(Arrays.asList("This is actuating service 1"));
        service.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");
        service.setName("actuatingService1Name");
        service.setActsOn("Room1");
        service.setAffects(Arrays.asList("Temperature"));

        InputParameter inParam = new InputParameter();
        inParam.setName("inputParam1");
        inParam.setMandatory(true);
        inParam.setArray(false);
        inParam.setDatatype("xsd:string");

        RangeRestriction restriction1 = new RangeRestriction();
        restriction1.setMin(Double.valueOf(2.0));
        restriction1.setMax(Double.valueOf(10.0));
        inParam.setRestrictions(Arrays.asList(restriction1));
        service.setInputParameter(Arrays.asList(inParam));

        Parameter outputParameter = new Parameter();
        outputParameter.setDatatype("xsd:string");
        outputParameter.setArray(false);
        service.setOutputParameter(outputParameter);

        resourceValidateAndTranslate(service);
    }


    @Test
    public void testActuatorValidateAndCreate() {
        Actuator actuator = new Actuator();
        actuator.setId("mobile1");
        actuator.setLabels(Arrays.asList("Mobile 1"));
        actuator.setComments(Arrays.asList("This is mobile 1"));
        actuator.setLocatedAt("Paris");
        actuator.setInterworkingServiceURL("http://symbiote-h2020.eu/example/interworkingService");

        actuator.setCapabilities(Arrays.asList("actuatingService1Name"));

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
        List<Resource> resources = Arrays.asList(resource);
        manager.validateAndCreateBIMResourceToRDF(resources);
    }

}