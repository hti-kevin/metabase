import React from "react";

import { Route } from "metabase/hoc/Title";
import { IndexRedirect } from "react-router";
import { t } from "ttag";
import ToolsApp from "./containers/ToolsApp";
import ErrorOverview from "./containers/ErrorOverview";
import ErrorDetail from "./containers/ErrorDetail";
import { createAdminRouteGuard } from "metabase/admin/utils";

const getRoutes = store => (
  <Route
    path="tools"
    title={t`Tools`}
    component={createAdminRouteGuard("tools", ToolsApp)}
  >
    <IndexRedirect to="errors" />
    <Route
      path="errors"
      title={t`Erroring Questions`}
      component={ErrorOverview}
    />
    <Route path="errors/:cardId" component={ErrorDetail} />
  </Route>
);

export default getRoutes;
