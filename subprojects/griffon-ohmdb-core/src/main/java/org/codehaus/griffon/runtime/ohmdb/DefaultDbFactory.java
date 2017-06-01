/*
 * Copyright 2014-2017 the original author or authors.
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

import com.ohmdb.api.Db;
import com.ohmdb.api.Ohm;
import griffon.core.Configuration;
import griffon.core.GriffonApplication;
import griffon.core.injection.Injector;
import griffon.exceptions.GriffonException;
import griffon.plugins.ohmdb.DbFactory;
import griffon.plugins.ohmdb.OhmdbBootstrap;
import org.codehaus.griffon.runtime.core.storage.AbstractObjectFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static griffon.util.ConfigUtils.getConfigValueAsBoolean;
import static griffon.util.ConfigUtils.getConfigValueAsString;
import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultDbFactory extends AbstractObjectFactory<Db> implements DbFactory {
    private static final String ERROR_DATASOURCE_BLANK = "Argument 'dataSourceName' must not be blank";

    private final Set<String> dataSourceNames = new LinkedHashSet<>();

    @Inject
    private Injector injector;

    @Inject
    public DefaultDbFactory(@Nonnull @Named("ohmdb") Configuration configuration, @Nonnull GriffonApplication application) {
        super(configuration, application);
        dataSourceNames.add(KEY_DEFAULT);

        if (configuration.containsKey(getPluralKey())) {
            Map<String, Object> ohmdbs = (Map<String, Object>) configuration.get(getPluralKey());
            dataSourceNames.addAll(ohmdbs.keySet());
        }
    }

    @Nonnull
    @Override
    public Set<String> getDataSourceNames() {
        return dataSourceNames;
    }

    @Nonnull
    @Override
    public Map<String, Object> getConfigurationFor(@Nonnull String dataSourceName) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_BLANK);
        return narrowConfig(dataSourceName);
    }

    @Nonnull
    @Override
    protected String getSingleKey() {
        return "dataSource";
    }

    @Nonnull
    @Override
    protected String getPluralKey() {
        return "dataSources";
    }

    @Nonnull
    @Override
    public Db create(@Nonnull String name) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        Map<String, Object> config = narrowConfig(name);

        if (config.isEmpty()) {
            throw new IllegalArgumentException("DataSource '" + config + "' is not configured.");
        }

        event("OhmdbConnectStart", asList(name, config));

        Db db = createDb(config, name);

        for (Object o : injector.getInstances(OhmdbBootstrap.class)) {
            ((OhmdbBootstrap) o).init(name, db);
        }

        event("OhmdbConnectEnd", asList(name, config, db));

        return db;
    }

    @Override
    public void destroy(@Nonnull String name, @Nonnull Db instance) {
        requireNonBlank(name, ERROR_DATASOURCE_BLANK);
        requireNonNull(instance, "Argument 'instance' must not be null");
        Map<String, Object> config = narrowConfig(name);

        if (config.isEmpty()) {
            throw new IllegalArgumentException("DataSource '" + config + "' is not configured.");
        }

        event("OhmdbDisconnectStart", asList(name, config, instance));

        for (Object o : injector.getInstances(OhmdbBootstrap.class)) {
            ((OhmdbBootstrap) o).destroy(name, instance);
        }

        destroyDb(config, instance);

        event("OhmdbDisconnectEnd", asList(name, config));
    }

    @Nonnull
    private Db createDb(@Nonnull Map<String, Object> config, @Nonnull String name) {
        File dbfile = resolveDBFile(config);
        Db db = null;
        try {
            db = Ohm.db(dbfile.getCanonicalPath());
            event("OhmdbConfigurationSetup", asList(name, config, db));
            return db;
        } catch (IOException ioe) {
            throw new GriffonException(ioe);
        }
    }

    private void destroyDb(@Nonnull Map<String, Object> config, @Nonnull Db db) {
        db.shutdown();

        boolean delete = getConfigValueAsBoolean(config, "delete", false);

        if (delete) {
            File dbfile = resolveDBFile(config);
            dbfile.deleteOnExit();
        }
    }

    @Nonnull
    private File resolveDBFile(@Nonnull Map<String, Object> config) {
        String dbfileName = getConfigValueAsString(config, "name", "db.bin");
        File dbfile = new File(dbfileName);
        if (!dbfile.isAbsolute()) {
            dbfile = new File(System.getProperty("user.dir"), dbfile.getPath());
        }
        dbfile.getParentFile().mkdirs();
        return dbfile;
    }
}
