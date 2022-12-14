package com.revealprecision.revealstreams.service;


import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.BOOLEAN;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.DATE;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.DOUBLE;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.INTEGER;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.OBJECT;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.STRING;

import com.revealprecision.revealstreams.constants.EntityTagScopes;
import com.revealprecision.revealstreams.constants.KafkaConstants;
import com.revealprecision.revealstreams.enums.EntityStatus;
import com.revealprecision.revealstreams.factory.LocationMetadataEventFactory;
import com.revealprecision.revealstreams.factory.PersonMetadataEventFactory;
import com.revealprecision.revealstreams.messaging.message.EntityTagEvent;
import com.revealprecision.revealstreams.messaging.message.LocationMetadataEvent;
import com.revealprecision.revealstreams.messaging.message.PersonMetadataEvent;
import com.revealprecision.revealstreams.persistence.domain.EntityTag;
import com.revealprecision.revealstreams.persistence.domain.Location;
import com.revealprecision.revealstreams.persistence.domain.Person;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import com.revealprecision.revealstreams.persistence.domain.metadata.LocationMetadata;
import com.revealprecision.revealstreams.persistence.domain.metadata.PersonMetadata;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.Metadata;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataList;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.MetadataObj;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.TagData;
import com.revealprecision.revealstreams.persistence.domain.metadata.infra.TagValue;
import com.revealprecision.revealstreams.persistence.repository.LocationMetadataRepository;
import com.revealprecision.revealstreams.persistence.repository.PersonMetadataRepository;
import com.revealprecision.revealstreams.props.KafkaProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

  private final LocationMetadataRepository locationMetadataRepository;
  private final PersonMetadataRepository personMetadataRepository;
  private final KafkaTemplate<String, LocationMetadataEvent> locationMetadataKafkaTemplate;
  private final KafkaTemplate<String, PersonMetadataEvent> personMetadataKafkaTemplate;
  private final KafkaProperties kafkaProperties;
  private final LocationService locationService;


  public List<MetadataObj> getLocationMetadataByTagName(UUID locationIdentifier,UUID planIdentifier, String tagName, LocalDateTime localDateTime, String scope) {
    Optional<LocationMetadata> locationMetadataOptional = locationMetadataRepository.findLocationMetadataByLocation_Identifier(
        locationIdentifier);
    if (locationMetadataOptional.isPresent()) {

      LocationMetadata locationMetadata = locationMetadataOptional.get();

      return locationMetadata.getEntityValue().getMetadataObjs().stream()
          .filter(metadataObj -> metadataObj.getTag().equals(tagName))
          .filter(metadataObj -> metadataObj.getCurrent().getMeta().getPlanId().equals(planIdentifier))
          .filter(metadataObj -> {
            if (EntityTagScopes.DATE.equals(scope)) {
              return metadataObj.isDateScope() && metadataObj.getDateForDateScope()
                  .equals(localDateTime.toString());
            } else {
              return true;
            }
          })
          .collect(Collectors.toList());

    } else {
      return null;
    }
  }



  public Object updateMetaData(UUID identifier, Object tagValue,
      Plan plan, UUID taskIdentifier,
      String user, String dataType, EntityTagEvent tag, String type, Object entity, String taskType,
      Class<?> aClass, String tagKey, String finalDateForScopeDateFields1) {
    if (aClass == Person.class) {
      return updatePersonMetadata(identifier, tagValue, plan, taskIdentifier, user, dataType, tag,
          type, (Person) entity, taskType, tagKey, finalDateForScopeDateFields1);
    } else if (aClass == Location.class) {
      return updateLocationMetadata(identifier, tagValue, plan, taskIdentifier, user, dataType, tag,
          type, (Location) entity, taskType, tagKey, finalDateForScopeDateFields1);
    }
    return null;
  }

  @Transactional
  public PersonMetadata updatePersonMetadata(UUID personIdentifier, Object tagValue,
      Plan plan, UUID taskIdentifier,
      String user, String dataType, EntityTagEvent tag, String type, Person person, String taskType,
      String tagKey, String dateForScopeDateFields) {

    PersonMetadata personMetadata;

    Optional<PersonMetadata> optionalPersonMetadata = personMetadataRepository.findPersonMetadataByPerson_Identifier(
        personIdentifier);
    if (optionalPersonMetadata.isPresent()) {

      OptionalInt optionalArrIndex = IntStream.range(0,
          optionalPersonMetadata.get().getEntityValue().getMetadataObjs().size()).filter(i ->
          optionalPersonMetadata.get().getEntityValue().getMetadataObjs().get(i).getTag()
              .equals(tag.getTag())
      ).findFirst();

      if (optionalArrIndex.isPresent()) {
        personMetadata = optionalPersonMetadata.get();

        int arrIndex = optionalArrIndex.getAsInt();
        //TODO: Add history

        personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().setValue(
            getTagValue(tagValue, dataType,
                personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent()
                    .getValue()));
        personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setUpdateDateTime(LocalDateTime.now());
        personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setUserId(user);
        personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setTaskType(taskType);

        //TODO: Add history

      } else {
        // tag does not exist in list
        MetadataObj metadataObj = getMetadataObj(tagValue,
            plan == null ? null : plan.getIdentifier(), taskIdentifier,
            user, dataType, tag, type, taskType, tagKey, dateForScopeDateFields);

        personMetadata = optionalPersonMetadata.get();
        List<MetadataObj> metadataObjs = new ArrayList<>(
            personMetadata.getEntityValue().getMetadataObjs());
        metadataObjs.add(metadataObj);
        personMetadata.getEntityValue().setMetadataObjs(metadataObjs);

      }
    } else {
      //person metadata does not exist
      personMetadata = new PersonMetadata();
      //TODO: check this
      personMetadata.setPerson(person);

      MetadataObj metadataObj = getMetadataObj(tagValue, plan == null ? null : plan.getIdentifier(),
          taskIdentifier, user,
          dataType, tag, type, taskType, tagKey, dateForScopeDateFields);

      MetadataList metadataList = new MetadataList();
      metadataList.setMetadataObjs(List.of(metadataObj));
      personMetadata.setEntityValue(metadataList);
      personMetadata.setEntityStatus(EntityStatus.ACTIVE);
      personMetadata.setCreatedBy("reveal-streams");
      personMetadata.setCreatedDatetime(LocalDateTime.now());
    }

    personMetadata.setModifiedBy("reveal-streams");
    personMetadata.setModifiedDatetime(LocalDateTime.now());

    PersonMetadata savedPersonMetadata = personMetadataRepository.save(personMetadata);

    List<UUID> locationList = locationService.getLocationsByPeople(person.getIdentifier())
        .stream()
        .map(Location::getIdentifier).collect(Collectors.toList());

    PersonMetadataEvent personMetadataEvent = PersonMetadataEventFactory.getPersonMetadataEvent(
        plan, locationList, savedPersonMetadata);

    personMetadataKafkaTemplate.send(
        kafkaProperties.getTopicMap().get(KafkaConstants.PERSON_METADATA_UPDATE),
        personMetadataEvent);

    return savedPersonMetadata;
  }


  public LocationMetadata updateLocationMetadata(UUID locationIdentifier, Object tagValue,
      Plan plan, UUID taskIdentifier,
      String user, String dataType, EntityTagEvent locationEntityTag, String type,
      Location location,
      String taskType, String tagKey, String dateForScopeDateFields) {

    LocationMetadata locationMetadata;

    Optional<LocationMetadata> locationMetadataOptional = locationMetadataRepository.findLocationMetadataByLocation_Identifier(
        locationIdentifier);
    if (locationMetadataOptional.isPresent()) {
      locationMetadata = locationMetadataOptional.get();

      OptionalInt optionalArrIndex = OptionalInt.empty();
      if (locationEntityTag.getScope() == null || (locationEntityTag.getScope() != null
          && !locationEntityTag.getScope().equals("Date"))) {
        optionalArrIndex = IntStream.range(0,
                locationMetadataOptional.get().getEntityValue().getMetadataObjs().size())
            .filter(
                i -> locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(i)
                    .getTag()
                    .equals(locationEntityTag.getTag()))
            .findFirst();
      } else {
        optionalArrIndex = IntStream.range(0,
                locationMetadataOptional.get().getEntityValue().getMetadataObjs().size())
            .filter(
                i -> locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(i)
                    .getTag()
                    .equals(locationEntityTag.getTag())
                    && (!locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(i)
                    .isDateScope() ||
                    locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(i)
                        .getDateForDateScope().equals(dateForScopeDateFields)))
            .findFirst();
      }
      if (optionalArrIndex.isPresent()) {
        //tag exists

        int arrIndex = optionalArrIndex.getAsInt();
        //TODO: Add history

        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().setValue(
            getTagValue(tagValue, dataType,
                locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent()
                    .getValue()));
        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setUpdateDateTime(LocalDateTime.now());
        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setUserId(user);
        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getCurrent().getMeta()
            .setTaskType(taskType);

        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).setActive(true);
        if (locationEntityTag.getScope().equals("Date")) {
          if (dateForScopeDateFields != null) {
            locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex)
                .setDateForDateScope(dateForScopeDateFields);
            locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).setDateScope(true);
            locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).setCaptureNumber(
                LocalDate.parse(dateForScopeDateFields, DateTimeFormatter.ISO_LOCAL_DATE)
                    .toEpochDay());
          }
        }

        //TODO: Add history

      } else {
        // tag does not exist in list
        MetadataObj metadataObj = getMetadataObj(tagValue,
            plan == null ? null : plan.getIdentifier(), taskIdentifier,
            user,
            dataType, locationEntityTag, type, taskType, tagKey, dateForScopeDateFields);


        List<MetadataObj> temp = new ArrayList<>(
            locationMetadata.getEntityValue().getMetadataObjs());
        temp.add(metadataObj);
        locationMetadata.getEntityValue().setMetadataObjs(temp);

      }
    } else {
      //location metadata does not exist
      locationMetadata = new LocationMetadata();
      //TODO: check this

      location.setIdentifier(locationIdentifier);
      locationMetadata.setLocation(location);

      MetadataObj metadataObj = getMetadataObj(tagValue, plan == null ? null : plan.getIdentifier(),
          taskIdentifier, user,
          dataType, locationEntityTag, type, taskType, tagKey, dateForScopeDateFields);

      MetadataList metadataList = new MetadataList();
      metadataList.setMetadataObjs(List.of(metadataObj));
      locationMetadata.setEntityValue(metadataList);

      locationMetadata.setEntityStatus(EntityStatus.ACTIVE);
      locationMetadata.setCreatedBy("reveal-streams");
      locationMetadata.setCreatedDatetime(LocalDateTime.now());

    }
    locationMetadata.setModifiedBy("reveal-streams");
    locationMetadata.setModifiedDatetime(LocalDateTime.now());
    LocationMetadata savedLocationMetadata = locationMetadataRepository.save(locationMetadata);

    LocationMetadataEvent locationMetadataEvent = LocationMetadataEventFactory.
        getLocationMetadataEvent(
            plan, location, savedLocationMetadata);

    locationMetadataKafkaTemplate.send(
        kafkaProperties.getTopicMap().get(KafkaConstants.LOCATION_METADATA_AGGREGATE_UPDATE),
        locationMetadataEvent);
    return savedLocationMetadata;
  }

  public LocationMetadata deactivateLocationMetadata(UUID locationIdentifier, EntityTag tag,
      Plan plan) {

    LocationMetadata locationMetadata;

    Optional<LocationMetadata> locationMetadataOptional = locationMetadataRepository.findLocationMetadataByLocation_Identifier(
        locationIdentifier);
    if (locationMetadataOptional.isPresent()) {

      locationMetadata = locationMetadataOptional.get();

      OptionalInt optionalArrIndex = IntStream.range(0,
              locationMetadataOptional.get().getEntityValue().getMetadataObjs().size())
          .filter(
              i -> locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(i).getTag()
                  .equals(tag.getTag()))
          .findFirst();

      if (optionalArrIndex.isPresent()) {
        //tag exists

        int arrIndex = optionalArrIndex.getAsInt();
        TagData oldObj = SerializationUtils.clone(
            locationMetadataOptional.get().getEntityValue().getMetadataObjs().get(arrIndex)
                .getCurrent());

        locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).setActive(false);

        if (locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getHistory()
            != null) {
          locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getHistory()
              .add(oldObj);
        } else {
          locationMetadata.getEntityValue().getMetadataObjs().get(arrIndex)
              .setHistory(List.of(oldObj));
        }

        locationMetadata.setModifiedBy("reveal-streams");
        locationMetadata.setModifiedDatetime(LocalDateTime.now());

        LocationMetadata savedLocationMetadata = locationMetadataRepository.save(locationMetadata);

        LocationMetadataEvent locationMetadataEvent = LocationMetadataEventFactory.
            getLocationMetadataEvent(
                plan, null, savedLocationMetadata);

        locationMetadataKafkaTemplate.send(
            kafkaProperties.getTopicMap().get(KafkaConstants.LOCATION_METADATA_AGGREGATE_UPDATE),
            locationMetadataEvent);
        return savedLocationMetadata;
      } else {
        // tag does not exist in list
        log.info("tag {} not present...ignoring ", tag);
      }
    } else {
      //location metadata does not exist
      log.info("no metadata for entity {} not present...ignoring ", locationIdentifier);
    }

    return null;
  }


  public PersonMetadata deactivatePersonMetadata(UUID locationIdentifier, EntityTag tag,
      Plan plan) {

    PersonMetadata personMetadata;

    Optional<PersonMetadata> personMetadataOptional = personMetadataRepository.findPersonMetadataByPerson_Identifier(
        locationIdentifier);
    if (personMetadataOptional.isPresent()) {

      OptionalInt optionalArrIndex = IntStream.range(0,
              personMetadataOptional.get().getEntityValue().getMetadataObjs().size())
          .filter(
              i -> personMetadataOptional.get().getEntityValue().getMetadataObjs().get(i).getTag()
                  .equals(tag.getTag()))
          .findFirst();

      if (optionalArrIndex.isPresent()) {
        //tag exists
        personMetadata = personMetadataOptional.get();

        int arrIndex = optionalArrIndex.getAsInt();
        TagData oldObj = SerializationUtils.clone(
            personMetadataOptional.get().getEntityValue().getMetadataObjs().get(arrIndex)
                .getCurrent());

        personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).setActive(false);

        if (personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getHistory()
            != null) {
          personMetadata.getEntityValue().getMetadataObjs().get(arrIndex).getHistory()
              .add(oldObj);
        } else {
          personMetadata.getEntityValue().getMetadataObjs().get(arrIndex)
              .setHistory(List.of(oldObj));
        }

        personMetadata.setModifiedBy("reveal-streams");
        personMetadata.setModifiedDatetime(LocalDateTime.now());

        PersonMetadata savedPersonMetadata = personMetadataRepository.save(personMetadata);

        List<Location> locationsByPeople = locationService.getLocationsByPeople(
            personMetadata.getPerson().getIdentifier());

        PersonMetadataEvent personMetadataEvent = PersonMetadataEventFactory.getPersonMetadataEvent(
            plan,
            locationsByPeople.stream().map(Location::getIdentifier).collect(Collectors.toList()),
            savedPersonMetadata);

        personMetadataKafkaTemplate.send(
            kafkaProperties.getTopicMap().get(KafkaConstants.PERSON_METADATA_UPDATE),
            personMetadataEvent);
        return savedPersonMetadata;
      } else {
        // tag does not exist in list
        log.info("tag {} not present...ignoring ", tag);
      }
    } else {
      //location metadata does not exist
      log.info("no metadata for entity {} not present...ignoring ", locationIdentifier);
    }

    return null;
  }

  private MetadataObj getMetadataObj(Object tagValue, UUID planIdentifier, UUID taskIdentifier,
      String user,
      String dataType, EntityTagEvent tag, String type, String taskType, String tagKey,
      String dateForScopeDateFields) {
    Metadata metadata = new Metadata();
    metadata.setPlanId(planIdentifier);
    metadata.setTaskId(taskIdentifier);
    metadata.setTaskType(taskType);
    metadata.setCreateDateTime(LocalDateTime.now());
    metadata.setUpdateDateTime(LocalDateTime.now());
    metadata.setUserId(user);

    TagValue value = getTagValue(tagValue, dataType, new TagValue());

    TagData tagData = new TagData();
    tagData.setMeta(metadata);
    tagData.setValue(value);

    MetadataObj metadataObj = new MetadataObj();
    metadataObj.setDataType(dataType);
    metadataObj.setTag(tag.getTag());
    metadataObj.setType(type);
    metadataObj.setEntityTagId(tag.getIdentifier());
    metadataObj.setCurrent(tagData);
    metadataObj.setActive(true);
    metadataObj.setTagKey(tagKey);

    if (tag.getScope().equals(EntityTagScopes.DATE)) {
      if (dateForScopeDateFields != null) {
        metadataObj.setDateForDateScope(dateForScopeDateFields);
        metadataObj.setDateScope(true);
        metadataObj.setCaptureNumber(
            LocalDate.parse(dateForScopeDateFields, DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay());
      }
    }

    return metadataObj;
  }


  private TagValue getTagValue(Object tagValue, String dataType, TagValue value) {
    switch (dataType) {
      case STRING:
        value.setValueString((String) tagValue);
        break;
      case INTEGER:
        value.setValueInteger((Integer) tagValue);
        break;
      case DATE:
        value.setValueDate((LocalDateTime) tagValue);
        break;
      case DOUBLE:
        value.setValueDouble((Double) tagValue);
        break;
      case BOOLEAN:
        value.setValueBoolean((Boolean) tagValue);
        break;
      case OBJECT:
        value.getValueObjects().add(tagValue);
        break;
      default:
        value.setValueString((String) tagValue);
        break;
    }
    return value;
  }



}
