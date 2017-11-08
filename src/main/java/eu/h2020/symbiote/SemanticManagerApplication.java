package eu.h2020.symbiote;

import eu.h2020.symbiote.messaging.RabbitManager;
import eu.h2020.symbiote.ontology.SemanticManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * Spring application for Semantic Manager.
 *
 * Created by tipech on 06.03.2017.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SemanticManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemanticManagerApplication.class, args);
    }

    private static Log log = LogFactory.getLog(SemanticManagerApplication.class);

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

    @Component
    public static class CLR implements CommandLineRunner {

        private final RabbitManager rabbitManager;

        @Autowired
        public CLR(RabbitManager rabbitManager) {
            this.rabbitManager = rabbitManager;
        }

        @Override
        public void run(String... args) throws Exception {
//
            //message retrieval - start rabbit exchange and consumers
//            this.rabbitManager.init();
            this.rabbitManager.startConsumers(SemanticManager.getManager());

            //Load all PIM models

            log.info("CLR run() and Rabbit Manager init()");
        }
    }
}