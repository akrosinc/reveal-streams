package com.revealprecision.revealstreams.api;

import com.revealprecision.revealstreams.props.TranslationProperties;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class TranslationDataController {

  private final TranslationProperties translationProperties;

  @GetMapping(value = "/translation-data")
  private Map<String,Map<String,String>> translationData(){

    return translationProperties.getTranslations();
  }

}
