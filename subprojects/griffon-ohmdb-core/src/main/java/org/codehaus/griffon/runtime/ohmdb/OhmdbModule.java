/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.griffon.runtime.ohmdb;

import griffon.core.Configuration;
import griffon.core.addon.GriffonAddon;
import griffon.core.injection.Module;
import griffon.plugins.ohmdb.DbFactory;
import griffon.plugins.ohmdb.DbHandler;
import griffon.plugins.ohmdb.DbStorage;
import org.codehaus.griffon.runtime.core.injection.AbstractModule;
import org.codehaus.griffon.runtime.util.ResourceBundleProvider;
import org.kordamp.jipsy.ServiceProviderFor;

import javax.inject.Named;
import java.util.ResourceBundle;

import static griffon.util.AnnotationUtils.named;

/**
 * @author Andres Almiray
 */
@Named("ohmdb")
@ServiceProviderFor(Module.class)
public class OhmdbModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        // tag::bindings[]
        bind(ResourceBundle.class)
            .withClassifier(named("ohmdb"))
            .toProvider(new ResourceBundleProvider("Ohmdb"))
            .asSingleton();

        bind(Configuration.class)
            .withClassifier(named("ohmdb"))
            .to(DefaultOhmdbConfiguration.class)
            .asSingleton();

        bind(DbStorage.class)
            .to(DefaultDbStorage.class)
            .asSingleton();

        bind(DbFactory.class)
            .to(DefaultDbFactory.class)
            .asSingleton();

        bind(DbHandler.class)
            .to(DefaultDbHandler.class)
            .asSingleton();

        bind(GriffonAddon.class)
            .to(OhmdbAddon.class)
            .asSingleton();
        // end::bindings[]
    }
}
