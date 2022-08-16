package com.revealprecision.revealstreams.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class PersonName {

  @NotNull
  private NameUseEnum use;
  @NotBlank
  private String text;
  @NotBlank
  private String family;
  private String given;
  private String prefix;
  private String suffix;
}
