package com.revealprecision.revealstreams.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.revealprecision.revealstreams.exceptions.NotFoundException;
import com.revealprecision.revealstreams.persistence.domain.Organization;
import com.revealprecision.revealstreams.persistence.domain.Organization.Fields;
import com.revealprecision.revealstreams.persistence.repository.OrganizationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  public Organization findByIdWithChildren(UUID identifier) {
    return organizationRepository.findById(identifier,
            EntityGraphUtils.fromAttributePaths(Fields.children))
        .orElseThrow(() -> new NotFoundException(
            Pair.of(Fields.identifier, identifier),
            Organization.class));
  }

}
