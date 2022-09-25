package com.revealprecision.revealstreams.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserLevel extends Message {

  private String userId;
  private String name;
  private Integer level;
  private String type;
  private String label;
}
