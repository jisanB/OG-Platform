/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.function;

import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * A function search result for display in a green screen.
 */
@BeanDefinition
public class WebFunctionSearchResult extends DirectBean {

  @PropertyDefinition
  private List<WebFunctionTypeDetails> _functions;
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code WebFunctionSearchResult}.
   * @return the meta-bean, not null
   */
  public static WebFunctionSearchResult.Meta meta() {
    return WebFunctionSearchResult.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(WebFunctionSearchResult.Meta.INSTANCE);
  }

  @Override
  public WebFunctionSearchResult.Meta metaBean() {
    return WebFunctionSearchResult.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the functions.
   * @return the value of the property
   */
  public List<WebFunctionTypeDetails> getFunctions() {
    return _functions;
  }

  /**
   * Sets the functions.
   * @param functions  the new value of the property
   */
  public void setFunctions(List<WebFunctionTypeDetails> functions) {
    this._functions = functions;
  }

  /**
   * Gets the the {@code functions} property.
   * @return the property, not null
   */
  public final Property<List<WebFunctionTypeDetails>> functions() {
    return metaBean().functions().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public WebFunctionSearchResult clone() {
    BeanBuilder<? extends WebFunctionSearchResult> builder = metaBean().builder();
    for (MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = ((Bean) value).clone();
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      WebFunctionSearchResult other = (WebFunctionSearchResult) obj;
      return JodaBeanUtils.equal(getFunctions(), other.getFunctions());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getFunctions());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("WebFunctionSearchResult{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("functions").append('=').append(JodaBeanUtils.toString(getFunctions())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code WebFunctionSearchResult}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code functions} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<WebFunctionTypeDetails>> _functions = DirectMetaProperty.ofReadWrite(
        this, "functions", WebFunctionSearchResult.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "functions");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -140572773:  // functions
          return _functions;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends WebFunctionSearchResult> builder() {
      return new DirectBeanBuilder<WebFunctionSearchResult>(new WebFunctionSearchResult());
    }

    @Override
    public Class<? extends WebFunctionSearchResult> beanType() {
      return WebFunctionSearchResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code functions} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<WebFunctionTypeDetails>> functions() {
      return _functions;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -140572773:  // functions
          return ((WebFunctionSearchResult) bean).getFunctions();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -140572773:  // functions
          ((WebFunctionSearchResult) bean).setFunctions((List<WebFunctionTypeDetails>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
