/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.compile.ohmdb;

import org.codehaus.griffon.compile.core.BaseConstants;
import org.codehaus.griffon.compile.core.MethodDescriptor;

import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedMethod;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotatedType;
import static org.codehaus.griffon.compile.core.MethodDescriptor.annotations;
import static org.codehaus.griffon.compile.core.MethodDescriptor.args;
import static org.codehaus.griffon.compile.core.MethodDescriptor.method;
import static org.codehaus.griffon.compile.core.MethodDescriptor.type;
import static org.codehaus.griffon.compile.core.MethodDescriptor.typeParams;
import static org.codehaus.griffon.compile.core.MethodDescriptor.types;

/**
 * @author Andres Almiray
 */
public interface OhmdbAwareConstants extends BaseConstants {
    String DB_TYPE = "com.ohmdb.api.Db";
    String DB_HANDLER_TYPE = "griffon.plugins.ohmdb.DbHandler";
    String DB_CALLBACK_TYPE = "griffon.plugins.ohmdb.DbCallback";
    String DB_HANDLER_PROPERTY = "dbHandler";
    String DB_HANDLER_FIELD_NAME = "this$" + DB_HANDLER_PROPERTY;

    String METHOD_WITH_DB = "withOhmdb";
    String METHOD_CLOSE_DB = "closeOhmdb";
    String DB_NAME = "dataSourceName";
    String CALLBACK = "callback";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_CLOSE_DB
        ),
        method(
            type(VOID),
            METHOD_CLOSE_DB,
            args(annotatedType(types(type(ANNOTATION_NONNULL)), JAVA_LANG_STRING))
        ),

        annotatedMethod(
            annotations(ANNOTATION_NONNULL),
            type(R),
            typeParams(R),
            METHOD_WITH_DB,
            args(annotatedType(annotations(ANNOTATION_NONNULL), DB_CALLBACK_TYPE, R))
        ),
        annotatedMethod(
            types(type(ANNOTATION_NONNULL)),
            type(R),
            typeParams(R),
            METHOD_WITH_DB,
            args(
                annotatedType(annotations(ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(ANNOTATION_NONNULL), DB_CALLBACK_TYPE, R))
        )
    };
}
