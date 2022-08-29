package com.revealprecision.revealstreams.service;

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

  public Long getAssignedStructureCountByLocationParentAndPlan(UUID planIdentifier, UUID parentLocationIdentifier){
    return planLocationsRepository.getAssignedStructureCountByLocationParentAndPlan(planIdentifier, parentLocationIdentifier);
  }

}