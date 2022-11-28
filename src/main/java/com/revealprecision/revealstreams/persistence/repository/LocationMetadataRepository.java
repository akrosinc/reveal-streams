package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.metadata.LocationMetadata;
import com.revealprecision.revealstreams.persistence.projection.LocationMetadataDoubleAggregateProjection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationMetadataRepository extends JpaRepository<LocationMetadata, UUID> {

  Optional<LocationMetadata> findLocationMetadataByLocation_Identifier(UUID locationIdentifier);

  @Query(value =
      "SELECT cast(lmda.locationParentIdentifier as varchar) as locationParentIdentifier, lmda.parentName, lmda.tag, sum(lmda.value) as value From location_metadata_double_aggregate lmda\n"
          + "\n"
          + "WHERE CAST(lmda.locationParentIdentifier as varchar) =:locationIdentifier and lmda.tag=:tag \n"
          + "group by lmda.locationParentIdentifier, lmda.parentName, lmda.tag", nativeQuery = true)
  LocationMetadataDoubleAggregateProjection getSumOfDoubleTagByLocationIdentifierAndTag(
      @Param("locationIdentifier") String locationIdentifier, @Param("tag") String tag);

  @Query(value =
      "SELECT DISTINCT cast(lmda.locationIdentifier as varchar) as locationIdentifier, lmda.locationName, lmda.tag, lmda.value From location_metadata_double_aggregate lmda\n"
          + "\n"
          + "WHERE CAST(lmda.locationIdentifier as varchar) =:locationIdentifier and lmda.tag=:tag \n", nativeQuery = true)
  LocationMetadataDoubleAggregateProjection getValueOfDoubleTagByLocationIdentifierAndTagOnTargetLevel(
      @Param("locationIdentifier") String locationIdentifier, @Param("tag") String tag);
}
