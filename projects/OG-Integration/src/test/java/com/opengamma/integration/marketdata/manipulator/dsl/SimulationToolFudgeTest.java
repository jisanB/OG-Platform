/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.opengamma.engine.fudgemsg.ArbitraryViewCycleExecutionSequenceFudgeBuilder;
import com.opengamma.engine.marketdata.manipulator.CompositeMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelector;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

public class SimulationToolFudgeTest {

  /**
   * tests a specific fudge related bug when trying to run the simulation tool against a remote server.
   * it only occurs when there are two or more scenarios in the simulation
   */
  @Test
  public void roundTrip() {
    Simulation.Builder builder = Simulation.builder();
    builder.addScenario().curve().named("foo").apply().parallelShift(0.1).execute();
    builder.addScenario().curve().named("bar").apply().parallelShift(0.1).execute();
    Simulation simulation = builder.build();
    MarketDataSelector selector = CompositeMarketDataSelector.of(simulation.allSelectors());
    ViewCycleExecutionOptions options = ViewCycleExecutionOptions.builder().setMarketDataSelector(selector).create();
    List<ViewCycleExecutionOptions> optionsList = simulation.cycleExecutionOptions(options);
    ArbitraryViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(optionsList);
    FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    ArbitraryViewCycleExecutionSequenceFudgeBuilder fudgeBuilder = new ArbitraryViewCycleExecutionSequenceFudgeBuilder();
    MutableFudgeMsg msg = fudgeBuilder.buildMessage(serializer, sequence);
    FudgeDeserializer deserializer = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    fudgeBuilder.buildObject(deserializer, msg);
  }
}
