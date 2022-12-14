package com.revealprecision.revealstreams.persistence.domain.actioncondition;

import java.util.List;
import lombok.Data;

@Data
public class Condition {
  String entity;
  String type;
  String dataType;
  String operator;
  String property;
  List<String> value;
  String group;
  boolean isJoinCondition;
  String joinedEntity;
}

