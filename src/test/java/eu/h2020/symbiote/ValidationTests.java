package eu.h2020.symbiote;

import eu.h2020.symbiote.core.cci.InformationModelRequest;
import eu.h2020.symbiote.core.internal.InformationModelValidationResult;
import eu.h2020.symbiote.core.internal.PIMMetaModelValidationResult;
import eu.h2020.symbiote.core.model.InformationModel;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.RDFInfo;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
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
            String temperatureUri = SymbioteModelsUtil.findInSymbioteCoreModels(TEMPERATURE_NAME);
            assertNotNull(temperatureUri);
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        try {
            SymbioteModelsUtil.findInSymbioteCoreModels(NONEXISTENT_NAME);
            fail();
        } catch (PropertyNotFoundException e) {
        }
    }
    public static final String PIM_1_0_1_FILE = "/rdf/bim-v1.0.1.owl";

    @Test
    public void testLoadBIMasPIM() {
        InformationModel rdfInfo = new InformationModel();
        try {
            rdfInfo.setRdfFormat(RDFFormat.Turtle);            
            rdfInfo.setRdf(IOUtils.toString(this.getClass()
                    .getResource(PIM_1_0_1_FILE)));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        InformationModelValidationResult validationResult = SemanticManager.getManager().validatePIMMetaModel(rdfInfo);
        if (!validationResult.isSuccess()) {
            System.out.println(validationResult.getMessage());
            fail();
        }
    }

}
