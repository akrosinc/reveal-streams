package com.revealprecision.revealstreams.props.amdr;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "amdr.report")
@Component
@Setter
@Getter
public class AmdrReportColumnProperties {

  private Map<String, String> drugColumns = Map.of(
      "kelch13", "Artemisinin (Kelch)",
      "pfmdr1", "Lumef (MDR1)",
      "crt_mdr1", "Amodi (MDR1+CRT)",
      "pfcrt", "Chlor(CRT)",
      "pfdhfr", "Sulfa (DHFR)",
      "pfdhps", "Pyrme(DHPS)",
      "dhfr", "SP",
      "dhfr_dhps", "SP-IPTp"
  );
  private Map<String, String> haplotypeColumns = Map.of(
      "kelch13", "Kelch",
      "pfmdr1", "MDR",
      "pfcrt", "CRT",
      "pfdhfr", "DHFR",
      "pfdhps", "DHPS"
  );
}
