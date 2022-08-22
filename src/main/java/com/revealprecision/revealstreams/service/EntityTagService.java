package com.revealprecision.revealstreams.service;


import static com.revealprecision.revealstreams.constants.EntityTagDataAggregationMethods.AVERAGE_;
import static com.revealprecision.revealstreams.constants.EntityTagDataAggregationMethods.MAX_;
import static com.revealprecision.revealstreams.constants.EntityTagDataAggregationMethods.MIN_;
import static com.revealprecision.revealstreams.constants.EntityTagDataAggregationMethods.SUM_;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.DOUBLE;
import static com.revealprecision.revealstreams.constants.EntityTagDataTypes.INTEGER;

import com.revealprecision.revealstreams.dto.EntityTagRequest;
import com.revealprecision.revealstreams.enums.LookupEntityTypeCodeEnum;
import com.revealprecision.revealstreams.exceptions.DuplicateCreationException;
import com.revealprecision.revealstreams.factory.EntityTagEventFactory;
import com.revealprecision.revealstreams.factory.EntityTagFactory;
import com.revealprecision.revealstreams.factory.EntityTagRequestFactory;
import com.revealprecision.revealstreams.messaging.message.EntityTagEvent;
import com.revealprecision.revealstreams.persistence.domain.EntityTag;
import com.revealprecision.revealstreams.persistence.domain.FormField;
import com.revealprecision.revealstreams.persistence.domain.LookupEntityType;
import com.revealprecision.revealstreams.persistence.projection.EntityTagRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityTagService {

  private final EntityTagRepository entityTagRepository;
  private final LookupEntityTypeService lookupEntityTypeService;
  private final FormFieldService formFieldService;


  private static final Map<String, List<String>> aggregationMethods = Map.of(
      INTEGER, List.of(SUM_, MAX_, MIN_, AVERAGE_),
      DOUBLE, List.of(SUM_, MAX_, MIN_, AVERAGE_));



  public Optional<EntityTag> getEntityTagByTagName(String name) {
    return entityTagRepository.getFirstByTag(name);
  }


  public EntityTag createEntityTag(EntityTagRequest entityTagRequest, boolean createAggregateTags) {

    LookupEntityType lookupEntityType = lookupEntityTypeService.getLookupEntityTypeByCode(
        entityTagRequest.getEntityType().getLookupEntityType());
    Set<FormField> formFields = null;
    if (entityTagRequest.getFormFieldNames() != null) {
      formFields = entityTagRequest.getFormFieldNames().entrySet().stream()
          .map(entry -> formFieldService.findByNameAndFormTitle(
              entry.getValue(), entry.getKey()))
          .filter(Objects::nonNull).collect(Collectors.toSet());
    }
    Optional<EntityTag> entityTagsByTagAndLookupEntityType_code = getEntityTagByTagNameAndLookupEntityType(
        entityTagRequest.getTag(), LookupEntityTypeCodeEnum.lookup(lookupEntityType.getCode()));
    if (entityTagsByTagAndLookupEntityType_code.isPresent()) {
      throw new DuplicateCreationException(
          "Entity tag with name " + entityTagRequest.getTag() + " for entity type "
              + lookupEntityType.getCode() + " already exists");
    }
    EntityTag save = entityTagRepository.save(
        EntityTagFactory.toEntity(entityTagRequest, lookupEntityType, formFields));

    Set<FormField> finalFormFields = formFields;
    if (createAggregateTags) {
      List<EntityTagEvent> entityTagEvents =
          aggregationMethods.get(save.getValueType()) == null ? null
              : aggregationMethods.get(save.getValueType()).stream()
                  .map(aggregationMethod ->
                      createAggregateEntityTag(entityTagRequest, aggregationMethod,
                          lookupEntityType,
                          finalFormFields, true)).map(EntityTagEventFactory::getEntityTagEvent)
                  .collect(Collectors.toList());

      log.debug("Automatically Created {} for requested tag creation: {}", entityTagEvents,
          entityTagRequest);
    }
    return save;
  }

  public Optional<EntityTag> getEntityTagByTagNameAndLookupEntityType(String name,
      LookupEntityTypeCodeEnum typeCodeEnum) {

    return entityTagRepository.findEntityTagsByTagAndLookupEntityType_Code(name,
        typeCodeEnum.getLookupEntityType());
  }
  private EntityTag createAggregateEntityTag(EntityTagRequest entityTagRequest, String str,
      LookupEntityType lookupEntityType, Set<FormField> formFields, boolean isAggregate) {
    EntityTagRequest entityTagSum = EntityTagRequestFactory.getCopy(entityTagRequest);
    entityTagSum.setTag(entityTagSum.getTag().concat(str));
    entityTagSum.setAggregate(isAggregate);
    return entityTagRepository.save(
        EntityTagFactory.toEntity(entityTagSum, lookupEntityType, formFields));
  }



  public Optional<EntityTag> findEntityTagById(UUID entityTagIdentifier) {
    return entityTagRepository.findById(entityTagIdentifier);
  }



}
