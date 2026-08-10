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

import app.bootstrap.core.cqrs.IQuery;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;

/**
 * Requests a single page of stored CBOMs. {@code page} is 1-based. Out-of-range values are clamped
 * rather than rejected, so every request yields a page.
 */
public record ListPagedCBOMsQuery(@Nullable Integer page, @Nullable Integer limit)
        implements IQuery<PagedCBOMs> {

    public static final int DEFAULT_LIMIT = 5;
    public static final int MAX_LIMIT = 100;

    @Override
    @Nonnull
    public Integer page() {
        return Optional.ofNullable(page).filter(p -> p >= 1).orElse(1);
    }

    @Override
    @Nonnull
    public Integer limit() {
        return Optional.ofNullable(limit)
                .filter(l -> l >= 1)
                .map(l -> Math.min(l, MAX_LIMIT))
                .orElse(DEFAULT_LIMIT);
    }
}
