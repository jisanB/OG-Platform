/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FXForwardYieldCurveNodePnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }
  
  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getPosition().getSecurity();
    return security instanceof FXForwardSecurity || security instanceof NonDeliverableFXForwardSecurity;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    ComputationTargetSpecification targetSpec = target.toSpecification();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, targetSpec, ValueProperties.all()));
  }
  
  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> payCurveNames = constraints.getValues(ValuePropertyNames.PAY_CURVE);
    if (payCurveNames == null || payCurveNames.size() != 1) {
      return null;
    }
    final Set<String> payCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    if (payCurveCalculationConfigNames == null || payCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE);
    if (receiveCurveNames == null || receiveCurveNames.size() != 1) {
      return null;
    }
    final Set<String> receiveCurveCalculationConfigNames = constraints.getValues(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    if (receiveCurveCalculationConfigNames == null || receiveCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> curveCurrencies = constraints.getValues(ValuePropertyNames.CURVE_CURRENCY);
    if (curveCurrencies == null || curveCurrencies.size() != 1) {
      return null;
    }
    final String payCurveCalculationConfigName = Iterables.getOnlyElement(payCurveCalculationConfigNames);
    final String receiveCurveCalculationConfigName = Iterables.getOnlyElement(receiveCurveCalculationConfigNames);
    final FXForwardSecurity security = (FXForwardSecurity) target.getPosition().getSecurity();
    final String payCurveName = Iterables.getOnlyElement(payCurveNames);
    final String receiveCurveName = Iterables.getOnlyElement(receiveCurveNames);
    final Currency payCurrency = security.accept(ForexVisitors.getPayCurrencyVisitor());
    final Currency receiveCurrency = security.accept(ForexVisitors.getReceiveCurrencyVisitor());
    final Currency curveCurrency = Currency.parse(Iterables.getOnlyElement(curveCurrencies));
    final String curveName;
    final String curveCalculationConfigName;
    if (curveCurrency.equals(payCurrency)) {
      curveName = payCurveName;
      curveCalculationConfigName = payCurveCalculationConfigName;
    } else if (curveCurrency.equals(receiveCurrency)) {
      curveName = receiveCurveName;
      curveCalculationConfigName = receiveCurveCalculationConfigName;
    } else {
      return null;
    }
    final ValueRequirement ycnsRequirement = getYCNSRequirement(payCurveName, payCurveCalculationConfigName, receiveCurveName, receiveCurveCalculationConfigName,
        curveCurrency.getCode(), curveName, security);
    final ValueProperties returnSeriesBaseConstraints = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.RECEIVE_CURVE)
        .withoutAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .withoutAny(ValuePropertyNames.PAY_CURVE)
        .withoutAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withoutAny(ValuePropertyNames.CURVE_CURRENCY)
        .withoutAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS).get();
    final ValueRequirement returnSeriesRequirement = getReturnSeriesRequirement(curveName, curveCurrency, curveCalculationConfigName, returnSeriesBaseConstraints);
    final Set<ValueRequirement> requirements = new HashSet<>();
    requirements.add(ycnsRequirement);
    requirements.add(returnSeriesRequirement);
    return requirements;
  }
  
  private ValueRequirement getYCNSRequirement(final String payCurveName, final String payCurveCalculationConfigName, final String receiveCurveName,
      final String receiveCurveCalculationConfigName, final String currencyName, final String curveName, final Security security) {
    final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfigName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfigName)
        .with(ValuePropertyNames.CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE_CURRENCY, currencyName)
        .with(ValuePropertyNames.CURVE, curveName).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES, ComputationTargetType.SECURITY, security.getUniqueId(), properties);
  }
  
  private ValueRequirement getReturnSeriesRequirement(String curveName, Currency curveCurrency, String curveCalculationConfigName, ValueProperties baseConstraints) {
    ComputationTargetSpecification targetSpec = ComputationTargetType.CURRENCY.specification(curveCurrency);
    ValueProperties constraints = baseConstraints.copy()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName)
        .get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES, targetSpec, constraints);
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    Builder builder = createValueProperties();
    for (ValueSpecification inputSpec : inputs.keySet()) {
      for (String propertyName : inputSpec.getProperties().getProperties()) {
        if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
          continue;
        }
        Set<String> values = inputSpec.getProperties().getValues(propertyName);
        if (values == null || values.isEmpty()) {
          builder.withAny(propertyName);
        } else {
          builder.with(propertyName, values);
        }
      }
    }
    builder.with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    ValueProperties properties = builder.get(); 
    ComputationTargetSpecification targetSpec = target.toSpecification();
    return ImmutableSet.of(
        new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, targetSpec, properties),
        new ValueSpecification(ValueRequirementNames.PNL_SERIES, targetSpec, properties));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    TenorLabelledLocalDateDoubleTimeSeriesMatrix1D returnSeries = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_RETURN_SERIES);
    DoubleLabelledMatrix1D sensitivities = (DoubleLabelledMatrix1D) inputs.getValue(ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    if (returnSeries.size() != sensitivities.size()) {
      throw new OpenGammaRuntimeException("Yield Curve Node Sensitivites vector of size " + sensitivities.size() + " but return series vector of size " + returnSeries.size());
    }
    
    int size = returnSeries.size();
    LocalDateDoubleTimeSeries[] nodesPnlSeries = new LocalDateDoubleTimeSeries[size];
    for (int i = 0; i < size; i++) {
      LocalDateDoubleTimeSeries nodePnlSeries = returnSeries.getValues()[i].multiply(sensitivities.getValues()[i]);
      nodesPnlSeries[i] = nodePnlSeries;
    }
    TenorLabelledLocalDateDoubleTimeSeriesMatrix1D pnlSeriesVector = new TenorLabelledLocalDateDoubleTimeSeriesMatrix1D(returnSeries.getKeys(), returnSeries.getLabels(), nodesPnlSeries);

    ValueProperties resultProperties = desiredValues.iterator().next().getConstraints();
    return ImmutableSet.of(new ComputedValue(new ValueSpecification(ValueRequirementNames.YIELD_CURVE_PNL_SERIES, target.toSpecification(), resultProperties), pnlSeriesVector));
  }

}
