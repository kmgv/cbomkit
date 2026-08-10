<template>
  <div>
    <cv-data-table-skeleton
      v-if="model.lastCboms.length === 0"
      :columns="columns.slice(0, 2)"
      :rows="model.scans.limit"
    >
    </cv-data-table-skeleton>
    <cv-data-table :columns="columns" v-else>
      <template v-slot:data>
        <cv-data-table-row v-for="scan in model.lastCboms" :key="scan.id">
          <cv-data-table-cell>
            <div class="container">
              {{ limitString(scan.projectIdentifier, 65) }}
              <cv-icon-button
                @click="openGitRepo(scan.gitUrl)"
                kind="ghost"
                size="sm"
                :icon="Launch16"
                label="Open in a new tab"
              />
            </div>
          </cv-data-table-cell>
          <cv-data-table-cell>{{ dateString(scan) }}</cv-data-table-cell>
          <cv-data-table-cell>
            <cv-button
              @click="showResultFromApi(scan)"
              style="float: right"
              kind="ghost"
              :icon="ArrowRight24"
              label="See cryptography components"
            >
              See {{ countComponents(scan) }}
              {{
                countComponents(scan) > 1
                  ? "cryptographic assets"
                  : "cryptographic asset"
              }}
            </cv-button>
          </cv-data-table-cell>
        </cv-data-table-row>
      </template>
    </cv-data-table>
    <!--
      Always rendered: its mount emits the initial `change`, which drives the first fetch.
      Deliberately no `:page` binding — the pagination owns the current page. Feeding it back
      a page that this component also updates from the response creates a loop: the prop change
      moves the inner select, which re-emits `change`, which fetches again.
    -->
    <cv-pagination
      :number-of-items="model.scans.totalElements"
      :actual-items-on-page="model.lastCboms.length"
      :page-sizes="pageSizes"
      @change="onPaginationChange"
    />
  </div>
</template>

<script>
import {model} from "@/model";
import {
  fetchCbomsPage,
  getCbomFromScan,
  getDetectionsFromCbom,
  limitString,
  openGitRepo,
  showResultFromApi,
} from "@/helpers";
import {ArrowRight24, Launch16} from "@carbon/icons-vue";

export default {
  name: "ListTable",
  data: function () {
    return {
      model,
      ArrowRight24,
      Launch16,
      columns: ["Most recent scans", "Date of scan", ""],
      pageSizes: [5, 10, 20],
      // last values actually requested, so the pagination's mount-time emit and any
      // repeated emit for the same page do not trigger a duplicate fetch
      requestedPage: null,
      requestedLimit: null,
    };
  },
  methods: {
    limitString,
    showResultFromApi,
    getDetectionsFromCbom,
    countComponents: function (scan) {
      return getDetectionsFromCbom(getCbomFromScan(scan)).length;
    },
    dateString: function (scan) {
      // Parse the input date string
      const date = new Date(scan.createdAt);

      // Check if the date is valid
      if (isNaN(date)) {
        return "-";
      }

      // Get day, month, and year components
      const day = date.getDate();
      const month = date.getMonth() + 1; // Months are 0-based, so add 1
      const year = date.getFullYear();

      // Create the formatted date string
      return `${day}/${month}/${year}`;
    },
    openGitRepo,
    onPaginationChange: function ({ page, length }) {
      if (page === this.requestedPage && length === this.requestedLimit) {
        return;
      }
      this.requestedPage = page;
      this.requestedLimit = length;
      fetchCbomsPage(page, length);
    },
    test: function () {

    },
  },
};
</script>

<style scoped>
.container {
  display: flex; /* Use flexbox to arrange items horizontally */
  align-items: center; /* Vertically align items to the center */
}
</style>

<!-- Prevents scrolling bug on the ListTable -->
<style>
.bx--data-table-content {
  overflow: hidden;
}
</style>
