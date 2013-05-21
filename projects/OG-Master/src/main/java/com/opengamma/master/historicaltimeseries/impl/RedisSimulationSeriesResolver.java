/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.master.historicaltimeseries.impl;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

public class RedisSimulationSeriesResolver implements HistoricalTimeSeriesResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(RedisSimulationSeriesResolver.class);

  @Override
  public HistoricalTimeSeriesResolutionResult resolve(ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
                                                      String resolutionKey) {
    if (identifierBundle.size() > 1) {
      s_logger.warn("Attempted to call RedisSimulationSeriesSource with bundle {}. Calls with more than 1 entry in ID bundle are probably misuse of this class.", identifierBundle);
    }
    ExternalId externalId = identifierBundle.getExternalIds().iterator().next();
    final UniqueId uniqueId = UniqueId.of(externalId.getScheme().getName(), externalId.getValue());
    ManageableHistoricalTimeSeriesInfo htsInfo = new ManageableHistoricalTimeSeriesInfo() {
      @Override
      public UniqueId getUniqueId() {
        return uniqueId;
      }

      @Override
      public ExternalIdBundleWithDates getExternalIdBundle() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataField() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataSource() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getDataProvider() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public String getObservationTime() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

      @Override
      public ObjectId getTimeSeriesObjectId() {
        throw new UnsupportedOperationException("Unsupported operation.");
      }

    };
    HistoricalTimeSeriesResolutionResult result = new HistoricalTimeSeriesResolutionResult(htsInfo);
    return result;
  }
}
