import { getIn } from "icepick";
import { t } from "ttag";
import { GroupsPermissions } from "metabase-types/api";
import { UNABLE_TO_CHANGE_ADMIN_PERMISSIONS } from "metabase/admin/permissions/constants/messages";
import { EntityId, PermissionSubject } from "metabase/admin/permissions/types";
import { isRestrictivePermission } from "metabase/admin/permissions/utils/graph";

export const DETAILS_PERMISSION_REQUIRES_DATA_ACCESS = t`Manage database access requires full data access.`;

export const DETAILS_PERMISSION_OPTIONS = {
  no: {
    label: t`No`,
    value: "no",
    icon: "close",
    iconColor: "danger",
  },
  yes: {
    label: t`Yes`,
    value: "yes",
    icon: "check",
    iconColor: "success",
  },
};

const getDetailsPermission = (
  permissions: GroupsPermissions,
  groupId: number,
  databaseId: number,
) =>
  getIn(permissions, [groupId, databaseId, "details"]) ??
  DETAILS_PERMISSION_OPTIONS.no.value;

export const buildDetailsPermission = (
  entityId: EntityId,
  groupId: number,
  isAdmin: boolean,
  permissions: GroupsPermissions,
  dataAccessPermissionValue: string,
  permissionSubject: PermissionSubject,
) => {
  if (permissionSubject !== "schemas") {
    return null;
  }

  const value = isRestrictivePermission(dataAccessPermissionValue)
    ? DETAILS_PERMISSION_OPTIONS.no.value
    : getDetailsPermission(permissions, groupId, entityId.databaseId);

  const isDisabled =
    isAdmin || isRestrictivePermission(dataAccessPermissionValue);

  return {
    permission: "details",
    type: "details",
    isDisabled,
    disabledTooltip: isAdmin
      ? UNABLE_TO_CHANGE_ADMIN_PERMISSIONS
      : DETAILS_PERMISSION_REQUIRES_DATA_ACCESS,
    isHighlighted: isAdmin,
    value,
    options: [DETAILS_PERMISSION_OPTIONS.no, DETAILS_PERMISSION_OPTIONS.yes],
  };
};
