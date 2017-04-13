package eu.h2020.symbiote;

import java.util.Arrays;
import java.util.List;

/**
 * Holds the constants used in the tests. Meant to be imported into tests.
 *
 * Created by Szymon Mueller on 12/04/2017.
 */
public class TestSetupConfig {

    public static final String GENERAL_INTERWORKING_URL = "http://symbiote-h2020.eu/example/interworkingService";
    public static final String GENERAL_FOI = "Room1";
    public static final String GENERAL_LOCATION = "Paris";

    public static final String STATIONARY1_ID = "stationary1";
    public static final List<String> STATIONARY1_LABELS = Arrays.asList("Stationary 1");
    public static final List<String> STATIONARY1_COMMENTS = Arrays.asList("This is stationary 1");
    public static final String STATIONARY1_URL = GENERAL_INTERWORKING_URL;
    public static final String STATIONARY1_LOCATION = GENERAL_LOCATION;
    public static final String STATIONARY1_FOI = GENERAL_FOI;
    public static final List<String> STATIONARY1_PROPERTIES = Arrays.asList("Temperature","Humidity");

    public static final String MOBILE1_ID = "mobile1";
    public static final List<String> MOBILE1_LABELS = Arrays.asList("Mobile 1");
    public static final List<String> MOBILE1_COMMENTS = Arrays.asList("This is mobile 1");
    public static final String MOBILE1_URL = GENERAL_INTERWORKING_URL;
    public static final String MOBILE1_LOCATION = GENERAL_LOCATION;
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
    public static final String ACTUATING_SERVICE1_ACTSON = GENERAL_FOI;
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
    public static final String ACTUATOR1_LOCATION = GENERAL_LOCATION;
    public static final List<String> ACTUATOR1_CAPABILITIES = Arrays.asList(ACTUATING_SERVICE1_NAME);


}
