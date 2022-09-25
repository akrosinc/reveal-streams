package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.PerformanceUserType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceUserTypeRepository extends JpaRepository<PerformanceUserType, UUID> {

  PerformanceUserType findPerformanceUserTypeByPlanIdentifierAndUserString(UUID planIdentifier, String user);

}
