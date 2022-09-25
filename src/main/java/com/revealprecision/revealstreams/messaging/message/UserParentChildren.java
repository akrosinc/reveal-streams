package com.revealprecision.revealstreams.messaging.message;

import java.util.Set;
import java.util.UUID;
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
public class UserParentChildren extends Message {

  private UUID planIdentifier;
  private UserLevel parent;
  private Set<UserLevel> children;
}
