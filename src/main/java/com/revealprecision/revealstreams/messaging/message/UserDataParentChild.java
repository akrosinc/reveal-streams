package com.revealprecision.revealstreams.messaging.message;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserDataParentChild extends Message {

  private UUID planIdentifier;
  private UserLevel parent;
  private UserLevel child;
}
