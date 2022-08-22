package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.repository.PlanRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import com.revealprecision.revealstreams.persistence.domain.Plan.Fields;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlanService {

  private final PlanRepository planRepository;

  public Plan findPlanByIdentifier(UUID planIdentifier) {
    return planRepository.findById(planIdentifier).orElseThrow(
        () -> new NotFoundException(Pair.of(Fields.identifier, planIdentifier), Plan.class));
  }

  public Plan findNullablePlanByIdentifier(UUID planIdentifier) {
   return planRepository.findPlanByIdentifier(planIdentifier);
  }

}