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

package org.jboss.errai.marshalling.client.marshallers;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
@ImplementationAliases({AbstractList.class, ArrayList.class, LinkedList.class})
public class ListMarshaller implements Marshaller<JSONValue, List> {
  @Override
  public Class<List> getTypeHandled() {
    return List.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public List demarshall(JSONValue o, MarshallingSession ctx) {
    if (o == null) return null;

    JSONArray jsonArray = o.isArray();
    if (jsonArray == null) return null;

    ArrayList<Object> list = new ArrayList<Object>();
    Marshaller<Object, Object> cachedMarshaller = null;

    for (int i = 0; i < jsonArray.size(); i++) {
      JSONValue elem = jsonArray.get(i);
      if (cachedMarshaller == null || !cachedMarshaller.handles(elem)) {
        cachedMarshaller = ctx.getMarshallerForType(ctx.determineTypeFor(null, elem));
      }

      list.add(cachedMarshaller.demarshall(elem, ctx));
    }

    return list;
  }

  @Override
  public String marshall(List o, MarshallingSession ctx) {
    if (o == null) {
      return "null";
    }

    StringBuilder buf = new StringBuilder("[");
    Marshaller<Object, Object> cachedMarshaller = null;
    Object elem;
    for (int i = 0; i < o.size(); i++) {
      if (i > 0) {
        buf.append(",");
      }
      elem = o.get(i);

      if (cachedMarshaller == null) {
        if (elem instanceof Number || elem instanceof Boolean || elem instanceof Character) {
          cachedMarshaller = MarshallUtil.getQualifiedNumberMarshaller(elem);
        }
        else {
          cachedMarshaller = ctx.getMarshallerForType(elem.getClass().getName());
        }
      }

      buf.append(cachedMarshaller.marshall(elem, ctx));
    }

    return buf.append("]").toString();
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isArray() != null;
  }
}
