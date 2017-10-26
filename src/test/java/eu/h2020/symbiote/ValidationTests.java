package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.InformationModelValidationResult;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationRequest;
import eu.h2020.symbiote.core.internal.ResourceInstanceValidationResult;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.ontology.SemanticManager;
import eu.h2020.symbiote.ontology.errors.PropertyNotFoundException;
import eu.h2020.symbiote.ontology.utils.SymbioteModelsUtil;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static eu.h2020.symbiote.TestSetupConfig.*;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.BIM;

/**
 * Created by Szymon Mueller on 03/05/2017.
 */
//@RunWith(MockitoJUnitRunner.class)
public class ValidationTests {

    private String TEMPERATURE_NAME = "temperature";
    private String NONEXISTENT_NAME = "temperature1234566789";
//    private String BIM_RESOURCE_FILE = "/bim_resource.ttl";
    private String BIM_RESOURCE_FILE = "/bim_from_rest.ttl";

    @Before
    public void init() {
        loadBIM();
    }

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

    //TODO why apparently BIM does not have owl:Ontology?
    @Test
    public void testLoadBIMasPIM() {
        InformationModel rdfInfo = new InformationModel();
        try {
            rdfInfo.setRdfFormat(RDFFormat.Turtle);
            rdfInfo.setRdf(ModelHelper.writeAll(ModelHelper.readModel(BIM.getURI(), false, false), RDFFormat.Turtle));
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

    @Test
    public void bimResourceValidationTest() {
        try {

            ResourceInstanceValidationRequest request = new ResourceInstanceValidationRequest();
            request.setInformationModelId("BIM");
            request.setRdfFormat(RDFFormat.Turtle);
            String resourceRdf = IOUtils.toString(this.getClass()
                    .getResource(BIM_RESOURCE_FILE));
            request.setRdf(resourceRdf);
            ResourceInstanceValidationResult result = SemanticManager.getManager().validateResourcesInstance(request);
            assertNotNull(result);
            assertNotNull(result.getObjectDescription());
            assertTrue(result.isSuccess());
            assertEquals("Should find 1 resource", 1, result.getObjectDescription().size());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error occurred when loading model from file");
        }
    }
}
