/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.swap.FixedFloatSwapDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.SwapConvention;
import com.opengamma.financial.instrument.swap.TenorSwapDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapLegVisitor;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurityVisitor;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SwapSecurityConverter implements SwapSecurityVisitor<FixedIncomeInstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
  private final RegionSource _regionSource;

  public SwapSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final RegionSource regionSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(regionSource, "region source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
    _regionSource = regionSource;
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FixedIncomeInstrumentDefinition<?> visitSwapSecurity(final SwapSecurity security) {
    if (!(security.getPayLeg() instanceof InterestRateLeg) || !(security.getReceiveLeg() instanceof InterestRateLeg)) {
      throw new OpenGammaRuntimeException("Can only handle interest rate swaps");
    }
    final InterestRateLeg payLeg = (InterestRateLeg) security.getPayLeg();
    final InterestRateLeg receiveLeg = (InterestRateLeg) security.getReceiveLeg();
    if (!((InterestRateNotional) payLeg.getNotional()).getCurrency().equals(
        ((InterestRateNotional) receiveLeg.getNotional()).getCurrency())) {
      throw new OpenGammaRuntimeException("Can only handle swaps with the same currency on the pay and receive legs");
    }
    final ZonedDateTime effectiveDate = security.getEffectiveDate().toZonedDateTime();
    final ZonedDateTime maturityDate = security.getMaturityDate().toZonedDateTime();
    final Currency currency = ((InterestRateNotional) payLeg.getNotional()).getCurrency();
    if (payLeg instanceof FixedInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return getFixedFloatSwapDefinition(security, effectiveDate, maturityDate, currency, true);
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FixedInterestRateLeg) {
      return getFixedFloatSwapDefinition(security, effectiveDate, maturityDate, currency, false);
    } else if (payLeg instanceof FloatingInterestRateLeg && receiveLeg instanceof FloatingInterestRateLeg) {
      return getTenorSwapDefinition(security, effectiveDate, maturityDate, currency);
    }
    throw new OpenGammaRuntimeException("Can only handle fixed / floating swaps and tenor swaps");
  }

  private FixedFloatSwapDefinition getFixedFloatSwapDefinition(final SwapSecurity swapSecurity,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final Currency currency,
      final boolean payFixed) {
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FixedInterestRateLeg fixedLeg = (FixedInterestRateLeg) (payFixed ? payLeg : receiveLeg);
    final FloatingInterestRateLeg floatLeg = (FloatingInterestRateLeg) (payFixed ? receiveLeg : payLeg);
    final Calendar calendar = CalendarUtil.getCalendar(_regionSource, _holidaySource, fixedLeg.getRegionIdentifier());
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_SWAP"));
    final MySwapLegVisitor visitor = new MySwapLegVisitor(effectiveDate, maturityDate, calendar, conventions, currency);
    final FixedSwapLegDefinition fixedLegDefinition = visitor.visitFixedInterestRateLeg(fixedLeg);
    final FloatingSwapLegDefinition floatingLegDefinition = visitor.visitFloatingInterestRateLeg(floatLeg);
    return new FixedFloatSwapDefinition(fixedLegDefinition, floatingLegDefinition);
  }

  private FixedIncomeInstrumentDefinition<TenorSwap<Payment>> getTenorSwapDefinition(final SwapSecurity swapSecurity,
      final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final Currency currency) {
    final SwapLeg payLeg = swapSecurity.getPayLeg();
    final SwapLeg receiveLeg = swapSecurity.getReceiveLeg();
    final FloatingInterestRateLeg floatPayLeg = (FloatingInterestRateLeg) payLeg;
    final FloatingInterestRateLeg floatReceiveLeg = (FloatingInterestRateLeg) receiveLeg;
    final Calendar calendar = CalendarUtil
        .getCalendar(_regionSource, _holidaySource, floatPayLeg.getRegionIdentifier());
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency.getCode() + "_TENOR_SWAP"));
    final MySwapLegVisitor visitor = new MySwapLegVisitor(effectiveDate, maturityDate, calendar, conventions, currency);
    final FloatingSwapLegDefinition payLegDefinition = visitor.visitFloatingInterestRateLeg(floatPayLeg);
    final FloatingSwapLegDefinition receiveLegDefinition = visitor.visitFloatingInterestRateLeg(floatReceiveLeg);
    return new TenorSwapDefinition(payLegDefinition, receiveLegDefinition);
  }

  private class MySwapLegVisitor implements SwapLegVisitor<FixedIncomeInstrumentDefinition<?>> {
    private final ZonedDateTime _effectiveDate;
    private final ZonedDateTime _maturityDate;
    private final Calendar _calendar;
    private final ConventionBundle _conventions;
    private final Currency _currency;
    private final String _currencyCode;

    public MySwapLegVisitor(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
        final Calendar calendar, final ConventionBundle conventions, final Currency currency) {
      _effectiveDate = effectiveDate;
      _maturityDate = maturityDate;
      _calendar = calendar;
      _conventions = conventions;
      _currency = currency;
      _currencyCode = _currency.getCode();
    }

    @Override
    public FixedSwapLegDefinition visitFixedInterestRateLeg(final FixedInterestRateLeg fixedLeg) {
      final ZonedDateTime[] nominalDates = ScheduleCalculator.getUnadjustedDateSchedule(_effectiveDate, _maturityDate,
          fixedLeg.getFrequency());
      //TODO are settlement days really 0 for swaps?    
      final ZonedDateTime[] settlementDates = ScheduleCalculator.getAdjustedDateSchedule(nominalDates,
          fixedLeg.getBusinessDayConvention(), _calendar, 0);
      final double notional = ((InterestRateNotional) fixedLeg.getNotional()).getAmount();
      final double fixedRate = fixedLeg.getRate();
      final SwapConvention convention = new SwapConvention(0, fixedLeg.getDayCount(),
          fixedLeg.getBusinessDayConvention(), _calendar, false, _currencyCode + "_FIXED_LEG_SWAP_CONVENTION");
      return new FixedSwapLegDefinition(_effectiveDate, nominalDates, settlementDates, notional, fixedRate, convention);
    }

    @Override
    public FloatingSwapLegDefinition visitFloatingInterestRateLeg(final FloatingInterestRateLeg floatLeg) {
      final ZonedDateTime[] nominalDates = ScheduleCalculator.getUnadjustedDateSchedule(_effectiveDate, _maturityDate,
          floatLeg.getFrequency());
      //TODO are settlement days really 0 for swaps?    
      final ZonedDateTime[] settlementDates = ScheduleCalculator.getAdjustedDateSchedule(nominalDates,
          floatLeg.getBusinessDayConvention(), _calendar, 0);
      final ZonedDateTime[] resetDates = ScheduleCalculator.getAdjustedResetDateSchedule(_effectiveDate, nominalDates,
          floatLeg.getBusinessDayConvention(), _calendar, _conventions.getSwapFloatingLegSettlementDays());
      final ZonedDateTime[] maturityDates = ScheduleCalculator.getAdjustedMaturityDateSchedule(_effectiveDate,
          nominalDates, floatLeg.getBusinessDayConvention(), _calendar, floatLeg.getFrequency());
      final double notional = ((InterestRateNotional) floatLeg.getNotional()).getAmount();
      final double initialRate = floatLeg.getInitialFloatingRate();
      final double spread = floatLeg.getSpread();
      final SwapConvention convention = new SwapConvention(0, floatLeg.getDayCount(),
          floatLeg.getBusinessDayConvention(), _calendar, false, _currencyCode + "_FLOATING_LEG_SWAP_CONVENTION");
      return new FloatingSwapLegDefinition(_effectiveDate, nominalDates, settlementDates, resetDates, maturityDates,
          notional, initialRate, spread, convention);
    }
  }
}
