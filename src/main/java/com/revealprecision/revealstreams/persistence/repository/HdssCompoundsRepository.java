package com.revealprecision.revealstreams.persistence.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.revealprecision.revealstreams.persistence.domain.HdssCompounds;
import com.revealprecision.revealstreams.persistence.projection.HdssEventDataProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualTaskBusinessStateByLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualsByLocationProjection;
import com.revealprecision.revealstreams.persistence.projection.IndividualsPerCompoundByLocationProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;

public interface HdssCompoundsRepository extends EntityGraphJpaRepository<HdssCompounds, UUID> {

  @Query(value = "SELECT cast(lp.identifier as varchar) as locationIdentifier, lp.name as locationName,count(*) as individualCount\n"
      + "from hdss.hdss_compounds hc\n"
      + "left join (SELECT lr.location_identifier as child_location, arr.ancestor\n"
      + "        from location_relationship lr,\n"
      + "             unnest(lr.ancestry) with ordinality arr(ancestor, pos)\n"
      + "        ) as lr on lr.child_location = hc.structure_id\n"
      + "left join location lp on lr.ancestor = lp.identifier\n"
      + "left join geographic_level gl on gl.identifier = lp.geographic_level_identifier\n"
      + "where lp.identifier = :locationIdentifier\n"
      + "group by lp.identifier,lp.name;",nativeQuery = true)
  IndividualsByLocationProjection getNumberOfIndividualsByLocation(UUID locationIdentifier);


  @Query(value = "SELECT  hc.compound_id as compound ,count(*) as individualCount\n"
      + "from hdss.hdss_compounds hc\n"
      + "         left join (SELECT lr.location_identifier as child_location, arr.ancestor\n"
      + "                    from location_relationship lr,\n"
      + "                         unnest(lr.ancestry) with ordinality arr(ancestor, pos)\n"
      + ") as lr on lr.child_location = hc.structure_id\n"
      + "         left join location lp on lr.ancestor = lp.identifier\n"
      + "         left join geographic_level gl on gl.identifier = lp.geographic_level_identifier\n"
      + "where lp.identifier = :locationIdentifier \n"
      + "group by hc.compound_id",nativeQuery = true)

  List<IndividualsPerCompoundByLocationProjection> getNumberOfIndividualsPerCompoundByLocation(UUID locationIdentifier);


  @Query(value = "SELECT a.title as title, t.business_status as businessStatus, count(*) as businessStatusCount\n"
      + "from task t\n"
      + "    left join action a on a.identifier = t.action_identifier\n"
      + "         inner join person p on p.identifier = t.base_entity_identifier\n"
      + "         inner join person_location pl on pl.person_identifier = p.identifier\n"
      + "    inner join (SELECT lr.location_identifier as child_location, arr.ancestor\n"
      + "               from location_relationship lr,\n"
      + "                    unnest(lr.ancestry) with ordinality arr(ancestor, pos)\n"
      + "                ) as lr on lr.child_location = pl.location_identifier\n"
      + "    left join location lp on lr.ancestor = lp.identifier\n"
      + "    left join geographic_level gl on gl.identifier = lp.geographic_level_identifier\n"
      + "where\n"
      + "      t.plan_identifier = :planIdentifier\n"
      + "  and lp.identifier = :locationIdentifier\n"
      + "group by a.title, t.business_status", nativeQuery = true)
  List<IndividualTaskBusinessStateByLocationProjection> getTaskBusinessStateCountsByLocation(UUID planIdentifier,UUID locationIdentifier);


