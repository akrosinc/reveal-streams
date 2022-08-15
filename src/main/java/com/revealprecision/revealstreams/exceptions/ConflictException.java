package com.revealprecision.revealstreams.exceptions;
import com.revealprecision.revealstreams.exceptions.constant.Error;

public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }

  public ConflictException(Class<?> parentClass, Object parentIdentifier, Class<?> childClass,
      Object childIdentifier) {
    super(String.format(Error.RESOURCES_NOT_WIRED, parentClass.getSimpleName(),
        parentIdentifier.toString(), childClass.getSimpleName(), childIdentifier.toString()));
  }
}
