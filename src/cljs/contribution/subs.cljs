(ns contribution.subs
  (:require
    [cljs-web3.core :as web3]
    [contribution.constants :as constants]
    [goog.string :as gstring]
    [goog.string.format]
    [medley.core :as medley]
    [re-frame.core :refer [reg-sub]]
    [cljs-time.core :as t]))

(reg-sub
  :db/now
  (fn [db]
    (:now db)))

(reg-sub
  :contribution/dnt-balance
  :<- [:district0x/balances]
  :<- [:district0x/smart-contracts]
  (fn [[balances contracts]]
    (get-in balances [(:address (:contribution contracts)) :dnt])))

(reg-sub
  :confirmed-not-us-citizen?
  (fn [db]
    (:confirmed-not-us-citizen? db)))

(reg-sub
  :confirmed-terms?
  (fn [db]
    (:confirmed-terms? db)))

(reg-sub
  :confirmations-submitted?
  (fn [db]
    (:confirmations-submitted? db)))

(reg-sub
  :contribution/contrib-period
  (fn [db]
    (:contribution/contrib-period db)))

(reg-sub
  :disallowed-country?
  (fn [db]
    (:disallowed-country? db)))

(reg-sub
  :contribution/current-contrib-period-status
  :<- [:contribution/contrib-period]
  :<- [:db/now]
  (fn [[{:keys [:contrib-period/start-time :contrib-period/end-time]} now]]
    (cond
      (or (not start-time)
          (not end-time)
          (not (t/before? start-time now)))
      :contrib-period-status/not-started

      (and (t/after? now start-time)
           (t/before? now end-time))
      :contrib-period-status/running

      (t/after? now end-time)
      :contrib-period-status/ended)))

(reg-sub
  :contribution/configuration
  (fn [db]
    (merge
      (select-keys db [:contribution/stopped? :contribution/founder1 :contribution/founder2
                       :contribution/early-sponsor :contribution/wallet :contribution/advisers
                       :contribution/max-gas-price])
      {:contribution-address (get-in db [:smart-contracts :contribution :address])
       :dnt-token-address (get-in db [:smart-contracts :dnt-token :address])})))

(reg-sub
  :contribution-contract-address
  :<- [:district0x/smart-contracts]
  (fn [contracts]
    (get-in contracts [:contribution :address])))

(reg-sub
  :db/active-address-founder?
  :<- [:district0x/active-address]
  :<- [:contribution/configuration]
  (fn [[active-address {:keys [:contribution/founder1 :contribution/founder2]}]]
    (contains? #{founder1 founder2} active-address)))

(reg-sub
  :form.contribution/enable-contrib-period
  (fn [db _]
    (:default (:form.contribution/enable-contrib-period db))))

(reg-sub
  :form.contribution/contribute
  (fn [db _]
    (:default (:form.contribution/contribute db))))

