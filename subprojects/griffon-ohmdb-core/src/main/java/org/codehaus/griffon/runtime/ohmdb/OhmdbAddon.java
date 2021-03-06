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
package org.codehaus.griffon.runtime.ohmdb;

import com.ohmdb.api.Db;
import griffon.annotations.core.Nonnull;
import griffon.core.GriffonApplication;
import griffon.core.env.Metadata;
import griffon.core.events.StartupStartEvent;
import griffon.plugins.monitor.MBeanManager;
import griffon.plugins.ohmdb.DbCallback;
import griffon.plugins.ohmdb.DbFactory;
import griffon.plugins.ohmdb.DbHandler;
import griffon.plugins.ohmdb.DbStorage;
import org.codehaus.griffon.runtime.core.addon.AbstractGriffonAddon;
import org.codehaus.griffon.runtime.ohmdb.monitor.DbStorageMonitor;

import javax.application.event.EventHandler;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;

/**
 * @author Andres Almiray
 */
@Named("ohmdb")
public class OhmdbAddon extends AbstractGriffonAddon {
    @Inject
    private DbHandler dbHandler;

    @Inject
    private DbFactory dbFactory;

    @Inject
    private DbStorage dbStorage;

    @Inject
    private MBeanManager mbeanManager;

    @Inject
    private Metadata metadata;

    @Override
    public void init(@Nonnull GriffonApplication application) {
        mbeanManager.registerMBean(new DbStorageMonitor(metadata, dbStorage));
    }

    @EventHandler
    public void handleStartupStartEvent(@Nonnull StartupStartEvent event) {
        for (String dataSourceName : dbFactory.getDataSourceNames()) {
            Map<String, Object> config = dbFactory.getConfigurationFor(dataSourceName);
            if (getConfigValueAsBoolean(config, "connect_on_startup", false)) {
                dbHandler.withOhmdb(new DbCallback<Object>() {
                    @Override
                    public Object handle(@Nonnull String dataSourceName, @Nonnull Db db) {
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public void onShutdown(@Nonnull GriffonApplication application) {
        for (String dataSourceName : dbFactory.getDataSourceNames()) {
            dbHandler.closeOhmdb(dataSourceName);
        }
    }
}
