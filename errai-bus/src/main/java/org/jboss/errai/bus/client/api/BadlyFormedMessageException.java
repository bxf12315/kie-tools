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

package org.jboss.errai.bus.client.api;

/**
 * An exception thrown when a message has not been properly formed and is therefore, not transmittable.
 */
public class BadlyFormedMessageException extends RuntimeException {
  public BadlyFormedMessageException(String message) {
    super(message);
  }

  public BadlyFormedMessageException(String message, Throwable cause) {
    super(message, cause);
  }
}
