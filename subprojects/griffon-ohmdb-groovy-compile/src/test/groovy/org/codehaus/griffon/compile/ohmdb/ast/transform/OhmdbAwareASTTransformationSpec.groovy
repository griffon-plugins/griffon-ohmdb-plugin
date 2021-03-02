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
package org.codehaus.griffon.compile.ohmdb.ast.transform

import griffon.plugins.ohmdb.DbHandler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * @author Andres Almiray
 */
class OhmdbAwareASTTransformationSpec extends Specification {
    def 'OhmdbAwareASTTransformation is applied to a bean via @OhmdbAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''
        @griffon.transform.ohmdb.OhmdbAware
        class Bean { }
        new Bean()
        ''')

        then:
        bean instanceof DbHandler
        DbHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }

    def 'OhmdbAwareASTTransformation is not applied to a DbHandler subclass via @OhmdbAware'() {
        given:
        GroovyShell shell = new GroovyShell()

        when:
        def bean = shell.evaluate('''import griffon.plugins.ohmdb.DbCallback
        import griffon.plugins.ohmdb.DbHandler
        import griffon.annotations.core.Nonnull
        @griffon.transform.ohmdb.OhmdbAware
        class DbHandlerBean implements DbHandler {
            @Override
             <R> R withOhmdb(@Nonnull DbCallback<R> callback)  {
                return null
            }
            @Override
             <R> R withOhmdb(@Nonnull String dataSourceName, @Nonnull DbCallback<R> callback) {
                 return null
            }
            @Override
            void closeOhmdb(){}
            @Override
            void closeOhmdb(@Nonnull String dataSourceName){}
        }
        new DbHandlerBean()
        ''')

        then:
        bean instanceof DbHandler
        DbHandler.methods.every { Method target ->
            bean.class.declaredMethods.find { Method candidate ->
                candidate.name == target.name &&
                    candidate.returnType == target.returnType &&
                    candidate.parameterTypes == target.parameterTypes &&
                    candidate.exceptionTypes == target.exceptionTypes
            }
        }
    }
}
