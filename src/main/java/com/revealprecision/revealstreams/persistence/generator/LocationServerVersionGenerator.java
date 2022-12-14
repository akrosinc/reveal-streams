package com.revealprecision.revealstreams.persistence.generator;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.tuple.ValueGenerator;

public class LocationServerVersionGenerator implements ValueGenerator<Long> {

  private static final String SELECT_QUERY = "select nextval('location_server_version_seq')";

  @Override
  public Long generateValue(Session session, Object owner) {
    NativeQuery nextValQuery = session.createSQLQuery(SELECT_QUERY);
    Number nextVal = (Number) nextValQuery.setFlushMode(FlushMode.COMMIT).uniqueResult();
    return nextVal.longValue();
  }
}
