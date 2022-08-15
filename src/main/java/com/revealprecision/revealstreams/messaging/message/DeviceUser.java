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
public class DeviceUser extends Message {

  private UUID deviceUserIdentifier;
  private String deviceUserName;
}
