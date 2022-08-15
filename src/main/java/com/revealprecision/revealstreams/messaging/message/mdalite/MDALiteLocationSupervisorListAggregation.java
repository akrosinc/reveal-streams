package com.revealprecision.revealstreams.messaging.message.mdalite;


import com.revealprecision.revealstreams.messaging.Message;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MDALiteLocationSupervisorListAggregation extends Message {

  private Map<String, String> supervisorNames = new HashMap<>();
}
