package eu.h2020.symbiote.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import eu.h2020.symbiote.core.internal.InformationModelListResponse;
import eu.h2020.symbiote.messaging.consumers.*;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.utils.LocationFinder;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import eu.h2020.symbiote.utils.LocationRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.apache.jena.vocabulary.RSS.channel;

/**
 * Bean used to manage internal communication using RabbitMQ.
 * It is responsible for declaring exchanges and using routing keys from centralized config server.
 * <p>
 * Created by Szymon Mueller
 */
@Component
public class RabbitManager {

    private static Log log = LogFactory.getLog(RabbitManager.class);

    @Value("${rabbit.host}")
    private String rabbitHost;
    @Value("${rabbit.username}")
    private String rabbitUsername;
    @Value("${rabbit.password}")
    private String rabbitPassword;

    //Platform ontology routing keys
    @Value("${rabbit.routingKey.platform.model.validationRequested}")
    private String platformModelValidationRequestedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.validationPerformed}")
    private String platformModelValidationPerformedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.created}")
    private String platformModelCreatedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.modified}")
    private String platformModelModifiedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.removed}")
    private String platformModelRemovedRoutingKey;
    @Value("${rabbit.routingKey.platform.instance.validationRequested}")
    private String platformInstanceValidationRequestedRoutingKey;
    @Value("${rabbit.routingKey.platform.instance.validationPerformed}")
    private String platformInstanceValidationPerformedRoutingKey;
    @Value("${rabbit.routingKey.platform.instance.translationRequested}")
    private String platformInstanceTranslationRequestedRoutingKey;
    @Value("${rabbit.routingKey.platform.instance.translationPerformed}")
    private String platformInstanceTranslationPerformedRoutingKey;

    @Value("${rabbit.routingKey.platform.model.allInformationModelsRequested}")
    private String platformInformationModelRequestedKey;

    //Resource ontology routing keys
    @Value("${rabbit.routingKey.resource.instance.validationRequested}")
    private String resourceInstanceValidationRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.validationPerformed}")
    private String resourceInstanceValidationPerformedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.translationRequested}")
    private String resourceInstanceTranslationRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.translationPerformed}")
    private String resourceInstanceTranslationPerformedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.resource.instance.translationRequested}")
    private String sspResourceInstanceTranslationRequestedRoutingKey;

    //Platform exchange
    @Value("${rabbit.exchange.platform.name}")
    private String platformExchangeName;
    @Value("${rabbit.exchange.platform.type}")
    private String platformExchangeType;
    @Value("${rabbit.exchange.platform.durable}")
    private boolean plaftormExchangeDurable;
    @Value("${rabbit.exchange.platform.autodelete}")
    private boolean platformExchangeAutodelete;
    @Value("${rabbit.exchange.platform.internal}")
    private boolean platformExchangeInternal;
    @Value("${rabbit.routingKey.platform.creationRequested}")
    private String platformCreationRequestedRoutingKey;
    @Value("${rabbit.routingKey.platform.created}")
    private String platformCreatedRoutingKey;
    @Value("${rabbit.routingKey.platform.modified}")
    private String platformModifiedRoutingKey;
    @Value("${rabbit.routingKey.platform.removed}")
    private String platformDeletedRoutingKey;

    //Resource exchange
    @Value("${rabbit.exchange.resource.name}")
    private String resourceExchangeName;
    @Value("${rabbit.exchange.resource.type}")
    private String resourceExchangeType;
    @Value("${rabbit.exchange.resource.durable}")
    private boolean resourceExchangeDurable;
    @Value("${rabbit.exchange.resource.autodelete}")
    private boolean resourceExchangeAutodelete;
    @Value("${rabbit.exchange.resource.internal}")
    private boolean resourceExchangeInternal;

    @Value("${rabbit.routingKey.resource.sparqlSearchRequested}")
    private String resourceSparqlSearchRequestedRoutingKey;

    @Value("${semantic.insert.whole.location.for.existing}")
    private boolean insertWholeLocation;


    private Connection connection;

    private final LocationRepository locationRepository;

    @Autowired
    public RabbitManager(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    /**
     * Initiates connection with Rabbit server using parameters from ConfigProperties
     *
     * @throws IOException
     * @throws TimeoutException
     */
    public Connection getConnection() throws IOException, TimeoutException {
        if (connection == null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(this.rabbitHost);
            factory.setUsername(this.rabbitUsername);
            factory.setPassword(this.rabbitPassword);
            this.connection = factory.newConnection();
        }
        return this.connection;
    }

    /**
     * Method creates channel and declares Rabbit exchanges.
     * It triggers start of all consumers used in Registry communication.
     */
    @PostConstruct
    public void init() {
        //FIXME check if there is better exception handling in @postconstruct method
        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();

            factory.setHost(this.rabbitHost);
            factory.setUsername(this.rabbitUsername);
            factory.setPassword(this.rabbitPassword);

            this.connection = factory.newConnection();

            channel = this.connection.createChannel();
            channel.exchangeDeclare(this.platformExchangeName,
                    this.platformExchangeType,
                    this.plaftormExchangeDurable,
                    this.platformExchangeAutodelete,
                    this.platformExchangeInternal,
                    null);

            channel.exchangeDeclare(this.resourceExchangeName,
                    this.resourceExchangeType,
                    this.resourceExchangeDurable,
                    this.resourceExchangeAutodelete,
                    this.resourceExchangeInternal,
                    null);

//            LocationFinder.getSingleton(this.resourceExchangeName, this.resourceSparqlSearchRequestedRoutingKey, this.connection, this);
            LocationFinder.getSingleton(this.insertWholeLocation,locationRepository);

            scheduleLoadingOfPIMs();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } finally {
            closeChannel(channel);
        }
    }

    /**
     * Cleanup method for rabbit - set on pre destroy
     */
    @PreDestroy
    public void cleanup() {
//        FIXME check if there is better exception handling in @predestroy method
//        log.info("Rabbit cleaned!");
//        try {
//            Channel channel;
//            if (this.connection != null && this.connection.isOpen()) {
//                channel = connection.createChannel();
//                channel.queueUnbind("placeholderQueue", this.placeholderExchangeName, this.placeholderRoutingKey);
//                channel.queueDelete("placeholderQueue");
//                closeChannel(channel);
//                this.connection.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            if (this.connection != null && this.connection.isOpen())
                this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method gathers all of the rabbit consumer starter methods
     */
    public void startConsumers(SemanticManager semanticManager) {
        log.debug("Starting consumers...");
        try {
//            registerPIMInstanceCreationConsumer(semanticManager);
            registerPIMMetaModelCreationConsumer(semanticManager);
            registerPIMMetaModelDeleteConsumer(semanticManager);
            registerPIMMetaModelModifyConsumer(semanticManager);
//            registerValidateAndCreateBIMPlatform(semanticManager);
            registerValidateAndCreateBIMResource(semanticManager);
            registerValidateAndCreateSspResource(semanticManager);
//            registerValidatePIMInstanceConsumer(semanticManager);
            registerValidatePIMMetaModelConsumer(semanticManager);
            registerValidateResourceInstanceConsumer(semanticManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void scheduleLoadingOfPIMs() {
        try {
            Channel tempChannel = connection.createChannel();

            String queueName = tempChannel.queueDeclare("symbIoTe-SemanticManager-pimsLookup", true, true, false, null).getQueue();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    try {
                        String response = sendRpcMessage(tempChannel, queueName, platformExchangeName, platformInformationModelRequestedKey, "", String.class.getCanonicalName());
                        ObjectMapper mapper = new ObjectMapper();
                        InformationModelListResponse informationModelsList = mapper.readValue(response, InformationModelListResponse.class);
                        SymbioteModelsUtil.addModels(informationModelsList.getBody());
                    } catch (IOException e) {
                        log.error("Error occurred when loading PIMs from registry");
                    }
                }
            };
            Timer timer = new Timer("pim download task",false);
            timer.schedule(task,30000);


        } catch (IOException e) {
            log.error("Error occurred when loading PIMs from registry");
        }

    }

    public void sendCustomMessage(String exchange, String routingKey, String objectInJson) {
        sendMessage(exchange, routingKey, objectInJson);
        log.info("- Custom message sent");
    }

    /**
     * Method publishes given message to the given exchange and routing key.
     * Props are set for correct message handle on the receiver side.
     *
     * @param exchange   name of the proper Rabbit exchange, adequate to topic of the communication
     * @param routingKey name of the proper Rabbit routing key, adequate to topic of the communication
     * @param message    message content in JSON String format
     */
    private void sendMessage(String exchange, String routingKey, String message) {
        AMQP.BasicProperties props;
        Channel channel = null;
        try {
            channel = this.connection.createChannel();
            props = new AMQP.BasicProperties()
                    .builder()
                    .contentType("application/json")
                    .build();

            channel.basicPublish(exchange, routingKey, props, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeChannel(channel);
        }
    }

    /**
     * Method used to send message via RPC (Remote Procedure Call) pattern.
     * In this implementation it covers asynchronous Rabbit communication with synchronous one, as it is used by conventional REST facade.
     * Before sending a message, a temporary response queue is declared and its name is passed along with the message.
     * When a consumer handles the message, it returns the result via the response queue.
     * Since this is a synchronous pattern, it uses timeout of 20 seconds. If the response doesn't come in that time, the method returns with null result.
     *
     * @param exchangeName name of the eschange to send message to
     * @param routingKey   routing key to send message to
     * @param message      message to be sent
     * @return response from the consumer or null if timeout occurs
     */
    public String sendRpcMessage(Channel channel, String responseQueueName, String exchangeName, String routingKey, String message, String classType) {
        try {
            log.info("Sending RPC message: " + message);

            String correlationId = UUID.randomUUID().toString();

            Map<String, Object> headers = new HashMap<>();
            headers.put("__TypeId__", classType);
            headers.put("__ContentTypeId__", Object.class.getCanonicalName());
//
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(correlationId)
                    .replyTo(responseQueueName)
                    .contentType("application/json")
                    .headers(headers)
                    .build();

            final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(correlationId)) {
                        log.debug("Got reply with correlationId: " + correlationId);
                        response.offer(new String(body, "UTF-8"));
                        getChannel().basicCancel(this.getConsumerTag());
                    } else {
                        log.debug("Got answer with wrong correlationId... should be " + correlationId + " but got " + properties.getCorrelationId());
                    }
                }
            };

            channel.basicConsume(responseQueueName, true, consumer);

            channel.basicPublish(exchangeName, routingKey, props, message.getBytes());

            String responseMsg = response.take();
            log.info("Response received: " + (responseMsg.length() > 200 ? responseMsg.substring(0,200) + (" ..."): responseMsg ) );
            return responseMsg;
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Closes given channel if it exists and is open.
     *
     * @param channel rabbit channel to close
     */
    private void closeChannel(Channel channel) {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a Validate PIM Meta Model consumer
     */
    public void registerValidatePIMMetaModelConsumer(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-PIM-MetaModel";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, platformExchangeName, platformModelValidationRequestedRoutingKey);
        ValidatePIMMetaModelConsumer consumer = new ValidatePIMMetaModelConsumer(channel, semanticManager);

        log.debug("Creating PIM meta model consumer");
        channel.basicConsume(queueName, false, consumer);
    }

//    /**
//     * Registers a Validate PIM Instance consumer
//     */
//    public void registerValidatePIMInstanceConsumer(SemanticManager semanticManager) throws IOException {
//        String queueName = "symbIoTe-SemanticManager-validate-PIM-Instance";
//
//        Channel channel = connection.createChannel();
//        channel.queueDeclare(queueName, true, false, false, null);
//        channel.queueBind(queueName, platformExchangeName, platformInstanceValidationRequestedRoutingKey);
//        ValidatePIMInstancelConsumer consumer = new ValidatePIMInstancelConsumer(channel, semanticManager);
//
//        log.debug("Creating PIM instance consumer");
//        channel.basicConsume(queueName, false, consumer);
//    }

    /**
     * Registers a Validate Resource Instance consumer
     */
    public void registerValidateResourceInstanceConsumer(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-Resource-Instance";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, resourceExchangeName, resourceInstanceValidationRequestedRoutingKey);
        ValidateResourcesInstanceConsumer consumer = new ValidateResourcesInstanceConsumer(channel, semanticManager);

        log.debug("Creating resource instance consumer");
        channel.basicConsume(queueName, false, consumer);
    }

//    /**
//     * Registers a Validate and Create BIM platform consumer
//     */
//    public void registerValidateAndCreateBIMPlatform(SemanticManager semanticManager) throws IOException {
//        String queueName = "symbIoTe-SemanticManager-validate-and-create-BIM-Platform";
//
//        Channel channel = connection.createChannel();
//        channel.queueDeclare(queueName, true, false, false, null);
//        channel.queueBind(queueName, platformExchangeName, platformInstanceTranslationRequestedRoutingKey);
//        ValidateAndCreateRDFForBIMPlatformConsumer consumer = new ValidateAndCreateRDFForBIMPlatformConsumer(channel, semanticManager);
//
//        log.debug("Creating BIM platform validation and creation consumer");
//        channel.basicConsume(queueName, false, consumer);
//    }

    /**
     * Registers a Validate and Create BIM resource consumer
     */
    public void registerValidateAndCreateBIMResource(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-and-create-BIM-Resource";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, resourceExchangeName, resourceInstanceTranslationRequestedRoutingKey);
        ValidateAndCreateRDFForBIMResourceConsumer consumer = new ValidateAndCreateRDFForBIMResourceConsumer(channel, semanticManager);

        log.debug("Creating BIM resource validation and creation consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a Validate and Create SSP resource consumer
     */
    public void registerValidateAndCreateSspResource(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-and-create-SSP-Resource";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, resourceExchangeName, sspResourceInstanceTranslationRequestedRoutingKey);
        ValidateAndCreateRDFForSspResourceConsumer consumer = new ValidateAndCreateRDFForSspResourceConsumer(channel, semanticManager);

        log.debug("Creating Ssp resource validation and creation consumer");
        channel.basicConsume(queueName, false, consumer);
    }

//    /**
//     * Registers a platform instance creation consumer
//     */
//    public void registerPIMInstanceCreationConsumer(SemanticManager semanticManager) throws IOException {
//        String queueName = "symbIoTe-SemanticManager-PIM-Instance-creation";
//
//        Channel channel = connection.createChannel();
//        channel.queueDeclare(queueName, true, false, false, null);
//        channel.queueBind(queueName, platformExchangeName, platformCreatedRoutingKey);
//        RegisterPIMInstanceConsumer consumer = new RegisterPIMInstanceConsumer(channel, semanticManager);
//
//        log.debug("Creating PIM platform instance consumer");
//        channel.basicConsume(queueName, false, consumer);
//    }

    /**
     * Registers a platform model creation consumer
     */
    public void registerPIMMetaModelCreationConsumer(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-PIM-MetaModel-creation";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, platformExchangeName, platformModelCreatedRoutingKey);
        RegisterPIMMetaModelConsumer consumer = new RegisterPIMMetaModelConsumer(channel, semanticManager);

        log.debug("Creating PIM Meta Model create consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a platform model creation consumer
     */
    public void registerPIMMetaModelDeleteConsumer(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-PIM-MetaModel-delete";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, platformExchangeName, platformModelRemovedRoutingKey);
        DeletePIMMetaModelConsumer consumer = new DeletePIMMetaModelConsumer(channel, semanticManager);

        log.debug("Creating PIM Meta Model delete consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a platform model creation consumer
     */
    public void registerPIMMetaModelModifyConsumer(SemanticManager semanticManager) throws IOException {
        String queueName = "symbIoTe-SemanticManager-PIM-MetaModel-modify";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, false, true, true, null);
        channel.queueBind(queueName, platformExchangeName, platformModelModifiedRoutingKey);
        ModifyPIMMetaModelConsumer consumer = new ModifyPIMMetaModelConsumer(channel, semanticManager);

        log.debug("Creating PIM Meta Model modify consumer");
        channel.basicConsume(queueName, false, consumer);
    }

}