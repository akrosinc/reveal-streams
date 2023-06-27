package com.revealprecision.revealstreams.props;


import static com.revealprecision.revealstreams.constants.DashboardColumns.ABSENT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.ADMINISTERED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.BUSINESS_STATUS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.CHILD_UNDER_5;
import static com.revealprecision.revealstreams.constants.DashboardColumns.COVERAGE_OF_STRUCTURES_COMPLETED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.COVERAGE_OF_STRUCTURES_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FEMALES_15;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FEMALES_5_14;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FIELD_VERIFIED_POP_TARGET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FIELD_VERIFIED_POP_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.FOUND_COVERAGE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.HEAD_OF_HOUSE_HOLD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.HOUSEHOLD_DISTRIBUTION;
import static com.revealprecision.revealstreams.constants.DashboardColumns.LOST_DAMAGED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.MALES_15;
import static com.revealprecision.revealstreams.constants.DashboardColumns.MALES_5_14;
import static com.revealprecision.revealstreams.constants.DashboardColumns.MOBILIZED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NO_OF_FEMALES;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NO_OF_MALES;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NO_OF_PREGNANT_WOMEN;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NO_OF_ROOMS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NUMBER_OF_ADVERSE_EVENTS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.OFFICIAL_POP_TARGET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.OFFICIAL_POP_TREATMENT_COVERAGE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.PERCENTAGE_VISITED_EFFECTIVELY;
import static com.revealprecision.revealstreams.constants.DashboardColumns.PHONE_NUMBER;
import static com.revealprecision.revealstreams.constants.DashboardColumns.POINT_DISTRIBUTION;
import static com.revealprecision.revealstreams.constants.DashboardColumns.PREGNANT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.RECEIVED_BY_CDD;
import static com.revealprecision.revealstreams.constants.DashboardColumns.REFUSAL;
import static com.revealprecision.revealstreams.constants.DashboardColumns.RETURNED_TO_SUPERVISOR;
import static com.revealprecision.revealstreams.constants.DashboardColumns.REVIEWED_WITH_DECISION;
import static com.revealprecision.revealstreams.constants.DashboardColumns.SICK;
import static com.revealprecision.revealstreams.constants.DashboardColumns.SPRAY_COVERAGE_OF_FOUND_STRUCTURES;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_COMPLETE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_NOT_YET_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_PARTIALLY_COMPLETE;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_REFUSED_ABSENT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_REMAINING_TO_SPRAY_TO_REACH_90;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_SPRAYED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURES_VISITED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.STRUCTURE_STATUS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TARGET_SPRAY_AREAS;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_LIVING_ON_THE_STREET;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_STRUCTURES;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_STRUCTURES_FOUND;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_STRUCTURES_TARGETED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_STRUCTURE_COUNT;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_TREATED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.TOTAL_UNTREATED;
import static com.revealprecision.revealstreams.constants.DashboardColumns.VISITED_AREAS;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reveal.locales")
@Setter
@Getter
public class TranslationProperties {

