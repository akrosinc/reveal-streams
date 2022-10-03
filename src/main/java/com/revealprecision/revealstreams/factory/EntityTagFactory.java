package com.revealprecision.revealstreams.factory;


import static com.revealprecision.revealstreams.constants.EntityTagScopes.GLOBAL;

import com.revealprecision.revealstreams.dto.EntityTagRequest;
import com.revealprecision.revealstreams.enums.EntityStatus;
import com.revealprecision.revealstreams.persistence.domain.EntityTag;

import com.revealprecision.revealstreams.persistence.domain.FormField;
import com.revealprecision.revealstreams.persistence.domain.LookupEntityType;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityTagFactory {

  public static EntityTag toEntity(EntityTagRequest entityTagRequest,
      LookupEntityType lookupEntityType, Set<FormField> formFields) {

    EntityTag entityTag = EntityTag.builder()
        .tag(entityTagRequest.getTag())
        .definition(entityTagRequest.getDefinition())
        .lookupEntityType(lookupEntityType)
        .valueType(entityTagRequest.getValueType().equals("number")?"double":entityTagRequest.getValueType())
        .aggregationMethod(entityTagRequest.getAggregationMethod())
        .generated(entityTagRequest.isGenerated())
        .referencedFields(entityTagRequest.getReferencedFields())
        .generationFormula(entityTagRequest.getGenerationFormula())
        .scope(entityTagRequest.getScope().equalsIgnoreCase(GLOBAL)?GLOBAL:entityTagRequest.getScope())
        .resultExpression(entityTagRequest.getResultExpression())
        .isResultLiteral(entityTagRequest.isResultLiteral())
        .addToMetadata(entityTagRequest.isAddToMetadata())
        .isAggregate(entityTagRequest.isAggregate())
        .build();

    if (formFields != null) {
      entityTag.setFormFields(formFields);
    }

    entityTag.setEntityStatus(EntityStatus.ACTIVE);
    entityTag.setCreatedBy("reveal-streams");
    entityTag.setModifiedBy("reveal-streams");
    return entityTag;
  }
}
