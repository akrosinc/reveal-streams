package com.revealprecision.revealstreams.persistence.domain;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserBulkException extends AbstractAuditableEntity {

    @Id
    @GeneratedValue
    private UUID identifier;

    private String username;

    private String message;

    @ManyToOne
    @JoinColumn(name = "user_bulk_identifier")
    private UserBulk userBulk;
}