  private Map<String, Map<String, String>> translations = Map.of(
      "en",
      Stream.<Pair<String, String>>of(
          Pair.of(TARGET_SPRAY_AREAS, "Targeted spray areas"),
          Pair.of(VISITED_AREAS, "Total  Spray Areas Visited"),
          Pair.of(TOTAL_STRUCTURES, "Total structures"),
          Pair.of(TOTAL_STRUCTURES_TARGETED, "Total Structures Targeted"),
          Pair.of(TOTAL_STRUCTURES_FOUND, "Total Structures Found"),
          Pair.of(STRUCTURES_SPRAYED, "Total Structures Sprayed"),
          Pair.of(PERCENTAGE_VISITED_EFFECTIVELY, "Spray Areas Effectively sprayed"),
          Pair.of(STRUCTURE_STATUS, "Structure Status"),
          Pair.of(NO_OF_ROOMS, "No of Rooms"),
          Pair.of(NO_OF_MALES, "No of Males"),
          Pair.of(NO_OF_FEMALES, "No of Females"),
          Pair.of(NO_OF_PREGNANT_WOMEN, "No of Pregnant Women"),
          Pair.of(FOUND_COVERAGE, "Found Coverage (Found/Target)"),
          Pair.of(SPRAY_COVERAGE_OF_FOUND_STRUCTURES, "Spray Coverage of Found(Sprayed/Found)"),
          Pair.of(STRUCTURES_REMAINING_TO_SPRAY_TO_REACH_90,
              "Structures remaining to spray to reach 90% spray coverage"),
          Pair.of(REVIEWED_WITH_DECISION, "Reviewed with decision"),
          Pair.of(MOBILIZED, "Mobilized"), Pair.of(OFFICIAL_POP_TARGET, "Official Pop Target"),
          Pair.of(FIELD_VERIFIED_POP_TARGET, "Field Verified Pop Target"),
          Pair.of(TOTAL_TREATED, "Total Treated"),
          Pair.of(OFFICIAL_POP_TREATMENT_COVERAGE, "Official Pop Treatment Coverage"),
          Pair.of(FIELD_VERIFIED_POP_TREATMENT_COVERAGE, "Field Verified Pop Treatment Coverage"),
          Pair.of(TOTAL_STRUCTURE_COUNT, "Total Structure Count"),
          Pair.of(STRUCTURES_COMPLETE, "Structures Complete"),
          Pair.of(STRUCTURES_VISITED, "Structures Visited"),
          Pair.of(STRUCTURES_PARTIALLY_COMPLETE, "Structures Partially Complete"),
          Pair.of(STRUCTURES_REFUSED_ABSENT, "Structures Refused / Absent"),
          Pair.of(STRUCTURES_NOT_YET_VISITED, "Structures Not Visited Yet"),
          Pair.of(COVERAGE_OF_STRUCTURES_VISITED, "Coverage Of Structures Visited"),
          Pair.of(COVERAGE_OF_STRUCTURES_COMPLETED, "Coverage Of Structures Completed"),

          Pair.of(RECEIVED_BY_CDD, "Received by CDD"),
          Pair.of(ADMINISTERED, "Administered"),
          Pair.of(LOST_DAMAGED, "Lost / Damaged"),
          Pair.of(NUMBER_OF_ADVERSE_EVENTS, "# of adverse events"),
          Pair.of(RETURNED_TO_SUPERVISOR, "Returned to supervisor"),
          Pair.of(BUSINESS_STATUS, "Business Status"),
          Pair.of(HOUSEHOLD_DISTRIBUTION, "Household Distribution"),
          Pair.of(POINT_DISTRIBUTION, "Point Distribution"),
          Pair.of(TOTAL_LIVING_ON_THE_STREET, "Total living on the street"),
          Pair.of(MALES_5_14, "Male 5-14 years"),
          Pair.of(MALES_15, "Male 15+ years"),
          Pair.of(FEMALES_5_14, "Female 5-14 years"),
          Pair.of(FEMALES_15, "Female 15+ years"),
          Pair.of(TOTAL_UNTREATED, "Total Untreated"),
          Pair.of(PREGNANT, "Total Untreated Pregnant"),
          Pair.of(CHILD_UNDER_5, "Total Untreated Child < 5"),
          Pair.of(SICK, "Total Untreated Sick"),
          Pair.of(ABSENT, "Total Untreated Absent"),
          Pair.of(REFUSAL, "Total Untreated Refusal"),
          Pair.of(PHONE_NUMBER, "Phone Number"),
          Pair.of(NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD, "Number of Structures within household"),
          Pair.of(HEAD_OF_HOUSE_HOLD, "Head of Household")).collect(
          Collectors.toMap(Pair::getFirst, Pair::getSecond)),
      "pt",
      Stream.of(
          Pair.of(TARGET_SPRAY_AREAS, "Targeted spray areas pt"),
          Pair.of(VISITED_AREAS, "Total  Spray Areas Visited pt"),
          Pair.of(TOTAL_STRUCTURES, "Total structures pt"),
          Pair.of(TOTAL_STRUCTURES_TARGETED, "Total Structures Targeted pt"),
          Pair.of(TOTAL_STRUCTURES_FOUND, "Total Structures Found pt"),
          Pair.of(STRUCTURES_SPRAYED, "Total Structures Sprayed pt"),
          Pair.of(PERCENTAGE_VISITED_EFFECTIVELY, "Spray Areas Effectively sprayed pt"),
          Pair.of(STRUCTURE_STATUS, "Structure Status pt"),
          Pair.of(NO_OF_ROOMS, "No of Rooms pt"),
          Pair.of(NO_OF_MALES, "No of Males pt"),
          Pair.of(NO_OF_FEMALES, "No of Females pt"),
          Pair.of(NO_OF_PREGNANT_WOMEN, "No of Pregnant Women pt"),
          Pair.of(FOUND_COVERAGE, "Found Coverage (Found/Target) pt"),
          Pair.of(SPRAY_COVERAGE_OF_FOUND_STRUCTURES, "Spray Coverage of Found(Sprayed/Found) pt"),
          Pair.of(STRUCTURES_REMAINING_TO_SPRAY_TO_REACH_90,
              "Structures remaining to spray to reach 90% spray coverage pt"),
          Pair.of(REVIEWED_WITH_DECISION, "Reviewed with decision pt")
      ).collect(
          Collectors.toMap(Pair::getFirst, Pair::getSecond)),
      "fr",
      Stream.<Pair<String, String>>of(
          Pair.of(OFFICIAL_POP_TARGET, "Couverture officielle du anti-pop"),
          Pair.of(FIELD_VERIFIED_POP_TARGET, "Couverture du anti-pop vérifiée sur le terrain"),
          Pair.of(TOTAL_TREATED, "totale traitée"),
          Pair.of(OFFICIAL_POP_TREATMENT_COVERAGE, "Couverture officielle du traitement anti-pop"),
          Pair.of(FIELD_VERIFIED_POP_TREATMENT_COVERAGE,
              "Couverture du traitement anti-pop vérifiée sur le terrain"),
          Pair.of(TOTAL_STRUCTURE_COUNT, "Nombre total de structures"),
          Pair.of(STRUCTURES_COMPLETE, "Ouvrages achevés"),
          Pair.of(STRUCTURES_VISITED, "Ouvrages visités"),
          Pair.of(STRUCTURES_PARTIALLY_COMPLETE, "Structures partiellement achevées"),
          Pair.of(STRUCTURES_REFUSED_ABSENT, "Structures Refusées / Absentes"),
          Pair.of(STRUCTURES_NOT_YET_VISITED, "Structures non encore visitées"),
          Pair.of(COVERAGE_OF_STRUCTURES_VISITED, "Couverture des structures visitées"),
          Pair.of(COVERAGE_OF_STRUCTURES_COMPLETED, "Couverture des structures achevées"),
          Pair.of(RECEIVED_BY_CDD, "Reçu par CDD"),
          Pair.of(ADMINISTERED, "Administré"),
          Pair.of(LOST_DAMAGED, "Perdu / Endommagé"),
          Pair.of(NUMBER_OF_ADVERSE_EVENTS, "# d'événements indésirables"),
          Pair.of(RETURNED_TO_SUPERVISOR, "Renvoyé au superviseur"),
          Pair.of(BUSINESS_STATUS, "Statut de l'entreprise"),
          Pair.of(HOUSEHOLD_DISTRIBUTION, "Répartition des ménages"),
          Pair.of(POINT_DISTRIBUTION, "Répartition des points"),
          Pair.of(TOTAL_LIVING_ON_THE_STREET, "Total vivant dans la rue"),
          Pair.of(MALES_5_14, "Homme 5-14 ans"),
          Pair.of(MALES_15, "Homme de 15 ans et plus"),
          Pair.of(FEMALES_5_14, "Femme 5-14 ans"),
          Pair.of(FEMALES_15, "Femme de 15 ans et plus"),
          Pair.of(TOTAL_UNTREATED, "Total non traité"),
          Pair.of(PREGNANT, "Total des femmes enceintes non traitées"),
          Pair.of(CHILD_UNDER_5, "Total des enfants non traités < 5"),
          Pair.of(SICK, "Total des malades non traités"),
          Pair.of(ABSENT, "Total des absences non traitées"),
          Pair.of(REFUSAL, "Refus total non traité"),
          Pair.of(PHONE_NUMBER, "Numéro de téléphone"),
          Pair.of(NUMBER_OF_STRUCTURES_WITHIN_HOUSEHOLD, "Nombre de structures au sein du ménage"),
          Pair.of(HEAD_OF_HOUSE_HOLD, "Chef de ménage")).collect(
          Collectors.toMap(Pair::getFirst, Pair::getSecond)));
}
