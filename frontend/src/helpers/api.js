import { model, ErrorStatus } from "@/model.js";
import { API_SCANS_URL, API_CHECK_POLICY, API_CHECK_POLICY_NAME } from "@/app.config";
import { checkValidComplianceResults, createLocalComplianceReport, isViewerOnly } from "@/helpers.js";


// Identifies the most recent page request. Responses can arrive out of order when the user
// clicks through pages quickly, so anything but the newest response is discarded.
let latestScansRequest = 0;

export function fetchCbomsPage(page, limit) {
  let apiUrl = `${API_SCANS_URL}?page=${page}&limit=${limit}`;
  const requestId = ++latestScansRequest;
  model.scans.loading = true;
  return fetchDataFromApi(apiUrl, null)
    .then((jsonData) => {
      if (requestId !== latestScansRequest) {
        return;
      }
      model.lastCboms = jsonData.data;
      // the server clamps out-of-range values, so echo back what it actually used
      model.scans.page = jsonData.page;
      model.scans.limit = jsonData.limit;
      model.scans.totalPages = jsonData.totalPages;
      model.scans.totalElements = jsonData.totalElements;
      if (jsonData.totalElements === 0) {
        model.addError(ErrorStatus.EmptyDatabase);
      }
    })
    .catch((error) => {
      console.error("Error:", error.message);
      if (requestId === latestScansRequest) {
        model.addError(ErrorStatus.NoConnection);
      }
    })
    .finally(() => {
      if (requestId === latestScansRequest) {
        model.scans.loading = false;
      }
    });
}

function getLocalComplianceReport(cbom) {
  let jsonDataLocal = createLocalComplianceReport(cbom);
  if (checkValidComplianceResults(jsonDataLocal)) {
    model.policyCheckResult = jsonDataLocal;
  } else {
    model.policyCheckResult = { error: true };
  }
}

function getRemoteComplianceReport(cbom, policyIdentifier = API_CHECK_POLICY_NAME) {
  const apiUrl = `${API_CHECK_POLICY}?policyIdentifier=${policyIdentifier}`;

  // Create the request options
  const requestOptions = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(cbom),
  };

  // Make the POST request
  fetchDataFromApi(apiUrl, requestOptions)
    .then((jsonData) => {
      if (checkValidComplianceResults(jsonData)) {
        model.policyCheckResult = jsonData;
      } else {
        // An error occured in the backend compliance service, we use the local compliance service instead
        console.warn("Using the local compliance report instead of the remote one")
        model.addError(ErrorStatus.FallBackLocalComplianceReport)
        getLocalComplianceReport(cbom)
      }
    })
    .catch(() => {
      console.warn("Using the local compliance report instead of the remote one")
      model.addError(ErrorStatus.FallBackLocalComplianceReport)
      getLocalComplianceReport(cbom)
    });
}

export function getComplianceReport(cbom, policyIdentifier = API_CHECK_POLICY_NAME) {
  if (isViewerOnly()) {
    getLocalComplianceReport(cbom)
  } else {
    getRemoteComplianceReport(cbom, policyIdentifier)
  }
}

function fetchDataFromApi(apiUrl, requestOptions) {
  let fetchPromise;
  if (requestOptions === null) {
    fetchPromise = fetch(apiUrl);
  } else {
    fetchPromise = fetch(apiUrl, requestOptions);
  }
  return fetchPromise
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      return response.json();
    })
    .then((data) => {
      // console.log(`Received data from ${apiUrl}:`, data)
      return data;
    })
    .catch((error) => {
      // Handle errors during the fetch
      console.error("Error during request:", error.message);
      throw error;
    });
}
