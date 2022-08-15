package com.revealprecision.revealstreams.persistence.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.revealprecision.revealstreams.persistence.domain.Plan;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends EntityGraphJpaRepository<Plan, UUID> {
}