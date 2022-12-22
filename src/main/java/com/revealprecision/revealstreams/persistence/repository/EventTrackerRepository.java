package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.EventTracker;
import com.revealprecision.revealstreams.persistence.projection.CddDrugReceivedAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.CddDrugWithdrawalAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.CddSupervisorDailySummaryAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyAdverseEventsAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyCddSummaryAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.OnchocerciasisSurveyDrugAccountabilityAggregationProjection;
import com.revealprecision.revealstreams.persistence.projection.TabletAccountabilityAggregationProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTrackerRepository extends JpaRepository<EventTracker, UUID> {

  Optional<EventTracker> findEventTrackerByAggregationKey(String aggregationKey);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier "
      + ",lp.name as locationName "
      + ",sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0)) as totalTreatedMaleFiveFourteen   \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0))  as totalTreatedMaleAboveFifteen   \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0))   as totalTreatedFemaleFiveFourteen  \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreatedFemaleAboveFifteen \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreated \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_absent'->>0) as int),0)) as totalUntreatedAbsent   \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_refusal'->>0) as int),0))  as totalUntreatedRefusal   \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_pregnant'->>0) as int),0))   as totalUntreatedPregnant  \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_childrenu5'->>0) as int),0))   as totalUntreatedUnderFive  \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_sick'->>0) as int),0)) as totalUntreatedSick \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_absent'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'untreated_refusal'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'untreated_pregnant'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'untreated_childrenu5'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'untreated_sick'->>0) as int),0)) as totalUntreated \n"
      + "  From event_tracker et "
      + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
      + "left join location l on lr.location_identifier = l.identifier "
      + "left join location lp on lr.location_parent_identifier = lp.identifier "
      + "WHERE et.event_type = 'mda_onchocerciasis_survey' and lp.identifier = :locationParentIdentifier "
      + "  and et.plan_identifier = :planIdentifier "
      + " group by   "
      + " lp.identifier,lp.name", nativeQuery = true)
  OnchocerciasisSurveyCddSummaryAggregationProjection getOnchoSurveyFromCddSummary(
      UUID locationParentIdentifier, UUID planIdentifier);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier "
      + ",lp.name as locationName "
      + ",CAST(et.observations->'nonhhtreatment_type'->>0 as varchar ) as treatmentLocationType"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0)) as totalTreatedMaleFiveFourteen   \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0))  as totalTreatedMaleAboveFifteen   \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0))   as totalTreatedFemaleFiveFourteen  \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreatedFemaleAboveFifteen \n"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreated \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_absent'->>0) as int),0)) as totalUntreatedAbsent   \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_refusal'->>0) as int),0))  as totalUntreatedRefusal   \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_pregnant'->>0) as int),0))   as totalUntreatedPregnant  \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_sick'->>0) as int),0)) as totalUntreatedSick \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_childrenu5'->>0) as int),0))   as totalUntreatedUnderFive  \n"
      + ",sum(COALESCE(CAST((et.observations->'untreated_absent'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'untreated_refusal'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'untreated_pregnant'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'untreated_childrenu5'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'untreated_sick'->>0) as int),0)) as totalUntreated \n"
      + "  From event_tracker et "
      + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
      + "left join location l on lr.location_identifier = l.identifier "
      + "left join location lp on lr.location_parent_identifier = lp.identifier "
      + "WHERE et.event_type = 'treatment_outside_household' and lp.identifier = :locationParentIdentifier "
      + "  and et.plan_identifier = :planIdentifier"
      + " group by   "
      + " lp.identifier,lp.name,et.observations->'nonhhtreatment_type'->>0", nativeQuery = true)
  List<OnchocerciasisSurveyCddSummaryAggregationProjection> getOnchoSurveyFromTreatmentOutsideHousehold(
      UUID locationParentIdentifier, UUID planIdentifier);

  @Query(value = "SELECT hoh_aggregate.head_of_household              as householdHead\n"
      + "     , hoh.hohphone                                 as householdHeadPhoneNumber\n"
      + "     , CAST(hoh.location_identifier as varchar) as locationIdentifier"
      + "     , hoh_aggregate.location_parent_name           as locationName\n"
      + "     , hoh_aggregate.numberOfStructures             as numberOfStructures\n"
      + "     , hoh_aggregate.totalTreatedMaleFiveFourteen   as totalTreatedMaleFiveFourteen\n"
      + "     , hoh_aggregate.totalTreatedMaleAboveFifteen   as totalTreatedMaleAboveFifteen\n"
      + "     , hoh_aggregate.totalTreatedFemaleFiveFourteen as totalTreatedFemaleFiveFourteen\n"
      + "     , hoh_aggregate.totalTreatedFemaleAboveFifteen as totalTreatedFemaleAboveFifteen\n"
      + "     , hoh_aggregate.totalTreated                   as totalTreated\n"
      + "     , hoh_aggregate.totalUntreatedAbsent           as totalUntreatedAbsent\n"
      + "     , hoh_aggregate.totalUntreatedRefusal          as totalUntreatedRefusal\n"
      + "     , hoh_aggregate.totalUntreatedPregnant         as totalUntreatedPregnant\n"
      + "     , hoh_aggregate.totalUntreatedSick             as totalUntreatedSick\n"
      + "     , hoh_aggregate.totalUntreatedUnderFive        as totalUntreatedUnderFive\n"
      + "     , hoh_aggregate.totalUntreated                 as totalUntreated\n"
      + "from (\n"
      + "         SELECT hh.head_of_household\n"
      + "              , lp.identifier    as location_parent_identifier\n"
      + "              , lp.name          as location_parent_name\n"
      + "              , count(*)         as numberOfStructures\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'treated_male_5_to_14' ->> 0) as int),\n"
      + "                             0)) as totalTreatedMaleFiveFourteen\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'treated_male_above_15' ->> 0) as int),\n"
      + "                             0)) as totalTreatedMaleAboveFifteen\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'treated_female_5_to_14' ->> 0) as int),\n"
      + "                             0)) as totalTreatedFemaleFiveFourteen\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'treated_female_above_15' ->> 0) as int),\n"
      + "                             0)) as totalTreatedFemaleAboveFifteen\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'treated_male_5_to_14' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'treated_male_above_15' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'treated_female_5_to_14' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'treated_female_above_15' ->> 0) as int),\n"
      + "                             0)) as totalTreated\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_absent' ->> 0) as int),\n"
      + "                             0)) as totalUntreatedAbsent\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_refusal' ->> 0) as int),\n"
      + "                             0)) as totalUntreatedRefusal\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_pregnant' ->> 0) as int),\n"
      + "                             0)) as totalUntreatedPregnant\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_sick' ->> 0) as int),\n"
      + "                             0)) as totalUntreatedSick\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_childrenu5' ->> 0) as int),\n"
      + "                             0)) as totalUntreatedUnderFive\n"
      + "              , sum(COALESCE(CAST((hh.observations -> 'untreated_absent' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'untreated_refusal' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'untreated_pregnant' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'untreated_childrenu5' ->> 0) as int), 0) +\n"
      + "                    COALESCE(CAST((hh.observations -> 'untreated_sick' ->> 0) as int),\n"
      + "                             0)) as totalUntreated\n"
      + "         from (\n"
      + "                  SELECT case\n"
      + "                             when et.observations -> 'structure_hoh' ->> 0 = 'No'\n"
      + "                                 THEN et.observations -> 'hoh_selected' ->> 0\n"
      + "                             when et.observations -> 'structure_hoh' ->> 0 = 'Yes'\n"
      + "                                 THEN et.observations -> 'hoh_typed' ->> 0\n"
      + "                             end         as head_of_household,\n"
      + "                         et.observations as observations,\n"
      + "                         et.location_identifier\n"
      + "\n"
      + "                  from event_tracker et\n"
      + "                  WHERE et.event_type = 'mda_onchocerciasis_survey'\n"
      + "                    and et.plan_identifier = :planIdentifier\n"
      + "              ) as hh\n"
      + "                  left join location_relationships lr\n"
      + "                            on lr.location_identifier = hh.location_identifier\n"
      + "                  left join location l on lr.location_identifier = l.identifier\n"
      + "                  left join location lp on lr.location_parent_identifier = lp.identifier\n"
      + "         group by lp.identifier, lp.name, hh.head_of_household\n"
      + "     ) as hoh_aggregate\n"
      + "\n"
      + "         LEFT JOIN (\n"
      + "    SELECT et.observations -> 'hoh_typed' ->> 0 as hohname,\n"
      + "           et.observations -> 'hoh_phone' ->> 0 as hohphone,\n"
      + "           et.location_identifier               as location_identifier"
      + "    from event_tracker et\n"
      + "    WHERE et.event_type = 'mda_onchocerciasis_survey'\n"
      + "      and et.observations ->> 'hoh_typed' is not null\n"
      + "      and et.observations ->> 'hoh_phone' IS NOT NULL\n"
      + "      and et.plan_identifier = :planIdentifier\n"
      + ")\n"
      + "    as hoh on hoh.hohname = hoh_aggregate.head_of_household\n"
      + "WHERE hoh_aggregate.location_parent_identifier = :locationParentIdentifier",nativeQuery = true)
  List<OnchocerciasisSurveyCddSummaryAggregationProjection> getOnchoSurveyFromHouseholdHeadData(UUID locationParentIdentifier, UUID planIdentifier);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier "
      + ",lp.name as locationName "
      + ",sum(COALESCE(CAST((et.observations->'tablets_used'->>0) as int),0)) as tabletsUsed   \n"
      + ",sum(COALESCE(CAST((et.observations->'tablets_received'->>0) as int),0))  as tabletsReceived   \n"
      + ",sum(COALESCE(CAST((et.observations->'tablets_returned'->>0) as int),0))   as tabletsReturned  \n"
      + ",sum(COALESCE(CAST((et.observations->'tablets_lost'->>0) as int),0)) as tabletsLost \n"
      + "  From event_tracker et "
      + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
      + "left join location l on lr.location_identifier = l.identifier "
      + "left join location lp on lr.location_parent_identifier = lp.identifier "
      + "WHERE et.event_type = 'cdd_drug_received' and lp.identifier = :locationParentIdentifier "
      + "  and et.plan_identifier = :planIdentifier"
      + " group by   "
      + " lp.identifier,lp.name", nativeQuery = true)
  OnchocerciasisSurveyDrugAccountabilityAggregationProjection getOnchoSurveyFromDrugAccountability(
      UUID locationParentIdentifier, UUID planIdentifier);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier "
      + ",lp.name as locationName "
      + ",sum(COALESCE(CAST((et.observations->'readminstered'->>0) as int),0)) as readminstered   \n"
      + "  From event_tracker et "
      + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
      + "left join location l on lr.location_identifier = l.identifier "
      + "left join location lp on lr.location_parent_identifier = lp.identifier "
      + "WHERE et.event_type = 'adverse_events_record' and lp.identifier = :locationParentIdentifier "
      + "  and et.plan_identifier = :planIdentifier"
      + " group by   "
      + " lp.identifier,lp.name", nativeQuery = true)
  OnchocerciasisSurveyAdverseEventsAggregationProjection getOnchoSurveyFromAdverseEventsRecord(
      UUID locationParentIdentifier, UUID planIdentifier);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier "
      + ",lp.name as locationName "
      + ",sum(COALESCE(CAST((et.observations->'treated_male_1_to_4'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreated \n"
      + ",sum(COALESCE(CAST((et.observations->'street_male_1_to_4'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'street_male_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'street_male_above_15'->>0) as int),0) + "
      + "   COALESCE(CAST((et.observations->'street_female_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'street_female_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'street_female_above_15'->>0) as int),0)) as totalLivingOnTheStreet "
      + ",sum(COALESCE(CAST((et.observations->'snake_bite_male_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'snake_bite_male_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_male_above_15'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_female_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'snake_bite_female_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_female_above_15'->>0) as int),0)) as totalBittenBySnake    "
      + " ,sum(COALESCE(CAST((et.observations->'snake_bite_hospital_male_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'snake_bite_hospital_male_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_hospital_male_above_15'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_hospital_female_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'snake_bite_hospital_female_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'snake_bite_hospital_female_above_15'->>0) as int),0)) as totalVisitedHealthFacilityAfterSnakeBite "
      + " ,sum(COALESCE(CAST((et.observations->'pwd_male_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'pwd_male_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'pwd_male_above_15'->>0) as int),0) + "
      + "   COALESCE(CAST((et.observations->'pwd_female_1_to_4'->>0) as int),0) +   "
      + "   COALESCE(CAST((et.observations->'pwd_female_5_to_14'->>0) as int),0) +  "
      + "   COALESCE(CAST((et.observations->'pwd_female_above_15'->>0) as int),0)) as totalPeopleLivingWithDisability "
      + " ,sum(COALESCE(CAST((et.observations->'adminstered'->>0) as int),0)) as administered "
      + " ,sum(CASE  "
      + "WHEN (et.observations->'drugs'->>0 = 'MBZ') THEN  COALESCE(CAST((et.observations->'damaged_mbz'->>0) as int),0)  "
      + "WHEN (et.observations->'drugs'->>0 = 'PZQ') THEN  COALESCE(CAST((et.observations->'damaged_pzq'->>0) as int),0) "
      + "ELSE 0 "
      + " END) as damaged "
      + " ,sum(COALESCE(CAST((et.observations->'adverse'->>0) as int),0) ) as adverse "
      + "  From event_tracker et "
      + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
      + "left join location l on lr.location_identifier = l.identifier "
      + "left join location lp on lr.location_parent_identifier = lp.identifier "
      + "WHERE et.event_type = 'cdd_supervisor_daily_summary' and lp.identifier = :locationParentIdentifier "
      + " and et.observations->'ntd_treated'->>0= :ntdTreated  and et.plan_identifier = :planIdentifier "
      + " group by   "
      + " lp.identifier,lp.name", nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection getAggregationDataFromCddSupervisorDailySummary(
      UUID locationParentIdentifier, String ntdTreated, UUID planIdentifier);

  @Query(value = "SELECT CAST(lp.identifier as varchar) as locationIdentifier \n"
      + "      ,lp.name as locationName \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_1_to_4'->>0) as int),0)) as totalTreatedMaleOneToFour  \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0)) as totalTreatedMaleFiveToFourteen     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0)) as totalTreatedMaleAboveFifteen     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_1_to_4'->>0) as int),0)) as totalTreatedFemaleOneToFour     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0)) as totalTreatedFemaleFiveToFourteen    \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreatedFemaleAboveFifteen \n"
      + "        From event_tracker et \n"
      + "      left join location_relationships lr on lr.location_identifier = et.location_identifier \n"
      + "      left join location l on lr.location_identifier = l.identifier \n"
      + "      left join location lp on lr.location_parent_identifier = lp.identifier \n"
      + "      WHERE et.event_type = 'cdd_supervisor_daily_summary' "
      + "       and lp.identifier = :locationParentIdentifier  and   et.plan_identifier = :planIdentifier\n"
      + "       and et.observations->'ntd_treated'->>0= :ntdTreated \n"
      + "       group by   \n"
      + "       lp.identifier,lp.name", nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection getAgeBreakDownAggregationFromCddSupervisorDailySummary(
      UUID locationParentIdentifier, String ntdTreated, UUID planIdentifier);

  @Query(value = "SELECT CAST(et.location_identifier as varchar) as locationIdentifier \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_1_to_4'->>0) as int),0)) as totalTreatedMaleOneToFour  \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0)) as totalTreatedMaleFiveToFourteen     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0)) as totalTreatedMaleAboveFifteen     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_1_to_4'->>0) as int),0)) as totalTreatedFemaleOneToFour     \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0)) as totalTreatedFemaleFiveToFourteen    \n"
      + "      ,sum(COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreatedFemaleAboveFifteen \n"
      + "        From event_tracker et \n"
      + "      WHERE et.event_type = 'cdd_supervisor_daily_summary' "
      + "       and et.location_identifier = :locationIdentifier  and   et.plan_identifier = :planIdentifier\n"
      + "       and et.observations->'ntd_treated'->>0= :ntdTreated \n"
      + "       group by   \n"
      + "       et.location_identifier", nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection getAgeBreakDownAggregationFromCddSupervisorDailySummaryOnPlanTarget(
      UUID locationIdentifier, String ntdTreated, UUID planIdentifier);

  @Query(value = "SELECT CAST(et.location_identifier as varchar) as locationIdentifier\n"
      + ",sum(COALESCE(CAST((et.observations->'treated_male_1_to_4'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_5_to_14'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'treated_female_above_15'->>0) as int),0)) as totalTreated \n"
      + ",sum(COALESCE(CAST((et.observations->'street_male_1_to_4'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'street_male_5_to_14'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'street_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'street_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'street_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'street_female_above_15'->>0) as int),0)) as totalLivingOnTheStreet \n"
      + ",sum(COALESCE(CAST((et.observations->'snake_bite_male_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_male_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_male_above_15'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_female_above_15'->>0) as int),0)) as totalBittenBySnake     \n"
      + ",sum(COALESCE(CAST((et.observations->'snake_bite_hospital_male_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_hospital_male_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_hospital_male_above_15'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_hospital_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_hospital_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'snake_bite_hospital_female_above_15'->>0) as int),0)) as totalVisitedHealthFacilityAfterSnakeBite  \n"
      + ",sum(COALESCE(CAST((et.observations->'pwd_male_1_to_4'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'pwd_male_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'pwd_male_above_15'->>0) as int),0) +    \n"
      + "\t COALESCE(CAST((et.observations->'pwd_female_1_to_4'->>0) as int),0) +      \n"
      + "\t COALESCE(CAST((et.observations->'pwd_female_5_to_14'->>0) as int),0) +     \n"
      + "\t COALESCE(CAST((et.observations->'pwd_female_above_15'->>0) as int),0)) as totalPeopleLivingWithDisability  \n"
      + ",sum(COALESCE(CAST((et.observations->'adminstered'->>0) as int),0)) as administered \n"
      + ",sum(CASE  "
      + " WHEN (et.observations->'drugs'->>0 = 'MBZ') THEN  COALESCE(CAST((et.observations->'damaged_mbz'->>0) as int),0) \n"
      + " WHEN (et.observations->'drugs'->>0 = 'PZQ') THEN  COALESCE(CAST((et.observations->'damaged_pzq'->>0) as int),0) \n"
      + " ELSE 0  END) as damaged  \n"
      + ",sum(COALESCE(CAST((et.observations->'adverse'->>0) as int),0) ) as adverse   \n"
      + "From event_tracker et \n"
      + "\n"
      + "WHERE et.event_type = 'cdd_supervisor_daily_summary' and et.location_identifier = :locationIdentifier\n"
      + " and et.observations->'ntd_treated'->>0= :ntdTreated   and et.plan_identifier = :planIdentifier \n"
      + " group by  et.location_identifier;", nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection getAggregationDataFromCddSupervisorDailSummaryOnPlanTarget(
      UUID locationIdentifier, String ntdTreated, UUID planIdentifier);


  @Query(value =
      "SELECT  CAST(lp.identifier as varchar) as locationIdentifier ,lp.name"
          + ", sum(COALESCE(CAST((et.observations->'pzq_returned'->>0) as int),0)) as pzqReturned "
          + ", sum(COALESCE(CAST((et.observations->'mebendazole_returned'->>0) as int),0)) as mbzReturned "
          + ", sum(COALESCE(CAST(( "
          + "  CASE \n"
          + "   WHEN et.observations->'drug_distributed'->>0='Mebendazole (MEB)' THEN et.observations->'number_households_visited'->>0 "
          + "   ELSE '0'\n"
          + "  END\n"
          + ") as int),0)) as mzbNumberHouseholdsVisited \n"
          + ", sum(COALESCE(CAST((\n"
          + "  CASE \n"
          + "   WHEN et.observations->'drug_distributed'->>0='Praziquantel (PZQ)' THEN et.observations->'number_households_visited'->>0 "
          + "   ELSE '0'\n"
          + "  END\n"
          + ") as int),0)) as pzqNumberHouseholdsVisited "
          + " from event_tracker et "
          + "left join location_relationships lr on lr.location_identifier = et.location_identifier "
          + "left join location l on lr.location_identifier = l.identifier "
          + "left join location lp on lr.location_parent_identifier = lp.identifier "
          + "WHERE et.event_type = 'tablet_accountability'  and lp.identifier = :locationIdentifier "
          + " and et.plan_identifier = :planIdentifier "
          + " group by  lp.identifier,lp.name", nativeQuery = true)
  TabletAccountabilityAggregationProjection getAggregationDataFromTabletAccountability(
      UUID locationIdentifier, UUID planIdentifier);

  @Query(value =
      "SELECT CAST(et.location_identifier as varchar) as locationIdentifier  "
          + ", sum(COALESCE(CAST((et.observations->'pzq_returned'->>0) as int),0)) as pzqReturned "
          + ", sum(COALESCE(CAST((et.observations->'mebendazole_returned'->>0) as int),0)) as mbzReturned "
          + ", sum(COALESCE(CAST(( "
          + "  CASE \n"
          + "   WHEN et.observations->'drug_distributed'->>0='Mebendazole (MEB)' THEN et.observations->'number_households_visited'->>0 "
          + "   ELSE '0'\n"
          + "  END\n"
          + ") as int),0)) as mzbNumberHouseholdsVisited \n"
          + ", sum(COALESCE(CAST((\n"
          + "  CASE \n"
          + "   WHEN et.observations->'drug_distributed'->>0='Praziquantel (PZQ)' THEN et.observations->'number_households_visited'->>0 "
          + "   ELSE '0'\n"
          + "  END\n"
          + ") as int),0)) as pzqNumberHouseholdsVisited "
          + "from event_tracker et\n"
          + "WHERE et.event_type = 'tablet_accountability'  and et.location_identifier = :locationIdentifier \n"
          + " and et.plan_identifier = :planIdentifier "
          + " group by  et.location_identifier", nativeQuery = true)
  TabletAccountabilityAggregationProjection getAggregationDataFromTabletAccountabilityOnPlanTarget(
      UUID locationIdentifier, UUID planIdentifier);

  @Query(value =
      "SELECT  CAST(lp.identifier as varchar) as locationIdentifier ,lp.name"
          + ", sum(COALESCE(CAST((et.observations->'pzq_received'->>0) as int),0)) as pzqReceived "
          + ", sum(COALESCE(CAST((et.observations->'mbz_received'->>0) as int),0)) as mbzReceived "
          + " from event_tracker et\n"
          + "\n"
          + "left join location_relationships lr on lr.location_identifier = et.location_identifier \n"
          + "left join location l on lr.location_identifier = l.identifier \n"
          + "left join location lp on lr.location_parent_identifier = lp.identifier \n"
          + "\n"
          + "WHERE et.event_type = 'cdd_drug_received'  and lp.identifier = :locationIdentifier \n"
          + " and et.plan_identifier = :planIdentifier "
          + " group by  lp.identifier,lp.name", nativeQuery = true)
  CddDrugReceivedAggregationProjection getAggregationDataFromCddDrugReceived(
      UUID locationIdentifier, UUID planIdentifier);

  @Query(value =
      "SELECT CAST(et.location_identifier as varchar) as locationIdentifier "
          + ", sum(COALESCE(CAST((et.observations->'pzq_received'->>0) as int),0)) as pzqReceived "
          + ", sum(COALESCE(CAST((et.observations->'mbz_received'->>0) as int),0)) as mbzReceived "
          + "from event_tracker et\n"
          + "WHERE et.event_type = 'cdd_drug_received'  and et.location_identifier = :locationIdentifier \n"
          + " and et.plan_identifier = :planIdentifier "
          + " group by  et.location_identifier", nativeQuery = true)
  CddDrugReceivedAggregationProjection getAggregationDataFromCddDrugReceivedOnPlanTarget(
      UUID locationIdentifier, UUID planIdentifier);

  @Query(value =
      "SELECT  CAST(lp.identifier as varchar) as locationIdentifier ,lp.name"
          + ", sum(COALESCE(CAST((et.observations->'pzq_withdrawn'->>0) as int),0)) as pzqWithdrawn "
          + ", sum(COALESCE(CAST((et.observations->'mbz_withdrawn'->>0) as int),0)) as mbzWithdrawn "
          + " from event_tracker et\n"
          + "\n"
          + "left join location_relationships lr on lr.location_identifier = et.location_identifier \n"
          + "left join location l on lr.location_identifier = l.identifier \n"
          + "left join location lp on lr.location_parent_identifier = lp.identifier \n"
          + "\n"
          + "WHERE et.event_type = 'cdd_drug_withdrawal'  and lp.identifier = :locationIdentifier \n"
          + " and et.plan_identifier = :planIdentifier "
          + " group by  lp.identifier,lp.name", nativeQuery = true)
  CddDrugWithdrawalAggregationProjection getAggregationDataFromCddDrugWithdrawal(
      UUID locationIdentifier, UUID planIdentifier);

  @Query(value =
      "SELECT CAST(et.location_identifier as varchar) as locationIdentifier "
          + ", sum(COALESCE(CAST((et.observations->'pzq_withdrawn'->>0) as int),0)) as pzqWithdrawn "
          + ", sum(COALESCE(CAST((et.observations->'mbz_withdrawn'->>0) as int),0)) as mbzWithdrawn "
          + "from event_tracker et\n"
          + "WHERE et.event_type = 'cdd_drug_withdrawal'  and et.location_identifier = :locationIdentifier \n"
          + " and et.plan_identifier = :planIdentifier "
          + " group by  et.location_identifier", nativeQuery = true)
  CddDrugWithdrawalAggregationProjection getAggregationDataFromCddDrugWithdrawalOnPlanTarget(
      UUID locationIdentifier, UUID planIdentifier);
}
