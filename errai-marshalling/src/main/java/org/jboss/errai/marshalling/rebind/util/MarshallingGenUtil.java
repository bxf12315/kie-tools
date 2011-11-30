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

package org.jboss.errai.marshalling.rebind.util;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingGenUtil {
  public static String getVarName(MetaClass clazz) {
    return clazz.isArray()
            ? getArrayVarName(clazz.getOuterComponentType().getCanonicalName())
            + "_D" + GenUtil.getArrayDimensions(clazz)
            : getVarName(clazz.getCanonicalName());
  }

  public static String getVarName(Class<?> clazz) {
    return getVarName(MetaClassFactory.get(clazz));
  }

  private static final String ARRAY_VAR_PREFIX = "arrayOf_";

  public static String getArrayVarName(String clazz) {
    char[] newName = new char[clazz.length() + ARRAY_VAR_PREFIX.length()];
    _replaceAllDotsWithUnderscores(ARRAY_VAR_PREFIX, newName, 0);
    _replaceAllDotsWithUnderscores(clazz, newName, ARRAY_VAR_PREFIX.length());
    return new String(newName);
  }

  public static String getVarName(String clazz) {
    char[] newName = new char[clazz.length()];
    _replaceAllDotsWithUnderscores(clazz, newName, 0);
    return new String(newName);
  }
  
  private static void _replaceAllDotsWithUnderscores(String sourceString, char[] destArray, int offset) {
    char c;
    for (int i = 0; i < sourceString.length(); i++) {
      if ((c = sourceString.charAt(i)) == '.') {
        destArray[i + offset] = '_';
      }
      else {
        destArray[i + offset] = c;
      }
    }
  }

}
