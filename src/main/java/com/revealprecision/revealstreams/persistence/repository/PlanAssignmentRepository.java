package com.revealprecision.revealstreams.persistence.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.revealprecision.revealstreams.persistence.domain.PlanAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAssignmentRepository extends EntityGraphJpaRepository<PlanAssignment, UUID> {



  @Query(value = "select new com.revealprecision.revealstreams.persistence.domain.PlanAssignment(pl.identifier, pl.organization.identifier, pl.organization.name, pl.planLocations.identifier, pl.planLocations.location.identifier) from PlanAssignment pl where pl.planLocations.plan.identifier = :planIdentifier")
  List<PlanAssignment> findPlanAssignmentsByPlanLocations_Plan_Identifier(UUID planIdentifier);


}