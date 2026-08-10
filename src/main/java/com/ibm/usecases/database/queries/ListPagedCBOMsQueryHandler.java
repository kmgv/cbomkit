/*
 * CBOMkit
 * Copyright (C) 2024 PQCA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.usecases.database.queries;

import app.bootstrap.core.cqrs.IQueryBus;
import app.bootstrap.core.cqrs.QueryHandler;
import com.ibm.infrastructure.database.readmodels.CBOMReadModel;
import com.ibm.infrastructure.database.readmodels.ICBOMReadRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import java.util.Collection;

@Singleton
public final class ListPagedCBOMsQueryHandler
        extends QueryHandler<ListPagedCBOMsQuery, PagedCBOMs> {

    @Nonnull private final ICBOMReadRepository readRepository;

    void onStart(@Observes StartupEvent event) {
        this.queryBus.register(this, ListPagedCBOMsQuery.class);
    }

    public ListPagedCBOMsQueryHandler(
            @Nonnull IQueryBus queryBus, @Nonnull ICBOMReadRepository readRepository) {
        super(queryBus);
        this.readRepository = readRepository;
    }

    @Override
    public @Nonnull PagedCBOMs handle(@Nonnull ListPagedCBOMsQuery listPagedCBOMsQuery)
            throws Exception {
        final int page = listPagedCBOMsQuery.page();
        final int limit = listPagedCBOMsQuery.limit();

        final long totalElements = this.readRepository.countRecentRepositories();
        final int totalPages = (int) ((totalElements + limit - 1) / limit);

        // a page past the end is not an error, it just has nothing on it
        final Collection<CBOMReadModel> data =
                this.readRepository.getRecentPaged((page - 1) * limit, limit);

        return new PagedCBOMs(data, page, limit, totalPages, totalElements);
    }
}
