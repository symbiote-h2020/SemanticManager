package eu.h2020.symbiote;

import eu.h2020.symbiote.core.model.Location;
import eu.h2020.symbiote.core.model.WGS84Location;
import eu.h2020.symbiote.core.model.resources.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds the constants used in the tests. Meant to be imported into tests.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class TestSetupConfig {

    public static final String PLATFORM_ID = "Platform1";

    public static final String GENERAL_INTERWORKING_URL = "http://symbiote-h2020.eu/example/interworkingService";
    public static final String GENERAL_FOI_NAME = "Room1";
    public static final String GENERAL_FOI_DESC = "This is room 1";
    public static final FeatureOfInterest GENERAL_FOI = new FeatureOfInterest();
    static {
        GENERAL_FOI.setLabels(Arrays.asList(GENERAL_FOI_NAME));
        GENERAL_FOI.setComments(Arrays.asList(GENERAL_FOI_DESC));
        GENERAL_FOI.setHasProperty(Arrays.asList("Temperature","Humidity"));
    }

    public static final String GENERAL_LOCATION_NAME = "Paris";
    public static final String GENERAL_LOCATION_DESCRIPTION = "This is paris";
    public static final double GENERAL_LOCATION_LAT = 48.864716d;
    public static final double GENERAL_LOCATION_LONG = 2.349014d;
    public static final double GENERAL_LOCATION_ALT = 15d;
    public static final Location GENERAL_LOCATION = new WGS84Location( GENERAL_LOCATION_LONG,GENERAL_LOCATION_LAT,GENERAL_LOCATION_ALT,GENERAL_LOCATION_NAME,GENERAL_LOCATION_DESCRIPTION );

    public static final String STATIONARY1_ID = "stationary1";
    public static final List<String> STATIONARY1_LABELS = Arrays.asList("Stationary 1");
    public static final List<String> STATIONARY1_COMMENTS = Arrays.asList("This is stationary 1");
    public static final String STATIONARY1_URL = GENERAL_INTERWORKING_URL;
    public static final Location STATIONARY1_LOCATION = GENERAL_LOCATION;
    public static final FeatureOfInterest STATIONARY1_FOI = GENERAL_FOI;
    public static final List<String> STATIONARY1_PROPERTIES = Arrays.asList("Temperature","Humidity");

    public static final String MOBILE1_ID = "mobile1";
    public static final List<String> MOBILE1_LABELS = Arrays.asList("Mobile 1");
    public static final List<String> MOBILE1_COMMENTS = Arrays.asList("This is mobile 1");
    public static final String MOBILE1_URL = GENERAL_INTERWORKING_URL;
    public static final Location MOBILE1_LOCATION = GENERAL_LOCATION;
    public static final List<String> MOBILE1_PROPERTIES = Arrays.asList("Temperature");

    public static final String SERVICE1_ID = "service1";
    public static final List<String> SERVICE1_LABELS = Arrays.asList("Service 1");
    public static final List<String> SERVICE1_COMMENTS = Arrays.asList("This is service 1");
    public static final String SERVICE1_URL = GENERAL_INTERWORKING_URL;
    public static final String SERVICE1_NAME = "service1Name";
    public static final String SERVICE1_INPUT_NAME = "inputParam1";
    public static final boolean SERVICE1_INPUT_MANDATORY = true;
    public static final boolean SERVICE1_INPUT_ARRAY = false;
    public static final String SERVICE1_INPUT_DATATYPE = "xsd:string";
    public static final Double SERVICE1_INPUT_RESTRICTION_MIN = Double.valueOf(2.0);
    public static final Double SERVICE1_INPUT_RESTRICTION_MAX = Double.valueOf(10.0);
    public static final boolean SERVICE1_OUTPUT_ARRAY = false;
    public static final String SERVICE1_OUTPUT_DATATYPE = "xsd:string";

    public static final String ACTUATING_SERVICE1_ID = "actuatingService1";
    public static final List<String> ACTUATING_SERVICE1_LABELS = Arrays.asList("Actuating Service 1");
    public static final List<String> ACTUATING_SERVICE1_COMMENTS = Arrays.asList("This is actuating service 1");
    public static final String ACTUATING_SERVICE1_URL = GENERAL_INTERWORKING_URL;
    public static final String ACTUATING_SERVICE1_NAME = "actuatingService1Name";
    public static final FeatureOfInterest ACTUATING_SERVICE1_ACTSON = GENERAL_FOI;
    public static final List<String> ACTUATING_SERVICE1_AFFECTS = Arrays.asList("Temperature");
    public static final String ACTUATING_SERVICE1_INPUT_NAME = "inputParam1";
    public static final boolean ACTUATING_SERVICE1_INPUT_MANDATORY = true;
    public static final boolean ACTUATING_SERVICE1_INPUT_ARRAY = false;
    public static final String ACTUATING_SERVICE1_INPUT_DATATYPE = "xsd:string";
    public static final Double ACTUATING_SERVICE1_INPUT_RESTRICTION_MIN = Double.valueOf(2.0);
    public static final Double ACTUATING_SERVICE1_INPUT_RESTRICTION_MAX = Double.valueOf(10.0);
    public static final boolean ACTUATING_SERVICE1_OUTPUT_ARRAY = false;
    public static final String ACTUATING_SERVICE1_OUTPUT_DATATYPE = "xsd:string";

    public static final String ACTUATOR1_ID = "actuator1";
    public static final List<String> ACTUATOR1_LABELS = Arrays.asList("Actuator 1");
    public static final List<String> ACTUATOR1_COMMENTS = Arrays.asList("This is actuator 1");
    public static final String ACTUATOR1_URL = GENERAL_INTERWORKING_URL;
    public static final Location ACTUATOR1_LOCATION = GENERAL_LOCATION;
    public static final List<ActuatingService> ACTUATOR1_CAPABILITIES = new ArrayList<>();

    public static final String MOBILEDEVICE1_ID = "mobiledevice1";
    public static final List<String> MOBILEDEVICE1_LABELS = Arrays.asList("Mobile device 1");
    public static final List<String> MOBILEDEVICE1_COMMENTS = Arrays.asList("This is mobile device 1");
    public static final String MOBILEDEVICE1_URL = GENERAL_INTERWORKING_URL;
    public static final Location MOBILEDEVICE1_LOCATION = GENERAL_LOCATION;
    public static final List<String> MOBILEDEVICE1_PROPERTIES = Arrays.asList("Temperature");
    public static final List<ActuatingService> MOBILEDEVICE1_CAPABILITIES = new ArrayList<>();

    public static final String STATIONARYDEVICE1_ID = "stationardevice1";
    public static final List<String> STATIONARYDEVICE1_LABELS = Arrays.asList("Stationary device 1");
    public static final List<String> STATIONARYDEVICE1_COMMENTS = Arrays.asList("This is stationary device 1");
    public static final String STATIONARYDEVICE1_URL = GENERAL_INTERWORKING_URL;
    public static final Location STATIONARYDEVICE1_LOCATION = GENERAL_LOCATION;
    public static final FeatureOfInterest STATIONARYDEVICE1_FOI = GENERAL_FOI;
    public static final List<String> STATIONARYDEVICE1_PROPERTIES = Arrays.asList("Temperature","Humidity");
    public static final List<ActuatingService> STATIONARYDEVICE1_CAPABILITIES = new ArrayList<>();


    static {
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
        ACTUATOR1_CAPABILITIES.add(service);
        MOBILEDEVICE1_CAPABILITIES.add(service);
        STATIONARYDEVICE1_CAPABILITIES.add(service);
    }

}
