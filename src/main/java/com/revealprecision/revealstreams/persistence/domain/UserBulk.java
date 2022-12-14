package com.revealprecision.revealstreams.persistence.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.revealprecision.revealstreams.enums.BulkStatusEnum;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@FieldNameConstants
@Audited
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = "entity_status='ACTIVE'")
public class UserBulk extends AbstractAuditableEntity {

  @Id
  @GeneratedValue
  private UUID identifier;

  @Column(nullable = false)
  private String filename;


  @Column(nullable = false)
  private String uploadedBy;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS[X]", timezone = "${spring.jackson.time-zone}")
  @Column(nullable = false)
  private LocalDateTime uploadedDatetime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BulkStatusEnum status;

  @OneToMany(mappedBy = "userBulk")
  private Set<User> users;

  @OneToMany(mappedBy = "userBulk")
  private Set<UserBulkException> userBulkExceptions;
}
