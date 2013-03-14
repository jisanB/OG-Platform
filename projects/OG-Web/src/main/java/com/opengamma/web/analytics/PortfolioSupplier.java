/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.google.common.base.Supplier;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class PortfolioSupplier implements Supplier<Portfolio> {

  private final ObjectId _portfolioId;
  private final VersionCorrection _versionCorrection;
  private final PositionSource _positionSource;

  /* package */ PortfolioSupplier(ObjectId portfolioId, VersionCorrection versionCorrection, PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _positionSource = positionSource;
    _portfolioId = portfolioId;
    _versionCorrection = versionCorrection;
  }

  @Override
  public Portfolio get() {
    if (_portfolioId != null) {
      return _positionSource.getPortfolio(_portfolioId, _versionCorrection);
    } else {
      return null;
    }
  }
}
