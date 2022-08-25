package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.LocationCounts;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationCountsRepository extends JpaRepository<LocationCounts, UUID> {

 LocationCounts findLocationCountsByParentLocationIdentifierAndLocationHierarchyIdentifierAndGeographicLevelName(UUID parentLocationIdentifier, UUID locationHierarchyIdentifier, String geographicLevelName);

}
