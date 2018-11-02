package translator.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import translator.exception.TranslatorException;

@Component
public class YandexTranslator extends TranslatorImpl {

  private ObjectMapper om = new ObjectMapper();


  @Value("${yandex.api_key}")
  private String apiKey;

  @Override
  protected HttpRequestBase getHttpRequest(String from, String to, String text, String encodedText) {
    String uri = UriComponentsBuilder.fromHttpUrl("https://translate.yandex.net/api/v1.5/tr.json/translate")
            .queryParam("key", apiKey)
            .queryParam("lang", from + "-" + to)
            .queryParam("text", encodedText).toUriString();
    return new HttpGet(uri);
  }

  @Override
  protected String getTranslationFrom(String responseAsStr) {
    try {
      return (String) om.readValue(responseAsStr, YandexResponse.class).text[0];
    } catch (Exception e) {
      throw new TranslatorException("Failed processing " + responseAsStr, e);
    }
  }

}

class YandexResponse {
  public String code;
  public String lang;
  public Object[] text;
}
