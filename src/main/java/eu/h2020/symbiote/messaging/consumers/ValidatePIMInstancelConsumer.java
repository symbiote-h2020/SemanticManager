//package eu.h2020.symbiote.messaging.consumers;
//
//import com.fasterxml.jackson.core.JsonParseException;
//import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.rabbitmq.client.AMQP;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.DefaultConsumer;
//import com.rabbitmq.client.Envelope;
//import eu.h2020.symbiote.core.internal.PIMInstanceValidationResult;
//import eu.h2020.symbiote.core.internal.RDFInfo;
//import eu.h2020.symbiote.messaging.RabbitManager;
//import eu.h2020.symbiote.ontology.SemanticManager;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.io.IOException;
//
///**
// * RabbitMQ Consumer implementation used for Placeholder actions
// *
// * Created by Szymon Mueller
// */
//public class ValidatePIMInstancelConsumer extends DefaultConsumer {
//
//    private static Log log = LogFactory.getLog(ValidatePIMInstancelConsumer.class);
//    private SemanticManager semanticManager;
//
//    /**
//     * Constructs a new instance and records its association to the passed-in channel.
//     * Managers beans passed as parameters because of lack of possibility to inject it to consumer.
//     *
//     * @param channel           the channel to which this consumer is attached
//     * @param semanticManager   semantic manager
//     */
//    public ValidatePIMInstancelConsumer(Channel channel,
//                                        SemanticManager semanticManager) {
//        super(channel);
//        this.semanticManager = semanticManager;
//    }
//
//    /**
//     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
//     *
//     * @param consumerTag the <i>consumer tag</i> associated with the consumer
//     * @param envelope    packaging data for the message
//     * @param properties  content header data for the message
//     * @param body        the message body (opaque, client-specific byte array)
//     * @throws IOException if the consumer encounters an I/O error while processing the message
//     * @see Envelope
//     */
//    @Override
//    public void handleDelivery(String consumerTag, Envelope envelope,
//                               AMQP.BasicProperties properties, byte[] body)
//            throws IOException {
//        String msg = new String(body);
//        log.debug( "Consume validate PIM instance requested message: " + msg );
//
//        //Try to parse the message
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            RDFInfo validateRequest = mapper.readValue(msg, RDFInfo.class);
//
//            PIMInstanceValidationResult response = this.semanticManager.validatePIMInstance(validateRequest);
//            //Send the response back to the client
//            log.debug( "Validation status: " + response.isSuccess() + ", message: " + response.getMessage());
//
//            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response:"[]");
//
//            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
//                    .Builder()
//                    .correlationId(properties.getCorrelationId())
//                    .build();
//            this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
//            log.debug("-> Message was sent back");
//
//            this.getChannel().basicAck(envelope.getDeliveryTag(), false);
//
//        } catch( JsonParseException | JsonMappingException e ) {
//            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
//        } catch( IOException e ) {
//            log.error("I/O Exception occurred when parsing Resource object" , e);
//        } catch( Exception e ) {
//            log.error("Generic error ocurred when handling delivery" , e);
//        }
//    }
//}
