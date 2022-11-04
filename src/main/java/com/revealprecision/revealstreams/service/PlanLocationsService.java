package com.revealprecision.revealstreams.service;

import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.repository.PlanLocationsRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class PlanLocationsService {

  private final PlanLocationsRepository planLocationsRepository;


  @Autowired
  public PlanLocationsService(PlanLocationsRepository planLocationsRepository) {
    this.planLocationsRepository = planLocationsRepository;
  }


  public Long getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
      UUID planIdentifier,
      String geoLevelName, UUID locationIdentifier, UUID locationHierarchyIdentifier
  ) {
    return planLocationsRepository.getNumberOfAssignedChildrenByGeoLevelNameWithinLocationAndHierarchyAndPlan(
        geoLevelName,
        locationIdentifier.toString(),
        locationHierarchyIdentifier,
        planIdentifier);
  }

  public Long getAssignedStructureCountByLocationParentAndPlan(Plan plan, Location location) {
    if (plan.getLocationHierarchy().getNodeOrder().get(plan.getLocationHierarchy().getNodeOrder()
            .indexOf(plan.getPlanTargetType().getGeographicLevel().getName())-1)
        .equals(location.getGeographicLevel().getName())) {
      return planLocationsRepository.getAssignedStructureCountOnPlanTargetByLocationParentAndPlan(
          plan.getIdentifier(), location.getIdentifier());
    } else {
      return planLocationsRepository.getAssignedStructureCountByLocationParentAndPlan(
          plan.getIdentifier(), location.getIdentifier());
    }
  }

  public Long getAssignedStructureCountByLocationParentAndPlanForWaterBodies(Plan plan, Location location) {

    if (plan.getLocationHierarchy().getNodeOrder().get(plan.getLocationHierarchy().getNodeOrder()
            .indexOf(plan.getPlanTargetType().getGeographicLevel().getName())-1)
        .equals(location.getGeographicLevel().getName())) {
      return planLocationsRepository.getAssignedStructureCountOnPlanTargetByLocationParentAndPlanForWaterBodies(
          plan.getIdentifier(), location.getIdentifier());
    } else {
      return planLocationsRepository.getAssignedStructureCountByLocationParentAndPlanForWaterBodies(
          plan.getIdentifier(), location.getIdentifier());
    }

  }
  public Long getAssignedStructureCountByLocationParentAndPlanForNonWaterBodies(Plan plan, Location location) {

    if (plan.getLocationHierarchy().getNodeOrder().get(plan.getLocationHierarchy().getNodeOrder()
            .indexOf(plan.getPlanTargetType().getGeographicLevel().getName())-1)
        .equals(location.getGeographicLevel().getName())) {
      return planLocationsRepository.getAssignedStructureCountOnPlanTargetByLocationParentAndPlanForNonWaterBodies(
          plan.getIdentifier(), location.getIdentifier());
    } else {
      return planLocationsRepository.getAssignedStructureCountByLocationParentAndPlanForNonWaterBodies(
          plan.getIdentifier(), location.getIdentifier());
    }
  }
}