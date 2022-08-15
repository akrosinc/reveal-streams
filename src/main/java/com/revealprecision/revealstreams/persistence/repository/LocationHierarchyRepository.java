package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.LocationHierarchy;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationHierarchyRepository extends JpaRepository<LocationHierarchy, UUID> {

}
