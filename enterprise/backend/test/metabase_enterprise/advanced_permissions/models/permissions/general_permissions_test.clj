(ns metabase-enterprise.advanced-permissions.models.permissions.general-permissions-test
  (:require [clojure.test :refer :all]
            [metabase-enterprise.advanced-permissions.models.permissions.general-permissions :as g-perms]
            [metabase.models :refer [GeneralPermissionsRevision PermissionsGroup]]
            [metabase.models.permissions :as perms]
            [metabase.models.permissions-group :as group]
            [metabase.test :as mt]
            [toucan.db :as db]))

;; -------------------------------------------------- Fetch Graph ---------------------------------------------------

(deftest general-permissions-graph-test
  (mt/with-temp* [PermissionsGroup [{group-id :id}]]
    ;; clear the graph revisions
    (db/delete! GeneralPermissionsRevision)
    (testing "group should be in graph if one of general permission is enabled"
      (let [graph (g-perms/graph)]
        (is (= 0 (:revision graph)))
        (is (partial= {(:id (group/admin))
                       {:monitoring   :yes
                        :setting      :yes
                        :subscription :yes}
                       (:id (group/all-users))
                       {:monitoring   :no
                        :setting      :no
                        :subscription :yes}}
                      (:groups graph)))))

    (testing "group has no permissions will not be included in the graph"
      (is (not (contains? (-> (:groups (g-perms/graph)) keys set)
                          group-id))))))

;;; ------------------------------------------------- Update Graph --------------------------------------------------

(defmacro with-new-group-and-current-graph
  "Create a new group-id and bind it with the `current-graph`."
  [group-id-binding current-graph-binding & body]
  `(mt/with-temp* [PermissionsGroup [{group-id# :id}]]
     (mt/with-current-user (mt/user->id :crowberto)
       ((fn [~group-id-binding ~current-graph-binding] ~@body) group-id# (g-perms/graph)))))

(deftest general-permissions-update-graph!-test
  (testing "Grant successfully and increase revision"
    (with-new-group-and-current-graph group-id current-graph
      (let [new-graph     (assoc-in current-graph [:groups group-id] {:setting      :yes
                                                                      :monitoring   :no
                                                                      :subscription :no})
            _             (g-perms/update-graph! new-graph)
            updated-graph (g-perms/graph)]
        (is (partial= (:groups new-graph) (:groups updated-graph)))
        (is (= (inc (:revision current-graph)) (:revision updated-graph))))))

  (testing "Revoke successfully and increase revision"
    (with-new-group-and-current-graph group-id current-graph
      (let [new-graph     (assoc-in current-graph [:groups group-id :subscription] :no)
            _             (g-perms/update-graph! new-graph)
            updated-graph (g-perms/graph)]
        (is (= (dissoc (:groups new-graph) group-id) (:groups updated-graph)))
        (is (= (inc (:revision current-graph)) (:revision updated-graph))))))

  (testing "We can do a no-op and revision won't changes"
    (with-new-group-and-current-graph group-id current-graph
      (g-perms/update-graph! current-graph)
      (let [updated-graph (g-perms/graph)]
        (is (= (:groups updated-graph) (:groups updated-graph)))
        (is (= (:revision current-graph) (:revision updated-graph))))))

  (testing "Failed when try to update permission for admin group"
    (with-new-group-and-current-graph group-id current-graph
      (let [new-graph (assoc-in current-graph [:groups (:id (group/admin)) :subscription] :no)]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"You cannot create or revoke permissions for the 'Admin' group."
             (g-perms/update-graph! new-graph))))))

  (testing "Failed when revision is mismatched"
    (with-new-group-and-current-graph group-id current-graph
      (let [new-graph (assoc current-graph :revision (inc (:revision current-graph)))]
        (is (thrown-with-msg?
             clojure.lang.ExceptionInfo
             #"Looks like someone else edited the permissions and your data is out of date. Please fetch new data and try again."
             (g-perms/update-graph! new-graph))))))

  (testing "Able to grant for a group that was not in the old graph"
    (with-new-group-and-current-graph group-id current-graph
      ;; subscription is granted for new group by default, so revoke it
      (perms/revoke-general-permissions! group-id :subscription)
      ;; making sure the `group-id` is not in the current-graph
      (is (not (contains? (-> (:groups (g-perms/graph)) keys set)
                          group-id)))
      (let [current-graph         (g-perms/graph)
            new-graph             (assoc-in current-graph [:groups group-id] {:setting      :yes
                                                                              :subscription :yes
                                                                              :monitoring   :no})
            _                     (g-perms/update-graph! new-graph)
            updated-graph         (g-perms/graph)]
        (is (= (:groups new-graph) (:groups updated-graph)))
        (is (= (inc (:revision current-graph)) (:revision updated-graph)))))))
