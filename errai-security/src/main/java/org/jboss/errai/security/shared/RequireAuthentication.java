/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.security.shared;

import java.lang.annotation.*;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.api.annotations.Alias;
import org.jboss.errai.common.client.api.interceptor.InterceptedCall;
import org.jboss.errai.security.client.local.SecurityUserInterceptor;

import javax.interceptor.InterceptorBinding;

/**
 * Indicates that the service can only be accessed by logged-in users. No
 * additional security permissions are required.
 * <p>
 * This annotation can be on a method or an entire class
 */
@Alias
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
@InterceptorBinding
@InterceptedCall(SecurityUserInterceptor.class)
public @interface RequireAuthentication {
}
