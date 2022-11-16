package com.revealprecision.revealstreams.persistence.repository;

import com.revealprecision.revealstreams.persistence.domain.EventTracker;
import com.revealprecision.revealstreams.persistence.projection.CddSupervisorDailySummaryAggregationProjection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTrackerRepository extends JpaRepository<EventTracker, UUID>{

  Optional<EventTracker> findEventTrackerByAggregationKey(String aggregationKey);


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
      + " and et.observations->'ntd_treated'->>0= :ntdTreated "
      + " group by   "
      + " lp.identifier,lp.name",nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection  getAggregationDataFromCddSupervisorDailySummary(UUID locationParentIdentifier, String ntdTreated);


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
      + " and et.observations->'ntd_treated'->>0= :ntdTreated "
      + "group by  et.location_identifier;",nativeQuery = true)
  CddSupervisorDailySummaryAggregationProjection getAggregationDataFromCddSupervisorDailSummaryOnPlanTarget(UUID locationIdentifier, String ntdTreated);

}
