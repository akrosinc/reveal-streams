package com.revealprecision.revealstreams.persistence.repository;


import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.projection.LocationFlat;
import com.revealprecision.revealstreams.persistence.projection.LocationMultipartCountProjection;
import com.revealprecision.revealstreams.persistence.projection.LocationNameProjection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {

  List<Location> getLocationsByPeople_Identifier(UUID personIdentifier);

  List<Location> findByIdentifierIn(Set<UUID> ids);

  @Query("        SELECT\n"
      + "            lr.location.identifier AS locationIdentifier,\n"
      + "            lr.parentLocation.identifier AS parentIdentifier,\n"
      + "            lr.location.name AS name,"
      + "            lr.location.geographicLevel.identifier as geoLevelIdentifier,"
      + "            lr.location.geographicLevel.name as geoLevelName\n"
      + "        FROM LocationRelationship lr\n"
      + "           WHERE  lr.location.geographicLevel.name <> 'structure' ")
  @QueryHints(value = {
      @QueryHint(name = "org.hibernate.fetchSize", value = "1000")
  })
  Stream<LocationFlat> streamAllFlat();

  @Query(value = "SELECT cast(l.identifier as varchar) as identifier, l.name as locationName from location l where l.identifier in :ids", nativeQuery = true)
  List<LocationNameProjection> findLocationNamesByIdentifierIn(Set<UUID> ids);


  @Query(value = "SELECT CAST(t.parentIdentifier as VARCHAR) as parentIdentifier, t.parentName as parentName, sum(locationCount) as locationCount from ("
      + "                  SELECT l.identifier as locationIdentifier, "
      + "                         l.name as locationName,\n"
      + "                         st_numgeometries(ST_GeomFromGeoJSON(cast(l.geometry as text))) as locationCount,\n"
      + "                         ancestors.item_object as parentIdentifier,\n"
      + "                         pl.name as parentName\n"
      + "                  from location l\n"
      + "                           left join geographic_level gl\n"
      + "                                     on gl.identifier = l.geographic_level_identifier\n"
      + "                           left join\n"
      + "                       (SELECT arr.item_object, lr.location_identifier\n"
      + "                        from location_relationship lr,\n"
      + "                             LATERAL unnest(array_append(lr.ancestry, lr.location_identifier)) WITH ORDINALITY arr(item_object, position)\n"
      + "                        WHERE lr.location_hierarchy_identifier =\n"
      + "                              :hierarchyIdentifier \n"
      + "                       ) ancestors on ancestors.location_identifier = l.identifier\n"
      + "                           left join location pl on ancestors.item_object = pl.identifier\n"
      + "                  WHERE gl.name = :geographicLevelName\n"
      + "              ) as t\n"
      + "WHERE t.parentIdentifier = :locationParentIdentifier\n"
      + "group by t.parentIdentifier, t.parentName LIMIT 1", nativeQuery = true)
  LocationMultipartCountProjection getMultipartLocationCountByLocationParent(UUID locationParentIdentifier, UUID hierarchyIdentifier, String geographicLevelName);


  List<Location> getLocationsByGeographicLevel_Name(String location);

  Optional<List<Location>> findAllByIdentifierIn(List<UUID> uuids);
}
