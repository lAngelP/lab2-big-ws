package translator.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.translate.Translate;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("googleTranslator")
public class GoogleTranslator extends TranslatorImpl {

  private ObjectMapper om = new ObjectMapper();

  private JsonParser parser = new JsonParser();

  private Translate translate;


  @Override
  protected HttpRequestBase getHttpRequest(String from, String to, String text, String encodedText) {
    //https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=This%20is%20a%20test%20of%20translation%20service
      String uri = UriComponentsBuilder.fromHttpUrl("https://translate.googleapis.com/translate_a/single")
              .queryParam("client","gtx")
              .queryParam("dt", "t")
              .queryParam("sl", from)
              .queryParam("tl", to)
              .queryParam("q", encodedText).toUriString();
      return new HttpGet(uri);
  }

  @Override
  protected String getTranslationFrom(String responseAsStr) {
    JsonElement result = parser.parse(responseAsStr);
    if(result.isJsonArray()){
      JsonArray resultAsArray = result.getAsJsonArray();
      if(resultAsArray.size() > 0 && resultAsArray.get(0).isJsonArray()){
        JsonArray sentences = resultAsArray.get(0).getAsJsonArray();
        List<String> translated = new ArrayList<>();
        for (JsonElement sentence: sentences) {
          if(sentence.isJsonArray()){
            translated.add(sentence.getAsJsonArray().get(0).getAsString());
          }
        }

        return Joiner.on(". ").join(translated);
      }
    }
    return responseAsStr;
  }

  protected String translateInternal(String from, String to, String text, String encodedText) throws IOException {
    HttpRequestBase requestBase = getHttpRequest(from, to, text, encodedText);
    HttpClient httpclient = HttpClientBuilder.create().build();
    HttpResponse response = httpclient.execute(requestBase);
    HttpEntity responseEntity = response.getEntity();
    String responseAsStr = transformToString(responseEntity);
    if (StringUtils.hasText(responseAsStr)) {
      return getTranslationFrom(responseAsStr);
    }
    return "";
  }

}