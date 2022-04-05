import { hasPremiumFeature } from "metabase-enterprise/settings";
import { PLUGIN_FEATURE_LEVEL_PERMISSIONS } from "metabase/plugins";

import { getFeatureLevelDataPermissions } from "./permission-management";

import {
  canDownloadResults,
  getDownloadWidgetMessageOverride,
} from "./query-downloads";
import { NAV_PERMISSION_GUARD } from "metabase/nav/utils";

import {
  canAccessDataModel,
  canAccessDatabaseManagement,
  getDataColumns,
} from "./utils";

if (hasPremiumFeature("advanced_permissions")) {
  NAV_PERMISSION_GUARD["data-model"] = canAccessDataModel;
  NAV_PERMISSION_GUARD["databases"] = canAccessDatabaseManagement;

  PLUGIN_FEATURE_LEVEL_PERMISSIONS.getFeatureLevelDataPermissions = getFeatureLevelDataPermissions;
  PLUGIN_FEATURE_LEVEL_PERMISSIONS.getDataColumns = getDataColumns;
  PLUGIN_FEATURE_LEVEL_PERMISSIONS.getDownloadWidgetMessageOverride = getDownloadWidgetMessageOverride;
  PLUGIN_FEATURE_LEVEL_PERMISSIONS.canDownloadResults = canDownloadResults;
}
