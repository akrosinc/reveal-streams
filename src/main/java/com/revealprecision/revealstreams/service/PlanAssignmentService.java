package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.persistence.domain.PlanAssignment;
import com.revealprecision.revealstreams.persistence.repository.PlanAssignmentRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanAssignmentService {

  private final PlanAssignmentRepository planAssignmentRepository;

  public List<PlanAssignment> getPlanAssignmentsByPlanIdentifier(UUID planIdentifier) {
    return planAssignmentRepository.findPlanAssignmentsByPlanLocations_Plan_Identifier(
        planIdentifier);
  }

}