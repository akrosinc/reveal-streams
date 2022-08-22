package com.revealprecision.revealstreams.factory;


import com.revealprecision.revealstreams.messaging.message.FormFieldEvent;
import com.revealprecision.revealstreams.persistence.domain.FormField;

public class FormFieldEventFactory {

  public static FormFieldEvent getFormFieldEvent(FormField formField) {
    return FormFieldEvent.builder()
        .dataType(formField.getDataType())
        .display(formField.getDisplay())
        .dataType(formField.getDataType())
        .identifier(formField.getIdentifier())
        .formTitle(formField.getFormTitle())
        .name(formField.getName())
        .build();
  }
}
