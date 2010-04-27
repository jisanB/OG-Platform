/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;

public class IncompleteBetaFunctionTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double EPS = 1e-9;
  private static final double A = 0.4;
  private static final double B = 0.2;
  private static final int MAX_ITER = 10000;
  private static final Function1D<Double, Double> BETA = new IncompleteBetaFunction(A, B);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeA1() {
    new IncompleteBetaFunction(-A, B);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeA2() {
    new IncompleteBetaFunction(-A, B, EPS, MAX_ITER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeB1() {
    new IncompleteBetaFunction(A, -B);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeB2() {
    new IncompleteBetaFunction(A, -B, EPS, MAX_ITER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeEps() {
    new IncompleteBetaFunction(A, B, -EPS, MAX_ITER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeIter() {
    new IncompleteBetaFunction(A, B, EPS, -MAX_ITER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLow() {
    BETA.evaluate(-0.3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHigh() {
    BETA.evaluate(1.5);
  }

  @Test
  public void test() {
    final double a = RANDOM.nextDouble();
    final double b = RANDOM.nextDouble();
    final double x = RANDOM.nextDouble();
    final Function1D<Double, Double> f1 = new IncompleteBetaFunction(a, b);
    final Function1D<Double, Double> f2 = new IncompleteBetaFunction(b, a);
    assertEquals(f1.evaluate(0.), 0, EPS);
    assertEquals(f1.evaluate(1.), 1, EPS);
    assertEquals(f1.evaluate(x), 1 - f2.evaluate(1 - x), EPS);
  }
}
