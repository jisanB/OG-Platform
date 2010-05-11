/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifiable;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * An immutable specification of a particular computation target that will be resolved
 * later on in a computation process.
 */
public final class ComputationTargetSpecification implements Serializable {

  /**
   * Fudge field name.
   */
  public static final String TYPE_FIELD_NAME = "computationTargetType";
  /**
   * Fudge field name.
   */
  public static final String IDENTIFIER_FIELD_NAME = "computationTargetIdentifier";

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The identifier of the target.
   */
  private final UniqueIdentifier _uid;

  /**
   * Construct a specification that refers to the specified object.
   * 
   * @param target  the target to create a specification for, may be null
   */
  public ComputationTargetSpecification(Object target) {
    _type = ComputationTargetType.determineFromTarget(target);
    switch (_type) {
      case MULTIPLE_POSITIONS:
      case POSITION:
      case SECURITY: {
        _uid = ((UniqueIdentifiable) target).getUniqueIdentifier();
        break;
      }
      case PRIMITIVE: {
        if (target instanceof Identifiable) {
          Identifier id = ((Identifiable) target).getIdentityKey();
          _uid = UniqueIdentifier.of(id.getScheme().getName(), id.getValue());
        } else if (target instanceof Identifier) {
          Identifier id = (Identifier) target;
          _uid = UniqueIdentifier.of(id.getScheme().getName(), id.getValue());
        } else {
          _uid = null;
        }
        break;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + _type);
    }
  }

  /**
   * Creates a lightweight specification of a computation target.
   * @param type  the type of the target, not null
   * @param uid  the target identifier, may be null
   */
  public ComputationTargetSpecification(ComputationTargetType targetType, UniqueIdentifier uid) {
    ArgumentChecker.notNull(targetType, "target type");
    if (targetType != ComputationTargetType.PRIMITIVE) {
      ArgumentChecker.notNull(uid, "identifier");
    }
    _type = targetType;
    _uid = uid;
  }

  /**
   * Creates a lightweight specification of a computation target.
   * @param type  the type of the target, not null
   * @param identifier  the target identifier, may be null
   */
  public ComputationTargetSpecification(ComputationTargetType targetType, Identifier identifier) {
    ArgumentChecker.notNull(targetType, "target type");
    if (targetType != ComputationTargetType.PRIMITIVE) {
      ArgumentChecker.notNull(identifier, "identifier");
    }
    _type = targetType;
    _uid = (identifier == null ? null : UniqueIdentifier.of(identifier.getScheme().getName(), identifier.getValue()));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type of the target.
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * Gets the identifier to the actual target.
   * @return the identifier, may be null
   */
  public Identifier getIdentifier() {
    if (_uid == null) {
      return null;
    }
    return Identifier.of(_uid.getScheme(), _uid.getValue());
  }

  /**
   * Gets the unique identifier, if one exists.
   * @return the unique identifier, may be null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTargetSpecification) {
      ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
      return _type == other._type &&
          ObjectUtils.equals(_uid, other._uid);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    if (_uid != null) {
      result = prime * result + _uid.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("CTSpec[")
      .append(getType())
      .append(", ")
      .append(getUniqueIdentifier())
      .append(']')
      .toString();
  }

  //-------------------------------------------------------------------------
  public void toFudgeMsg(FudgeMessageFactory fudgeContext, MutableFudgeFieldContainer msg) {
    msg.add(TYPE_FIELD_NAME, _type.name());
    FudgeFieldContainer identifierMsg = _uid.toFudgeMsg(fudgeContext);
    msg.add(IDENTIFIER_FIELD_NAME, identifierMsg);
  }

  public static ComputationTargetSpecification fromFudgeMsg(FudgeFieldContainer msg) {
    ComputationTargetType type = ComputationTargetType.valueOf(msg.getString(TYPE_FIELD_NAME));
    Identifier identifier = Identifier.fromFudgeMsg(msg.getMessage(IDENTIFIER_FIELD_NAME));
    return new ComputationTargetSpecification(type, identifier);
  }

}
