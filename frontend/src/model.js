import { reactive } from "vue";

export const model = reactive({
  // STATE
  useDarkMode: false,
  showResults: false,
  showDebugging: false,
  cbom: null,
  dependencies: null,
  scanning: {
    isScanning: false,
    scanningStatus: null,
    scanningStatusMessage: null,
    scanningStatusError: null,
    liveDetections: [],
    socket: null,
    numberOfFiles: null,
    numberOfLines: null,
    startTime: null,
    scanDuration: null,
    totalDuration: null,
  },
  codeOrigin: {
    projectIdentifier: null,
    scanUrl: null,
    gitUrl: null,
    revision: null,
    subfolder: null,
    commitID: null,
    uploadedFileName: null,
  },
  credentials: {
    username: null,
    password: null,
    pat: null,
  },
  policyCheckResult: null,
  errors: [],
  lastCboms: [],
  // pagination state for the scans table, mirrors the /api/v1/cbom/scans envelope
  scans: {
    page: 1,
    limit: 5,
    totalPages: 0,
    totalElements: 0,
    loading: true,
  },

  // METHODS
  startAgain() {
    this.resetScanningInfo();
    this.resetCodeOriginInfo();
    this.policyCheckResult = null;
    model.showResults = false;
  },
  resetScanningInfo() {
    model.scanning.isScanning = false;
    model.scanning.scanningStatus = null;
    model.scanning.scanningStatusMessage = null;
    model.scanning.scanningStatusError = null;
    model.scanning.liveDetections = [];
    model.scanning.socket = null;
    model.scanning.numberOfFiles = null;
    model.scanning.numberOfFiles = null;
    model.scanning.startTime = null;
    model.scanning.scanDuration = null;
    model.scanning.totalDuration = null;
    model.codeOrigin.commitID = null;
    model.cbom = null;
    model.dependencies = null;
  },
  resetCodeOriginInfo() {
    model.codeOrigin.projectIdentifier = null
    model.codeOrigin.scanUrl = null;
    model.codeOrigin.gitUrl = null;
    model.codeOrigin.revision = null;
    model.codeOrigin.subfolder = null;
    model.codeOrigin.commitID = null;
    model.codeOrigin.uploadedFileName = null;
  },
  resetCredentials() {
    model.credentials.username = null;
    model.credentials.password = null;
    model.credentials.pat = null;
  },
  addError(errorStatus, message) {
    this.errors.push({status: errorStatus, message: message});
  },
  closeError(index) {
    this.errors.splice(index, 1);
  },
});

export const ErrorStatus = {
  NoConnection: "NoConnection",
  InvalidRepo: "InvalidRepo",
  ScanError: "ScanError",
  JsonParsing: "JsonParsing",
  InvalidCbom: "InvalidCbom",
  IgnoredComponent: "IgnoredComponent",
  MultiUpload: "MultiUpload",
  EmptyDatabase: "EmptyDatabase",
  FallBackLocalComplianceReport: "FallBackLocalComplianceReport",
  ScanWarning: "ScanWarning",
};
