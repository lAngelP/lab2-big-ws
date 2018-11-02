package translator.web.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.springframework.ws.client.core.WebServiceTemplate;

import translator.Application;
import translator.web.ws.schema.GetTranslationRequest;
import translator.web.ws.schema.GetTranslationResponse;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@ActiveProfiles("yandexTranslator")
public class TranslatorEndpointTest {

  private Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

  @LocalServerPort
  private int port;

  @Before
  public void init() throws Exception {
    marshaller.setPackagesToScan(ClassUtils.getPackageName(GetTranslationRequest.class));
    marshaller.afterPropertiesSet();
  }

  @Test
  public void testSendAndReceive() {
    GetTranslationRequest request = new GetTranslationRequest();
    request.setLangFrom("en");
    request.setLangTo("es");
    request.setText("This is a test of translation service");
    Object response = new WebServiceTemplate(marshaller).marshalSendAndReceive("http://localhost:"
            + port + "/ws", request);
    assertNotNull(response);
    assertThat(response, instanceOf(GetTranslationResponse.class));
    GetTranslationResponse translation = (GetTranslationResponse) response;
    assertThat(translation.getTranslation(), is("Esto es una prueba de servicio de traducción"));
  }
}
