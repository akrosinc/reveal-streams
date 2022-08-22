package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.persistence.domain.FormField;
import com.revealprecision.revealstreams.persistence.repository.FormFieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormFieldService {

  private final FormFieldRepository formFieldRepository;

  public FormField findByNameAndFormTitle(String name, String formTitle) {
    return formFieldRepository.findByNameAndFormTitle(name, formTitle);
  }

}
