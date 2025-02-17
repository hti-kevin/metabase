import { User } from "metabase-types/api";

export interface UserWithFeaturePermissions extends User {
  permissions?: {
    can_access_data_model: boolean;
    can_access_database_management: boolean;
  };
}
