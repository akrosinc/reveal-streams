package com.revealprecision.revealstreams.messaging.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserData extends Message {

  private String submissionId;
  private UUID planIdentifier;
  private DeviceUser deviceUser;
  private String deviceUserLabel;
  private String fieldWorker;
  private String fieldWorkerLabel;
  private String district;
  private String districtLabel;
  private LocalDateTime captureTime;
  private List<List<OrgLevel>> orgHierarchy;
  private String orgLabel;
  private Map<String, Object> fields;

}
