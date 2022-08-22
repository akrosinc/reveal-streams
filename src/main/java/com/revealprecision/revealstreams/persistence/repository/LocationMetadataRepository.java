package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.metadata.LocationMetadata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationMetadataRepository extends JpaRepository<LocationMetadata, UUID> {

  Optional<LocationMetadata> findLocationMetadataByLocation_Identifier(UUID locationIdentifier);


}
