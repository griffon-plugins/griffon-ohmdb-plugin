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
import griffon.annotations.core.Nullable;
import griffon.plugins.ohmdb.DbCallback;
import griffon.plugins.ohmdb.DbFactory;
import griffon.plugins.ohmdb.DbHandler;
import griffon.plugins.ohmdb.DbStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static griffon.util.GriffonNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class DefaultDbHandler implements DbHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDbHandler.class);
    private static final String ERROR_DATASOURCE_NAME_BLANK = "Argument 'dataSourceName' must not be blank";
    private static final String ERROR_DB_NULL = "Argument 'db' must not be null";
    private static final String ERROR_CALLBACK_NULL = "Argument 'callback' must not be null";

    private final DbFactory dbFactory;
    private final DbStorage dbStorage;

    @Inject
    public DefaultDbHandler(@Nonnull DbFactory dbFactory, @Nonnull DbStorage dbStorage) {
        this.dbFactory = requireNonNull(dbFactory, "Argument 'dbFactory' must not be null");
        this.dbStorage = requireNonNull(dbStorage, "Argument 'dbStorage' must not be null");
    }

    @Nullable
    @Override
    public <R> R withOhmdb(@Nonnull DbCallback<R> callback) {
        return withOhmdb(DefaultDbFactory.KEY_DEFAULT, callback);
    }

    @Nullable
    @Override
    public <R> R withOhmdb(@Nonnull String dataSourceName, @Nonnull DbCallback<R> callback) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        Db db = getDb(dataSourceName);
        return doWithDb(dataSourceName, db, callback);
    }

    @Override
    public void closeOhmdb() {
        closeOhmdb(DefaultDbFactory.KEY_DEFAULT);
    }

    @Override
    public void closeOhmdb(@Nonnull String dataSourceName) {
        Db db = dbStorage.get(dataSourceName);
        if (db != null) {
            dbFactory.destroy(dataSourceName, db);
            dbStorage.remove(dataSourceName);
        }
    }

    @Nonnull
    private Db getDb(@Nonnull String dataSourceName) {
        Db db = dbStorage.get(dataSourceName);
        if (db == null) {
            db = dbFactory.create(dataSourceName);
            dbStorage.set(dataSourceName, db);
        }
        return db;
    }

    @Nullable
    @SuppressWarnings("ThrowFromFinallyBlock")
    static <R> R doWithDb(@Nonnull String dataSourceName, @Nonnull Db db, @Nonnull DbCallback<R> callback) {
        requireNonBlank(dataSourceName, ERROR_DATASOURCE_NAME_BLANK);
        requireNonNull(db, ERROR_DB_NULL);
        requireNonNull(callback, ERROR_CALLBACK_NULL);

        LOG.debug("Executing statements on db '{}'", dataSourceName);
        return callback.handle(dataSourceName, db);
    }
}
