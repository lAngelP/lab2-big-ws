package translator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import translator.domain.Language;
import translator.domain.LanguageSourceTarget;
import translator.domain.TranslatedText;
import translator.domain.Translator;
import translator.exception.TranslatorException;

import java.util.concurrent.Future;

@Service
public class TranslatorServiceImpl implements TranslatorService {

  private final Translator translator;

  @Autowired
  public TranslatorServiceImpl(Translator translator) {
    this.translator = translator;
  }

  @Override
  public TranslatedText translate(String langFrom, String langTo, String text) {
    LanguageSourceTarget languageSourceTarget = new LanguageSourceTarget(Language.fromString(langFrom), Language.fromString(langTo));
    if (languageSourceTarget.sourceAndTargeAreEquals()) {
      throw new TranslatorException("The languages from and to must be different.");
    }
    Future<String> translatorResult = translator.translate(languageSourceTarget, text);

    TranslatedText response = new TranslatedText();
    response.setFrom(languageSourceTarget.getSourceAsStr());
    response.setTo(languageSourceTarget.getTargetAsStr());
    response.setTranslation(getTranslation(translatorResult));
    return response;
  }

  private String getTranslation(Future<String> futureResult) {
    try {
      return futureResult.get();
    } catch (Exception e) {
      log().error("Problems getting the translation", e);
      return "Error:" + e.getMessage();
    }
  }
}
