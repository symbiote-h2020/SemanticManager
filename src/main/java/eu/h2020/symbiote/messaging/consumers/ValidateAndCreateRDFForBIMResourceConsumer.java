package eu.h2020.symbiote.messaging.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.internal.CoreResourceRegistryRequest;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationResult;
import eu.h2020.symbiote.messaging.RabbitManager;
import eu.h2020.symbiote.ontology.SemanticManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * RabbitMQ Consumer implementation used for Placeholder actions
 * <p>
 * Created by Szymon Mueller
 */
public class ValidateAndCreateRDFForBIMResourceConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ValidateAndCreateRDFForBIMResourceConsumer.class);
    private RabbitManager rabbitManager;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     * Managers beans passed as parameters because of lack of possibility to inject it to consumer.
     *
     * @param channel       the channel to which this consumer is attached
     * @param rabbitManager rabbit manager bean passed for access to messages manager
     */
    public ValidateAndCreateRDFForBIMResourceConsumer(Channel channel,
                                                      RabbitManager rabbitManager) {
        super(channel);
        this.rabbitManager = rabbitManager;
    }

    /**
     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param envelope    packaging data for the message
     * @param properties  content header data for the message
     * @param body        the message body (opaque, client-specific byte array)
     * @throws IOException if the consumer encounters an I/O error while processing the message
     * @see Envelope
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String msg = new String(body);
        log.debug("Consume validate and create RDF for resource " + msg);
        ObjectMapper mapper = new ObjectMapper();
        ResourceInstanceValidationResult response = null;
        //Try to parse the message
        try {
//            List<Resource> validateAndTranslateRequest = mapper.readValue(msg, new TypeReference<List<Resource>>(){});

            CoreResourceRegistryRequest coreResourceRegistryRequest = mapper.readValue(msg, CoreResourceRegistryRequest.class);

            response = SemanticManager.getManager().validateAndCreateBIMResourceToRDF(coreResourceRegistryRequest);
            //Send the response back to the client
            log.debug("Validation status: " + response.isSuccess() + ", message: " + response.getMessage());

//        } catch( JsonParseException | JsonMappingException e ) {
//            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
//        } catch( IOException e ) {
//            log.error("I/O Exception occurred when parsing Resource object" , e);
//        } catch( PropertyNotFoundException e) {
//            log.error("Could not find property: " + e.getMessage() , e);
        } catch (Exception e) {
            response = createResponseForError(e);
            log.error("Generic error occurred when handling delivery", e);
        }
        byte[] responseBytes = mapper.writeValueAsBytes(response != null ? response : "[]");

        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(properties.getCorrelationId())
                .build();
        this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
        log.debug("-> Message was sent back");

        this.getChannel().basicAck(envelope.getDeliveryTag(), false);
    }

    private ResourceInstanceValidationResult createResponseForError(Exception error) {
        ResourceInstanceValidationResult response = new ResourceInstanceValidationResult();
        response.setMessage(error.getMessage());
        response.setModelValidated("");
        response.setModelValidatedAgainst("");
        response.setObjectDescription(null);
        response.setSuccess(false);
        return response;
    }

}