  @Query(value = "SELECT count(*)\n"
      + "From (\n"
      + "         SELECT DISTINCT \n"
      + "            e.observations -> 'rdt' ->> 0        as rdt,\n"
      + "            e.observations -> 'individual' ->> 0 AS individual,\n"
      + "            hc.*,\n"
      + "            l.name\n"
      + "         FROM event_tracker e\n"
      + "                  left join hdss.hdss_compounds hc\n"
      + "                            on hc.individual_id = e.observations -> 'individual' ->> 0\n"
      + "                  left join (\n"
      + "             SELECT lr.location_identifier, parent.parent_id\n"
      + "             from location_relationship lr,\n"
      + "                  unnest(lr.ancestry) with ordinality parent(parent_id, pos)\n"
      + "         ) p on p.location_identifier = hc.structure_id\n"
      + "                  left join location l on l.identifier = parent_id\n"
      + "         WHERE e.observations -> 'individual' -> 0 IS NOT NULL\n"
      + "           and e.observations -> 'rdt' -> 0 is not null\n"
      + "           and l.identifier = :locationIdentifier \n"
      + "     ) c",nativeQuery = true)
  int getNumberOfTestedIndividualsByLocation(UUID locationIdentifier);

  @Query(value = "SELECT cast(c.locationIdentifier as varchar) as locationIdentifier,c.compound_id as compound,\n"
      + "       sum(c.pas_tested)   as passiveTested,\n"
      + "       sum(c.rcd_tested) as rcdTested,\n"
      + "       sum(c.pas_positive) as passivePositive,\n"
      + "       sum(c.rcd_positive) as rcdPositive,\n"
      + "       sum(c.pas_tested + c.rcd_tested) as totalTested,\n"
      + "       sum(c.rcd_positive + c.pas_positive) as totalCases\n"
      + " From ( "
      + "         SELECT DISTINCT CASE\n"
      + "                             WHEN e.observations -> 'rdt' ->> 0 = 'Positive' AND e.event_type = 'passive_case_detection' THEN 1\n"
      + "                             ELSE 0 END                                                      as pas_positive,\n"
      + "                         CASE\n"
      + "                             WHEN e.observations -> 'rdt' ->> 0 = 'Positive' AND e.event_type = 'rcd' THEN 1\n"
      + "                             ELSE 0 END                                                      as rcd_positive,\n"
      + "                         CASE\n"
      + "                             WHEN e.observations -> 'rdt' ->> 0 = 'Positive' AND e.event_type = 'passive_case_detection'\n"
      + "                                 OR e.observations -> 'rdt' ->> 0 = 'Negative' AND e.event_type = 'passive_case_detection' THEN 1\n"
      + "                             ELSE 0 END                                                      as pas_tested,\n"
      + "                         CASE\n"
      + "                             WHEN e.observations -> 'rdt' ->> 0 = 'Positive' AND e.event_type = 'rcd'\n"
      + "                                 OR e.observations -> 'rdt' ->> 0 = 'Negative' AND e.event_type = 'rcd' THEN 1\n"
      + "                             ELSE 0 END                                                      as rcd_tested,\n"
      + "                         hc.compound_id,\n"
      + "                          hc.structure_id as locationIdentifier,\n"
      + "                         hc.individual_id\n"
      + "         FROM event_tracker e\n"
      + "                  left join hdss.hdss_compounds hc\n"
      + "                            on hc.individual_id = e.observations -> 'individual' ->> 0\n"
      + "                  left join (\n"
      + "             SELECT lr.location_identifier, parent.parent_id\n"
      + "             from location_relationship lr,\n"
      + "                  unnest(lr.ancestry) with ordinality parent(parent_id, pos)\n"
      + "         ) p on p.location_identifier = hc.structure_id\n"
      + "                  left join location l on l.identifier = parent_id\n"
      + "         WHERE\n"
      + "               l.identifier = :locationIdentifier \n"
      + "           and e.plan_identifier = :planIdentifier\n"
      + "     ) c\n"
      + "group by c.compound_id,cast(c.locationIdentifier as varchar) ;",nativeQuery = true)
  List<HdssEventDataProjection> getEventDataForLocationAndPlan(UUID locationIdentifier, UUID planIdentifier);

}
