package eu.h2020.symbiote;

import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 03/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationTests {

    private String TEMPERATURE_NAME = "temperature";
    private String NONEXISTENT_NAME = "temperature1234566789";

    @Test
    public void testFindInSymbioteModels() {
        try {
            String temperatureUri = SymbioteModelsUtil.findInSymbioteModels(TEMPERATURE_NAME);
            assertNotNull(temperatureUri);
        } catch( PropertyNotFoundException e ) {
            e.printStackTrace();
            fail();
        }

        try {
            SymbioteModelsUtil.findInSymbioteModels(NONEXISTENT_NAME);
            fail();
        } catch (PropertyNotFoundException e) {
        }
    }

}
