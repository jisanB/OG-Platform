/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.historicaldata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * In memory HDP for testing.
 * @author jim
 */
public class InMemoryHistoricalDataProvider implements HistoricalDataSource {
  private Map<MetaDataKey, UniqueIdentifier> _metaUniqueIdentifierStore = new ConcurrentHashMap<MetaDataKey, UniqueIdentifier>();
  private Map<UniqueIdentifier, LocalDateDoubleTimeSeries> _timeSeriesStore = new ConcurrentHashMap<UniqueIdentifier, LocalDateDoubleTimeSeries>();
  private static final boolean INCLUDE_LAST_DAY = true;
  
  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemoryTS";

  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uidSupplier;

  /**
   * Creates an empty TimeSeriesSource using the default scheme for any {@link UniqueIdentifier}s created.
   */
  public InMemoryHistoricalDataProvider() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uidSupplier  the supplier of unique identifiers, not null
   */
  public InMemoryHistoricalDataProvider(final Supplier<UniqueIdentifier> uidSupplier) {
    Validate.notNull(uidSupplier, "uidSupplier");
    _uidSupplier = uidSupplier;
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle dsids, String dataSource, String dataProvider,
      String field) {
    UniqueIdentifier uid = _metaUniqueIdentifierStore.get(new MetaDataKey(dsids, dataSource, dataProvider, field));
    if (uid == null) {
      throw new DataNotFoundException("TimeSeries not found: " + uid);
    } else {
      return new ObjectsPair<UniqueIdentifier, LocalDateDoubleTimeSeries>(uid, _timeSeriesStore.get(uid));
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(
      IdentifierBundle dsids, String dataSource, String dataProvider,
      String field, LocalDate start, LocalDate end) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> dtsPair = getHistoricalData(dsids, dataSource, dataProvider, field);
    DoubleTimeSeries<LocalDate> subSeries = dtsPair.getSecond().subSeries(start, true, end, INCLUDE_LAST_DAY);
    dtsPair.setValue((LocalDateDoubleTimeSeries) subSeries);
    return dtsPair;
  }
  
  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid) {
    LocalDateDoubleTimeSeries timeSeries = _timeSeriesStore.get(uid);
    if (timeSeries == null) {
      return new ArrayLocalDateDoubleTimeSeries();
    } else {
      return timeSeries;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, LocalDate end) {
    LocalDateDoubleTimeSeries dts = getHistoricalData(uid);
    return (LocalDateDoubleTimeSeries) dts.subSeries(start, true, end, INCLUDE_LAST_DAY);
  }
 
  public void storeHistoricalTimeSeries(IdentifierBundle dsids, String dataSource, String dataProvider, String field, LocalDateDoubleTimeSeries dts) {
    MetaDataKey metaKey = new MetaDataKey(dsids, dataSource, dataProvider, field);
    UniqueIdentifier uid = null;
    synchronized (this) {
      uid = _metaUniqueIdentifierStore.get(metaKey);
      if (uid == null) {
        uid = _uidSupplier.get();
        _metaUniqueIdentifierStore.put(metaKey, uid);
      }
    }
    _timeSeriesStore.put(uid, dts);
  }
  
  private class MetaDataKey {
    private IdentifierBundle _dsids;
    private String _dataSource;
    private String _dataProvider;
    private String _field;
    
    public MetaDataKey(IdentifierBundle dsids, String dataSource, String dataProvider, String field) {
      _dsids = dsids;
      _dataSource = dataSource;
      _dataProvider = dataProvider;
      _field = field;
    }
    
    @Override
    public int hashCode() {
      return _dsids.hashCode() ^ _field.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof MetaDataKey)) {
        return false;
      }
      MetaDataKey other = (MetaDataKey) obj;
      if (_field == null) {
        if (other._field != null) {
          return false;
        }
      } else if (!_field.equals(other._field)) {
        return false;
      }
      if (_dsids == null) {
        if (other._dsids != null) {
          return false;
        }
      } else if (!_dsids.equals(other._dsids)) {
        return false;
      }
      if (_dataProvider == null) {
        if (other._dataProvider != null) {
          return false;
        }
      } else if (!_dataProvider.equals(other._dataProvider)) {
        return false;
      }
      if (_dataSource == null) {
        if (other._dataSource != null) {
          return false;
        }
      } else if (!_dataSource.equals(other._dataSource)) {
        return false;
      }
      return true;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers) {
    return getHistoricalData(identifiers, null, null, null);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, LocalDate end) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, true, end, INCLUDE_LAST_DAY);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String field, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> tsPair = getHistoricalData(identifiers, dataSource, dataProvider, field);
    if (tsPair != null) {
      LocalDateDoubleTimeSeries timeSeries = (LocalDateDoubleTimeSeries) tsPair.getSecond().subSeries(start, inclusiveStart, end, exclusiveEnd);
      return Pair.of(tsPair.getKey(), timeSeries);
    } else {
      return null;
    }
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    LocalDateDoubleTimeSeries timeseries = getHistoricalData(uid);
    if (timeseries != null) {
      return (LocalDateDoubleTimeSeries) timeseries.subSeries(start, inclusiveStart, end, exclusiveEnd);
    } else {
      return null;
    }
  }

}
