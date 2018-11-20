package eu.h2020.symbiote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.DescriptionType;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationRequest;
import eu.h2020.symbiote.messaging.RabbitManager;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.cim.StationarySensor;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.security.accesspolicies.common.AccessPolicyType;
import eu.h2020.symbiote.security.accesspolicies.common.IAccessPolicySpecifier;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Mael on 06/02/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagingTests {
    public static final String PLATFORM_MODEL_REMOVED_RK = "platformModelRemovedRK";
    public static final String PLATFORM_EXCHANGE_NAME = "platformExchangeName";
    public static final String IM_ID = "IM_ID";
    public static final String IM_NAME = "im_name";
    public static final String IM_OWNER = "im_owner";
    public static final String IM_URI = "http://www.symbiote-h2020.eu/ontology/im_id";
    public static final String IM_RDF = "RDF";
    public static final RDFFormat IM_FORMAT = RDFFormat.JSONLD;
    public static final String PLATFORM_MODEL_MODIFIED_RK = "platformModelModifiedRK";
    public static final String PLATFORM_MODEL_CREATED_RK = "platformModelCreatedRK";
    public static final String PLATFORM_CREATED_ROUTING_KEY = "platformCreatedRoutingKey";
    public static final String RESOURCE_EXCHANGE_NAME = "resourceExchangeName";
    public static final String RESOURCE_INSTANCE_TRANSLATION_REQUESTED_ROUTING_KEY = "resourceInstanceTranslationRequestedRoutingKey";
    public static final String PLATFORM_MODEL_VALIDATION_REQUESTED_RK = "platformModelValidationRequestedRK";
    public static final String RESOURCE_INSTANCE_VALIDATION_REQUESTED_ROUTING_KEY = "resourceInstanceValidationRequestedRoutingKey";

//    public static final String PLATFORM_EXCHANGE_NAME = "symbiote.platform";
//    public static final String PLATFORM_CREATED = "platform.created";
//    public static final String PLATFORM_MODIFIED = "platform.modified";
//    public static final String PLATFORM_DELETED = "platform.removed";
//    public static final String RESOURCE_EXCHANGE_NAME = "symbiote.resource";
//    public static final String RESOURCE_CREATED = "resource.created";
//    public static final String RESOURCE_MODIFIED = "resource.modified";
//    public static final String RESOURCE_DELETED = "resource.removed";
//    public static final String SEARCH_REQUESTED = "resource.searchRequested";
//    public static final String SEARCH_PERFORMED = "resource.searchPerformed";
//    public static final String SPARQL_REQUESTED = "resource.sparqlRequested";
//    public static final String SPARQL_PERFORMED = "resource.sparqlPerformed";
//    public static final String EXCHANGE_SEARCH = "symbiote.search";
//    public static final String POPULARITY_RK = "symbiote.popularity.rk";

    @Mock
    private SemanticManager semanticManager;

    @InjectMocks
    private RabbitManager rabbitManager;


    @Before
    public void setup() {
        ReflectionTestUtils.setField(rabbitManager, "rabbitHost", "localhost");
        ReflectionTestUtils.setField(rabbitManager, "rabbitUsername", "guest");
        ReflectionTestUtils.setField(rabbitManager, "rabbitPassword", "guest");

        ReflectionTestUtils.setField(rabbitManager, "platformModelValidationRequestedRoutingKey", PLATFORM_MODEL_VALIDATION_REQUESTED_RK);
        ReflectionTestUtils.setField(rabbitManager, "platformModelValidationPerformedRoutingKey", "platformModelValidationPerformedRK");
        ReflectionTestUtils.setField(rabbitManager, "platformModelCreatedRoutingKey", PLATFORM_MODEL_CREATED_RK);
        ReflectionTestUtils.setField(rabbitManager, "platformModelModifiedRoutingKey", PLATFORM_MODEL_MODIFIED_RK);
        ReflectionTestUtils.setField(rabbitManager, "platformModelRemovedRoutingKey", PLATFORM_MODEL_REMOVED_RK);
        ReflectionTestUtils.setField(rabbitManager, "platformInstanceValidationRequestedRoutingKey", "platformInstanceValidationReqRK");
        ReflectionTestUtils.setField(rabbitManager, "platformInstanceValidationPerformedRoutingKey", "platformInstanceValidationPerformedRK");
        ReflectionTestUtils.setField(rabbitManager, "platformInstanceTranslationRequestedRoutingKey", "platformInstanceTranslationRequestedRK");
        ReflectionTestUtils.setField(rabbitManager, "platformInstanceTranslationPerformedRoutingKey", "platformInstanceTranslationPerformedRK");
        ReflectionTestUtils.setField(rabbitManager, "platformInformationModelRequestedKey", "platformInformationModelRequestedRK");

        ReflectionTestUtils.setField(rabbitManager, "resourceInstanceValidationRequestedRoutingKey", RESOURCE_INSTANCE_VALIDATION_REQUESTED_ROUTING_KEY);
        ReflectionTestUtils.setField(rabbitManager, "resourceInstanceValidationPerformedRoutingKey", "resourceInstanceValidationPerformedRoutingKey");
        ReflectionTestUtils.setField(rabbitManager, "resourceInstanceTranslationRequestedRoutingKey", RESOURCE_INSTANCE_TRANSLATION_REQUESTED_ROUTING_KEY);
        ReflectionTestUtils.setField(rabbitManager, "resourceInstanceTranslationPerformedRoutingKey", "resourceInstanceTranslationPerformedRoutingKey");

        ReflectionTestUtils.setField(rabbitManager, "platformExchangeName", PLATFORM_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "plaftormExchangeDurable", false);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeAutodelete", true);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeInternal", false);
        ReflectionTestUtils.setField(rabbitManager, "platformCreationRequestedRoutingKey", "platformCreationRequestedRoutingKey");
        ReflectionTestUtils.setField(rabbitManager, "platformCreatedRoutingKey", PLATFORM_CREATED_ROUTING_KEY);
        ReflectionTestUtils.setField(rabbitManager, "platformModifiedRoutingKey", "platformModifiedRoutingKey");
        ReflectionTestUtils.setField(rabbitManager, "platformDeletedRoutingKey", "platformDeletedRoutingKey");


        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeName", RESOURCE_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeDurable", false);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeAutodelete", true);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeInternal", false);
        ReflectionTestUtils.setField(rabbitManager, "rabbitMessageTimeout", 30000);


        ReflectionTestUtils.setField(rabbitManager, "resourceSparqlSearchRequestedRoutingKey", "resourceSparqlSearchRequestedRoutingKey");


        ReflectionTestUtils.invokeMethod(rabbitManager, "init");
    }

    @After
    public void teardown() {
        ReflectionTestUtils.invokeMethod(rabbitManager, "cleanup");
    }

    @Test
    public void testDeletePIMMetaModelCalled() {
        try {
            rabbitManager.registerPIMMetaModelDeleteConsumer(semanticManager);

            ObjectMapper mapper = new ObjectMapper();
            InformationModel im = createInformationModel();
            String jsonIm = mapper.writeValueAsString(im);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_MODEL_REMOVED_RK, null, jsonIm);
            Thread.sleep(1000);
            ArgumentCaptor<InformationModel> imCaptor = ArgumentCaptor.forClass(InformationModel.class);
            verify(semanticManager, times(1)).deletePIMMetaModel(imCaptor.capture());
            assertNotNull(imCaptor.getValue());
            compareMetaModels(im,imCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testModifyPIMMetaModelCalled() {
        try {
            rabbitManager.registerPIMMetaModelModifyConsumer(semanticManager);

            ObjectMapper mapper = new ObjectMapper();
            InformationModel im = createInformationModel();
            String jsonIm = mapper.writeValueAsString(im);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_MODEL_MODIFIED_RK, null, jsonIm);
            Thread.sleep(1000);
            ArgumentCaptor<InformationModel> imCaptor = ArgumentCaptor.forClass(InformationModel.class);
            verify(semanticManager, times(1)).modifyPIMMetaModel(imCaptor.capture());
            assertNotNull(imCaptor.getValue());
            compareMetaModels(im,imCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testRegisterPIMMetaModelCalled() {
        try {
            rabbitManager.registerPIMMetaModelCreationConsumer(semanticManager);

            ObjectMapper mapper = new ObjectMapper();
            InformationModel im = createInformationModel();
            String jsonIm = mapper.writeValueAsString(im);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_MODEL_CREATED_RK, null, jsonIm);
            Thread.sleep(1000);
            ArgumentCaptor<InformationModel> imCaptor = ArgumentCaptor.forClass(InformationModel.class);
            verify(semanticManager, times(1)).registerNewPIMMetaModel(imCaptor.capture());
            assertNotNull(imCaptor.getValue());
            compareMetaModels(im,imCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidateAndCreateRDFForBIMResourceCalled() {
        try {
            rabbitManager.registerValidateAndCreateBIMResource(semanticManager);

            ObjectMapper mapper = new ObjectMapper();

            Map<String, IAccessPolicySpecifier> filteringPolicies = new HashMap<>();
            String resId= "res_id";
            filteringPolicies.put(resId,new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC,null));
            SecurityRequest securityRequest = new SecurityRequest("test1");
            Map<String, Resource> resMap = new HashMap<>();
            Resource stationarySensor = new StationarySensor();
            stationarySensor.setId(STATIONARY1_ID);
            stationarySensor.setName(STATIONARY1_LABEL);
            stationarySensor.setDescription(STATIONARY1_COMMENTS);
            ((StationarySensor)stationarySensor).setLocatedAt(STATIONARY1_LOCATION);
            stationarySensor.setInterworkingServiceURL(STATIONARY1_URL);
            ((StationarySensor)stationarySensor).setFeatureOfInterest(STATIONARY1_FOI);
            ((StationarySensor)stationarySensor).setObservesProperty(STATIONARY1_PROPERTIES);

//            String sensorString = mapper.writeValueAsString(stationarySensor);
//
            resMap.put(resId,stationarySensor);
            ObjectWriter objectWriter = mapper.writerFor(new TypeReference<Map<String, Resource>>() {
            });
            String bodyToSend = objectWriter.writeValueAsString(resMap);

            CoreResourceRegistryRequest request = new CoreResourceRegistryRequest(securityRequest,bodyToSend, DescriptionType.BASIC,"platform_id",filteringPolicies);


            String jsonIm = mapper.writeValueAsString(request);
//
//            CoreResourceRegistryRequest coreResourceRegistryRequest = mapper.readValue(jsonIm, CoreResourceRegistryRequest.class);
//
//            String mapBody = coreResourceRegistryRequest.getBody();
//
//            Map<String, Resource> resources = null;
//            resources = mapper.readValue(mapBody, new TypeReference<Map<String, Resource>>() {
//            });

            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_INSTANCE_TRANSLATION_REQUESTED_ROUTING_KEY, null, jsonIm);
            Thread.sleep(1000);
//            ArgumentCaptor<CoreResourceRegistryRequest> imCaptor = ArgumentCaptor.forClass(CoreResourceRegistryRequest.class);
            ArgumentCaptor<Map> imCaptor = ArgumentCaptor.forClass(Map.class);

            verify(semanticManager, times(1)).validateAndCreateBIMResourceToRDF(imCaptor.capture(),anyString(),anyBoolean());

            Map<String,Resource> value = imCaptor.<Map<String, Resource>>getValue();
            assertNotNull(value);
            assertEquals("Captured resource size must be 1", 1, value.size());
            assertEquals("Captured resource name must be same", STATIONARY1_LABEL, value.values().iterator().next().getName());

//            assertEquals("Captured object must have the same security request", securityRequest , value.getSecurityRequest());
//            assertTrue("Captured object must have filtering policies for resource", imCaptor.getValue().getFilteringPolicies().containsKey(resId));
//            assertEquals("Captured object must have the same filtering policies type", filteringPolicies.get(resId).getPolicyType(),imCaptor.getValue().getFilteringPolicies().get(resId).getPolicyType());
//            assertEquals("Captured object must have the same filtering policies claims", filteringPolicies.get(resId).getPolicyType(),imCaptor.getValue().getFilteringPolicies().get(resId).getPolicyType());
//            assertEquals("Captured object must have the same description type", request.getDescriptionType(), imCaptor.getValue().getDescriptionType());
//            assertEquals("Captured object must have the same platform id", request.getPlatformId(), imCaptor.getValue().getPlatformId());
//            assertEquals("Captured object must have the same body", request.getBody(), );

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InvalidArgumentsException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidatePIMMetaModelCalled() {
        try {
            rabbitManager.registerValidatePIMMetaModelConsumer(semanticManager);

            ObjectMapper mapper = new ObjectMapper();
            InformationModel im = createInformationModel();
            String jsonIm = mapper.writeValueAsString(im);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_MODEL_VALIDATION_REQUESTED_RK , null, jsonIm);
            Thread.sleep(1000);
            ArgumentCaptor<InformationModel> imCaptor = ArgumentCaptor.forClass(InformationModel.class);
            verify(semanticManager, times(1)).validatePIMMetaModel(imCaptor.capture());
            assertNotNull(imCaptor.getValue());

            compareMetaModels(im,imCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testValidateResourceInstanceCalled() {
        try {
            rabbitManager.registerValidateResourceInstanceConsumer(semanticManager);

            ObjectMapper mapper = new ObjectMapper();
            ResourceInstanceValidationRequest request = new ResourceInstanceValidationRequest();
            request.setInformationModelId("BIM");
            request.setInterworkingServiceURL("http://interworkingService.url");
            request.setRdf("RDF");
            request.setRdfFormat(RDFFormat.JSONLD);

            String jsonIm = mapper.writeValueAsString(request);
            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_INSTANCE_VALIDATION_REQUESTED_ROUTING_KEY, null, jsonIm);
            Thread.sleep(1000);
            ArgumentCaptor<ResourceInstanceValidationRequest> imCaptor = ArgumentCaptor.forClass(ResourceInstanceValidationRequest.class);
            verify(semanticManager, times(1)).validateResourcesInstance(imCaptor.capture());
            assertNotNull(imCaptor.getValue());

            assertEquals("Captured object must have the same informationModel", request.getInformationModelId(), imCaptor.getValue().getInformationModelId() );
            assertEquals("Captured object must have the same interworking service", request.getInterworkingServiceURL(), imCaptor.getValue().getInterworkingServiceURL() );
            assertEquals("Captured object must have the same rdf", request.getRdf(), imCaptor.getValue().getRdf() );
            assertEquals("Captured object must have the same rdf format", request.getRdfFormat(), imCaptor.getValue().getRdfFormat() );

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void sendMessage(String exchange, String routingKey, AMQP.BasicProperties properties, String message) {
        Channel channel = null;
        try {
            channel = rabbitManager.getConnection().createChannel();
            channel.basicPublish(exchange, routingKey, properties, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail();
        }
    }

    private InformationModel createInformationModel() {
        InformationModel im = new InformationModel();
        im.setId(IM_ID);
        im.setName(IM_NAME);
        im.setOwner(IM_OWNER);
        im.setUri(IM_URI);
        im.setRdf(IM_RDF);
        im.setRdfFormat(IM_FORMAT);
        return im;
    }

    private void compareMetaModels( InformationModel informationModel1, InformationModel informationModel2) {
        assertEquals("Captured object must have the same id", informationModel1.getId(), informationModel2.getId());
        assertEquals("Captured object must have the same name", informationModel1.getName(),informationModel2.getName());
        assertEquals("Captured object must have the same owner", informationModel1.getOwner(), informationModel2.getOwner());
        assertEquals("Captured object must have the same uri", informationModel1.getUri(), informationModel2.getUri());
        assertEquals("Captured object must have the same rdf", informationModel1.getRdf(), informationModel2.getRdf());
        assertEquals("Captured object must have the same rdf format", informationModel1.getRdfFormat(), informationModel2.getRdfFormat() );
    }

}













