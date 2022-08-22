package com.revealprecision.revealstreams.service;


import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.LookupEntityType;
import com.revealprecision.revealstreams.persistence.repository.LookupEntityTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LookupEntityTypeService {

  private final LookupEntityTypeRepository lookupEntityTypeRepository;

  public LookupEntityType getLookupEntityTypeByCode(String code) {
    return lookupEntityTypeRepository.findLookupEntityTypeByCode(code).orElseThrow(
        () -> new NotFoundException(Pair.of(LookupEntityType.Fields.code, code),
            LookupEntityType.class));
  }

}
