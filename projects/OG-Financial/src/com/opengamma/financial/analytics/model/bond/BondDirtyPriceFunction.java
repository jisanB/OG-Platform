/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.bond.BondPriceCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondDirtyPriceFunction extends BondFunction {
  public BondDirtyPriceFunction() {
    //_requirementName = MarketDataRequirementNames.MARKET_VALUE;
    _fieldName = "PX_LAST";
  }

  @Override
  protected Set<ComputedValue> getComputedValues(final Position position, final Bond bond, final double cleanPrice) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, position), getUniqueIdentifier());
    final double dirtyPrice = BondPriceCalculator.dirtyPrice(bond, cleanPrice / 100.0);
    return Sets.newHashSet(new ComputedValue(specification, dirtyPrice * 100.0));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.DIRTY_PRICE, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BondDirtyPriceFunction";
  }

}
