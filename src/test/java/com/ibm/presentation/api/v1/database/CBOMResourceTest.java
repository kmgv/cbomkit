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
package com.ibm.presentation.api.v1.database;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.annotation.Nonnull;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CBOMResourceTest {

    @Test
    @DisplayName(
            "Test that /api/v1/cbom/<projetcIdentifier> endpoint for an in valid pi returns 404")
    void testGetBOMInvalidPI() {
        given().when()
                .get("/api/v1/cbom/invalid")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Transactional
    @Test
    @DisplayName("Test that a CBOM can be stored, retrieved and deleted")
    void testCBOMStoreGetDelete() {
        String testIdentifier = "pkg:test/empty";

        String cbomString =
                "{"
                        + " \"bomFormat\": \"CycloneDX\","
                        + " \"specVersion\": \"1.6\","
                        + " \"serialNumber\": \"1\","
                        + " \"version\": 1 }";
        given().pathParam("projectIdentifier", testIdentifier)
                .when()
                .header("Content-type", "application/json")
                .body(cbomString)
                .when()
                .post("/api/v1/cbom/{projectIdentifier}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().pathParam("projectIdentifier", testIdentifier)
                .when()
                .get("/api/v1/cbom/{projectIdentifier}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "projectIdentifier", equalTo(testIdentifier),
                        "bom.serialNumber", equalTo("1"));

        given().pathParam("projectIdentifier", testIdentifier)
                .when()
                .delete("/api/v1/cbom/{projectIdentifier}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    @DisplayName(
            "Test that /api/v1/cbom/<projetcIdentifier> endpoint for an in valid pi returns 404")
    void testDeleteBOMInvalidPI() {
        given().when()
                .delete("/api/v1/cbom/invalid")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Test that /api/v1/cbom/lastn endpoint returns up to 5 CBOMS")
    void testGetLastCBOMs() {
        final int limit = 5;
        given().when()
                .get("/api/v1/cbom/last/" + limit)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("size()", lessThanOrEqualTo(limit));
    }

    @Test
    @DisplayName("Test that /api/v1/cbom/scans defaults to page 1 with a limit of 5")
    void testGetScansDefaults() {
        given().when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("page", equalTo(1))
                .body("limit", equalTo(5))
                .body("data.size()", lessThanOrEqualTo(5))
                .body("totalPages", greaterThanOrEqualTo(0))
                .body("totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Test that /api/v1/cbom/scans honours an explicit page and limit")
    void testGetScansExplicitPageAndLimit() {
        given().queryParam("page", 1)
                .queryParam("limit", 2)
                .when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("page", equalTo(1))
                .body("limit", equalTo(2))
                .body("data.size()", lessThanOrEqualTo(2));
    }

    @Test
    @DisplayName("Test that /api/v1/cbom/scans returns an empty page beyond the last one")
    void testGetScansPastLastPage() {
        given().queryParam("page", 99999)
                .when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("page", equalTo(99999))
                .body("data.size()", equalTo(0));
    }

    @Test
    @DisplayName("Test that /api/v1/cbom/scans clamps out-of-range page and limit values")
    void testGetScansClampsInvalidParams() {
        given().queryParam("page", 0)
                .queryParam("limit", -3)
                .when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("page", equalTo(1))
                .body("limit", equalTo(5));

        given().queryParam("limit", 100000)
                .when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("limit", equalTo(100));
    }

    @Transactional
    @Test
    @DisplayName("Test that /api/v1/cbom/scans pages do not overlap and match the total")
    void testGetScansPagesAreDisjoint() {
        final String[] identifiers = {"pkg:test/page-a", "pkg:test/page-b", "pkg:test/page-c"};
        final String cbomString =
                "{"
                        + " \"bomFormat\": \"CycloneDX\","
                        + " \"specVersion\": \"1.6\","
                        + " \"serialNumber\": \"1\","
                        + " \"version\": 1 }";
        for (String identifier : identifiers) {
            given().pathParam("projectIdentifier", identifier)
                    .when()
                    .header("Content-type", "application/json")
                    .body(cbomString)
                    .when()
                    .post("/api/v1/cbom/{projectIdentifier}")
                    .then()
                    .statusCode(Response.Status.OK.getStatusCode());
        }

        try {
            final List<String> firstPage = scansPage(1, 2);
            final List<String> secondPage = scansPage(2, 2);

            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).isNotEmpty();
            // a row must never appear on two pages
            assertThat(firstPage).doesNotContainAnyElementsOf(secondPage);
        } finally {
            for (String identifier : identifiers) {
                given().pathParam("projectIdentifier", identifier)
                        .when()
                        .delete("/api/v1/cbom/{projectIdentifier}")
                        .then()
                        .statusCode(Response.Status.OK.getStatusCode());
            }
        }
    }

    @Nonnull
    private static List<String> scansPage(int page, int limit) {
        return given().queryParam("page", page)
                .queryParam("limit", limit)
                .when()
                .get("/api/v1/cbom/scans")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getList("data.projectIdentifier", String.class);
    }
}
