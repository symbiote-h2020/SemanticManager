package eu.h2020.symbiote.ontology.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import eu.h2020.symbiote.core.ci.SparqlQueryOutputFormat;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.core.model.Location;
import eu.h2020.symbiote.core.model.WGS84Location;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for finding existing locations to reuse the URIs
 *
 * Created by Szymon Mueller on 05/05/2017.
 */
public class LocationFinder {

    private static final Log log = LogFactory.getLog(LocationFinder.class);

    private static LocationFinder singleton = null;

    private final String resourceExchange;

    private final String sparqlBindingKey;

    private final Channel channel;

    public LocationFinder(String resourceExchange, String sparqlBindingKey, Channel channel ) {
        this.resourceExchange = resourceExchange;
        this.sparqlBindingKey = sparqlBindingKey;
        this.channel = channel;
    }

    public static LocationFinder getSingleton(String resourceExchange, String sparqlBindingKey, Channel channel) {
        synchronized( LocationFinder.class ) {
            if( singleton == null ) {
                singleton = new LocationFinder(resourceExchange, sparqlBindingKey, channel);
            }
            return singleton;
        }
    }

    public static LocationFinder getSingleton() {
        return singleton;
    }

    public String queryForLocationUri(Location location, String platformId ) {
        String uri = null;
        String query = null;

        if( location instanceof WGS84Location ) {
            query = getWGS84SparqlQuery((WGS84Location) location, platformId);
        }

        //TODO other queries for WKTLocation and symbolic location

        if( query != null ) {
            String result = executeLocationQuery(query);

            BufferedReader reader = new BufferedReader(new StringReader(result));
            try {
                String l1 = reader.readLine();
                String l2 = reader.readLine();
                String l3 = reader.readLine();
                if (l1.equals("location")) {
                    if (l3 != null) {
                        log.error("Found more than one locations: <" + l2 + "> and <" + l3 + ">");
                    } else if( l2 == null) {
                        log.debug("Could not find any locations for the parameters");
                    } else {
                        log.debug("Found location fulfilling the criteria! LocationURI: " + l2);
                        uri = l2;
                    }
                } else {
                    log.error("Location query returned different value (instead of location): " + l1);
                }
                reader.close();
            } catch (Exception e) {
                log.error("Exception occurred when executing location query: " + e.getMessage(), e);
            }
        }

        return uri;
    }

    public String executeLocationQuery( String query ) {
        try {
            log.info("Creating location sparql query");
            ObjectMapper mapper = new ObjectMapper();
            CoreSparqlQueryRequest request = new CoreSparqlQueryRequest();
            request.setQuery(query);
            //TODO set proper token
            request.setToken("Jakistoken");
            request.setOutputFormat(SparqlQueryOutputFormat.CSV);

            String message = mapper.writeValueAsString(request);
            String response = sendRpcMessage(this.resourceExchange, this.sparqlBindingKey, message, request.getClass().getCanonicalName());
            if (response == null)
                return null;
            return mapper.readValue(response, String.class);
        } catch (IOException e) {
            log.error("Error when querying locations using sparql: " + e.getMessage(), e );
        }
        return null;
    }


    private String getWGS84SparqlQuery( WGS84Location location, String platformId ) {

        StringBuilder query = new StringBuilder();
        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n");
        query.append("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n");

        //Location test //dziala ok
        query.append("SELECT ?location WHERE {\n" );
        query.append("\t?location a cim:Location ;\n");
        query.append("\t\ta cim:WGS84Location ;\n");
        query.append("\t\trdfs:label \""+ location.getLabel() + "\" ;\n");
        query.append("\t\trdfs:comment \"" + location.getComment() + "\" .\n");

        //Ensure that location is defined for this platform...
        query.append("\t?platform a owl:Ontology ;\n");
        query.append("\t\tcim:id \"" + platformId + "\" ;\n");
        query.append("\t\tmim:hasService ?service .\n");
        query.append("\t?service mim:hasResource ?sensor .\n");
        query.append("\t?sensor a cim:Resource ;\n");
        query.append("\t\tcim:locatedAt ?location .\n");

        //WGS84-specific
        query.append("\t?location geo:lat \""+ location.getLatitude() + "\" ;\n");
        query.append("\t\tgeo:long \""+ location.getLongitude() + "\" ;\n");
        query.append("\t\tgeo:alt \""+ location.getAltitude() + "\" .\n");
        query.append("}");

        return query.toString();
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
    public String sendRpcMessage(String exchangeName, String routingKey, String message, String classType) {
        try {
            log.info("Sending RPC message: " + message);

            String replyQueueName = this.channel.queueDeclare().getQueue();

            String correlationId = UUID.randomUUID().toString();

            Map<String, Object> headers = new HashMap<>();
            headers.put("__TypeId__", classType);
            headers.put("__ContentTypeId__", Object.class.getCanonicalName());

            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueueName)
                    .contentType("application/json")
                    .headers(headers)
                    .build();

            QueueingConsumer consumer = new QueueingConsumer(channel);
            this.channel.basicConsume(replyQueueName, true, consumer);

            String responseMsg = null;

            this.channel.basicPublish(exchangeName, routingKey, props, message.getBytes());
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(20000);
                if (delivery == null) {
                    log.info("Timeout in response retrieval");
                    return null;
                }

                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    log.info("Wrong correlationID in response message");
                    responseMsg = new String(delivery.getBody());
                    break;
                }
            }

            log.info("Response received: " + responseMsg);
            return responseMsg;
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
