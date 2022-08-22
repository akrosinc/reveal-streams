package com.revealprecision.revealstreams.factory;


import com.revealprecision.revealstreams.dto.EntityTagRequest;

public class EntityTagRequestFactory {
  public static EntityTagRequest getCopy(EntityTagRequest entityTag) {

    return EntityTagRequest.builder()
        .tag(entityTag.getTag())
        .aggregationMethod(entityTag.getAggregationMethod())
        .scope(entityTag.getScope())
        .addToMetadata(entityTag.isAddToMetadata())
        .definition(entityTag.getDefinition())
        .valueType(entityTag.getValueType())
        .generationFormula(entityTag.getGenerationFormula())
        .generated(entityTag.isGenerated())
        .resultExpression(entityTag.getResultExpression())
        .formFieldNames(entityTag.getFormFieldNames())
        .isResultLiteral(entityTag.isResultLiteral())
        .entityType(
            entityTag.getEntityType())
        .addToMetadata(entityTag.isAddToMetadata())
        .isAggregate(entityTag.isAggregate())
        .build();
  }
}
