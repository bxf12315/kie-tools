/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.server.json.impl;

import org.jboss.errai.marshalling.client.api.json.*;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class ErraiJSONValue implements EJValue {
  private final Object obj;
  
  final static EJNull NULL_INSTANCE = new EJNull() {
  };

  public ErraiJSONValue(Object obj) {
    this.obj = obj;
  }

  @Override
  public EJArray isArray() {
    if (obj instanceof List) {
      return new ErraiJSONArray((List) obj);
    }
    return null;
  }

  @Override
  public EJNumber isNumber() {
    if (obj instanceof Number) {
      return new ErraiJSONNumber((Number) obj);
    }
    return null;
  }

  @Override
  public EJNull isNull() {
    return obj == null ? NULL_INSTANCE : null;
  }

  @Override
  public EJObject isObject() {
    if (obj instanceof Map) {
      return new ErraiJSONObject((Map) obj);
    }
    return null;
  }

  @Override
  public EJBoolean isBoolean() {
    if (obj instanceof Boolean) {
      return ((Boolean) obj) ? ErraiJSONBoolean.TRUE : ErraiJSONBoolean.FALSE;
    }      
    return null;
  }

  @Override
  public EJString isString() {
    if (obj instanceof String) {
      return new ErraiJSONString((String) obj);
    }
    return null;
  }

  @Override
  public Object getRawValue() {
    return obj;
  }
}
