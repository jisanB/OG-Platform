/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * 
 */
public class PDEResults1D {

  private double[] _f;
  private double[] _x;

  double getFunctionValue(int index) {
    return _f[index];
  }

}
