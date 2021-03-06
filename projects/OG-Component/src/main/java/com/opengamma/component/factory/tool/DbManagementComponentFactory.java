/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.factory.tool;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.management.DbManagementUtils;

/**
 * Component factory for a {@link DbManagement} instance.
 * <p>
 * This class is designed to allow protected methods to be overridden.
 */
@BeanDefinition
public class DbManagementComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier under which to publish
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The JDBC connection URL.
   */
  @PropertyDefinition(validate = "notNull")
  private String _jdbcUrl;
  /**
   * The database username.
   */
  @PropertyDefinition(validate = "notNull")
  private String _username;
  /**
   * The database password.
   */
  @PropertyDefinition(validate = "notNull")
  private String _password;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    DbManagement dbManagement = createDbManagement(repo);
    ComponentInfo info = new ComponentInfo(DbManagement.class, getClassifier());
    repo.registerComponent(info, dbManagement);
  }

  /**
   * Creates the management object without registering it.
   * 
   * @param repo  the component repository, only used to register secondary items like lifecycle, not null
   * @return the management object, not null
   */
  protected DbManagement createDbManagement(ComponentRepository repo) {
    // REVIEW jonathan 2012-10-12 -- should not be doing this (PLAT-2745)
    String dbHost;
    if (getJdbcUrl().startsWith("jdbc:sqlserver")) {
      // [PLAT-2745] jdbc:sqlserver://mysvr:1433;integratedSecurity=true;/databaseName=mydb
      dbHost = getJdbcUrl();
    } else if (getJdbcUrl().startsWith("jdbc:oracle")) {
      dbHost = getJdbcUrl();
    } else {
      int lastSlashIdx = getJdbcUrl().lastIndexOf("/");
      if (lastSlashIdx == -1) {
        throw new OpenGammaRuntimeException("JDBC URL must contain '/' before the database name");
      }
      dbHost = getJdbcUrl().substring(0, lastSlashIdx);
    }
    DbManagement dbManagement = DbManagementUtils.getDbManagement(dbHost);
    dbManagement.initialise(dbHost, getUsername(), getPassword());
    return dbManagement;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbManagementComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbManagementComponentFactory.Meta meta() {
    return DbManagementComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbManagementComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbManagementComponentFactory.Meta metaBean() {
    return DbManagementComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JDBC connection URL.
   * @return the value of the property, not null
   */
  public String getJdbcUrl() {
    return _jdbcUrl;
  }

  /**
   * Sets the JDBC connection URL.
   * @param jdbcUrl  the new value of the property, not null
   */
  public void setJdbcUrl(String jdbcUrl) {
    JodaBeanUtils.notNull(jdbcUrl, "jdbcUrl");
    this._jdbcUrl = jdbcUrl;
  }

  /**
   * Gets the the {@code jdbcUrl} property.
   * @return the property, not null
   */
  public final Property<String> jdbcUrl() {
    return metaBean().jdbcUrl().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database username.
   * @return the value of the property, not null
   */
  public String getUsername() {
    return _username;
  }

  /**
   * Sets the database username.
   * @param username  the new value of the property, not null
   */
  public void setUsername(String username) {
    JodaBeanUtils.notNull(username, "username");
    this._username = username;
  }

  /**
   * Gets the the {@code username} property.
   * @return the property, not null
   */
  public final Property<String> username() {
    return metaBean().username().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the database password.
   * @return the value of the property, not null
   */
  public String getPassword() {
    return _password;
  }

  /**
   * Sets the database password.
   * @param password  the new value of the property, not null
   */
  public void setPassword(String password) {
    JodaBeanUtils.notNull(password, "password");
    this._password = password;
  }

  /**
   * Gets the the {@code password} property.
   * @return the property, not null
   */
  public final Property<String> password() {
    return metaBean().password().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DbManagementComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbManagementComponentFactory other = (DbManagementComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getJdbcUrl(), other.getJdbcUrl()) &&
          JodaBeanUtils.equal(getUsername(), other.getUsername()) &&
          JodaBeanUtils.equal(getPassword(), other.getPassword()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJdbcUrl());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUsername());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPassword());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("DbManagementComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("jdbcUrl").append('=').append(JodaBeanUtils.toString(getJdbcUrl())).append(',').append(' ');
    buf.append("username").append('=').append(JodaBeanUtils.toString(getUsername())).append(',').append(' ');
    buf.append("password").append('=').append(JodaBeanUtils.toString(getPassword())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbManagementComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", DbManagementComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code jdbcUrl} property.
     */
    private final MetaProperty<String> _jdbcUrl = DirectMetaProperty.ofReadWrite(
        this, "jdbcUrl", DbManagementComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code username} property.
     */
    private final MetaProperty<String> _username = DirectMetaProperty.ofReadWrite(
        this, "username", DbManagementComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code password} property.
     */
    private final MetaProperty<String> _password = DirectMetaProperty.ofReadWrite(
        this, "password", DbManagementComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "jdbcUrl",
        "username",
        "password");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -1752402828:  // jdbcUrl
          return _jdbcUrl;
        case -265713450:  // username
          return _username;
        case 1216985755:  // password
          return _password;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbManagementComponentFactory> builder() {
      return new DirectBeanBuilder<DbManagementComponentFactory>(new DbManagementComponentFactory());
    }

    @Override
    public Class<? extends DbManagementComponentFactory> beanType() {
      return DbManagementComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code jdbcUrl} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jdbcUrl() {
      return _jdbcUrl;
    }

    /**
     * The meta-property for the {@code username} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> username() {
      return _username;
    }

    /**
     * The meta-property for the {@code password} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> password() {
      return _password;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((DbManagementComponentFactory) bean).getClassifier();
        case -1752402828:  // jdbcUrl
          return ((DbManagementComponentFactory) bean).getJdbcUrl();
        case -265713450:  // username
          return ((DbManagementComponentFactory) bean).getUsername();
        case 1216985755:  // password
          return ((DbManagementComponentFactory) bean).getPassword();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((DbManagementComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -1752402828:  // jdbcUrl
          ((DbManagementComponentFactory) bean).setJdbcUrl((String) newValue);
          return;
        case -265713450:  // username
          ((DbManagementComponentFactory) bean).setUsername((String) newValue);
          return;
        case 1216985755:  // password
          ((DbManagementComponentFactory) bean).setPassword((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DbManagementComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((DbManagementComponentFactory) bean)._jdbcUrl, "jdbcUrl");
      JodaBeanUtils.notNull(((DbManagementComponentFactory) bean)._username, "username");
      JodaBeanUtils.notNull(((DbManagementComponentFactory) bean)._password, "password");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
