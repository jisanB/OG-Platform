/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.json;


/**
 * Custom JSON builder
 * 
 * @param <T> class that can be serialised or deserialised by this builder
 */
public interface JSONBuilder<T>  {

  /**
   * Converts from JSON to java object
   * 
   * @param json the JSON document, not-null
   * @return the created object, null if json is invalid
   */
  T fromJSON(String json);
  
  /**
   * Converts from Java object to JSON document
   * 
   * @param object the java object, not-null
   * @return the created JSON document
   */
  String toJSON(T object);
}
