package com.revealprecision.revealstreams.persistence.domain.actioncondition;


import java.util.List;
import lombok.Data;

@Data
public class Query{
  String entity;
  List<Condition> andConditions;
  List<Condition> orConditions;
  List<Condition> joinConditions;
}
