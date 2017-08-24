package eu.h2020.symbiote;

import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;



import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 18/05/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSymbioteModelsUtil {

    private static final String BIM_PROPERTY_CARBONMONOXIDE = "carbonMonoxideConcentration";
    private static final String QU_REC_TEMPERATURE = "temperature";
    private static final String NON_EXISTING = "thispropertydoesnotexist";


    @Test
    public void testBIMPropertyFind() {
        findExistingProperty(BIM_PROPERTY_CARBONMONOXIDE);
    }

    @Test
    public void testQURECPropertyFind() {
        findExistingProperty(QU_REC_TEMPERATURE);
    }

    @Test
    public void testNonexistingFind() {
        findNonExistingProperty(NON_EXISTING);
    }


    private void findExistingProperty( String propertyName) {
        try {
            String property = SymbioteModelsUtil.findInSymbioteCoreModels(propertyName);
            assertNotNull("Found property should not be null",property);
            assertFalse("Found property should not be empty",property.isEmpty());
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail("Existing BIM property should be found!");
        }
    }

    private void findNonExistingProperty( String propertyName) {
        try {
            String property = SymbioteModelsUtil.findInSymbioteCoreModels(propertyName);
            fail("For nonexisting property error should be thrown");
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
        }
    }

}
