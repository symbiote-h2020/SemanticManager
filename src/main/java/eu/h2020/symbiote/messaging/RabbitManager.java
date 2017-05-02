package eu.h2020.symbiote.messaging;

import com.rabbitmq.client.*;
import eu.h2020.symbiote.messaging.consumers.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

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

    //Resource ontology routing keys
    @Value("${rabbit.routingKey.resource.instance.validationRequested}")
    private String resourceInstanceValidationRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.validationPerformed}")
    private String resourceInstanceValidationPerformedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.translationRequested}")
    private String resourceInstanceTranslationRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.instance.translationPerformed}")
    private String resourceInstanceTranslationPerformedRoutingKey;

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

    private Connection connection;

    @Autowired
    public RabbitManager() {
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
    public void init() {
        //FIXME check if there is better exception handling in @postconstruct method
        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost("127.0.0.1"); //todo value from properties
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


            startConsumers();
            //message retrieval
            //receiveMessages();

            // message to Search Service

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
    public void startConsumers() {
        log.debug("Starting consumers...");
        try {
            registerPIMInstanceCreationConsumer();
            registerPIMMetaModelCreationConsumer();
            registerValidateAndCreateBIMPlatform();
            registerValidateAndCreateBIMResource();
            registerValidatePIMInstanceConsumer();
            registerValidatePIMMetaModelConsumer();
            registerValidateResourceInstanceConsumer();
        } catch (IOException e) {
            e.printStackTrace();
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
    private void registerValidatePIMMetaModelConsumer() throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-PIM-MetaModel";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, platformExchangeName, platformModelValidationRequestedRoutingKey);
        ValidatePIMMetaModelConsumer consumer = new ValidatePIMMetaModelConsumer(channel, this);

        log.debug("Creating PIM meta model consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a Validate PIM Instance consumer
     */
    private void registerValidatePIMInstanceConsumer() throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-PIM-Instance";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, platformExchangeName, platformInstanceValidationRequestedRoutingKey);
        ValidatePIMInstancelConsumer consumer = new ValidatePIMInstancelConsumer(channel, this);

        log.debug("Creating PIM instance consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a Validate Resource Instance consumer
     */
    private void registerValidateResourceInstanceConsumer() throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-Resource-Instance";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, resourceExchangeName, resourceInstanceValidationRequestedRoutingKey);
        ValidateResourcesInstanceConsumer consumer = new ValidateResourcesInstanceConsumer(channel, this);

        log.debug("Creating resource instance consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a Validate and Create BIM platform consumer
     */
    private void registerValidateAndCreateBIMPlatform() throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-and-create-BIM-Platform";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, platformExchangeName, platformInstanceTranslationRequestedRoutingKey);
        ValidateAndCreateRDFForBIMPlatformConsumer consumer = new ValidateAndCreateRDFForBIMPlatformConsumer(channel, this);

        log.debug("Creating BIM platform validation and creation consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a Validate and Create BIM resource consumer
     */
    private void registerValidateAndCreateBIMResource() throws IOException {
        String queueName = "symbIoTe-SemanticManager-validate-and-create-BIM-Resource";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, resourceExchangeName, resourceInstanceTranslationRequestedRoutingKey);
        ValidateAndCreateRDFForBIMResourceConsumer consumer = new ValidateAndCreateRDFForBIMResourceConsumer(channel, this);

        log.debug("Creating BIM resource validation and creation consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a platform instance creation consumer
     */
    private void registerPIMInstanceCreationConsumer() throws IOException {
        String queueName = "symbIoTe-SemanticManager-PIM-Instance-creation";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, platformExchangeName, platformCreatedRoutingKey);
        RegisterPIMInstanceConsumer consumer = new RegisterPIMInstanceConsumer(channel, this);

        log.debug("Creating PIM platform instance consumer");
        channel.basicConsume(queueName, false, consumer);
    }

    /**
     * Registers a platform model creation consumer
     */
    private void registerPIMMetaModelCreationConsumer() throws IOException {
        String queueName = "symbIoTe-SemanticManager-PIM-MetaModel-creation";

        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, platformExchangeName, platformModelCreatedRoutingKey);
        RegisterPIMMetaModelConsumer consumer = new RegisterPIMMetaModelConsumer(channel, this);

        log.debug("Creating PIM Meta Model consumer");
        channel.basicConsume(queueName, false, consumer);
    }

}