(ns clohue.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]))

(defn- discover-bridge []
  "Discover a Philips Hue Bridge on the local network"
  (first (json/read-json (:body (client/get "https://www.meethue.com/api/nupnp")))))

(def ^:private bridge-memo (memoize discover-bridge))

(def bridge (bridge-memo))

(defn create-user [bridge device-type]
  "Create a new user to use to authenticate against the Hue Bridge"
  (let [{:keys [id internalipaddress]} bridge]
    (first (json/read-json (:body (client/post (str "http://" internalipaddress "/api")
                                               {:body (json/write-str {:devicetype device-type})
                                                :content-type :json
                                                :accept :json}))))))


(defn get-users [bridge username]
  (let [{:keys [id internalipaddress]} bridge]
    (:whitelist (json/read-json (:body (client/get (str "http://" internalipaddress "/api/" username "/config")))))))


(defn delete-user [bridge username username-to-delete]
  (let [{:keys [id internalipaddress]} bridge]
    (json/read-json (:body (client/delete (str "http://" internalipaddress "/api/" username "/config/whitelist/" username-to-delete))))))


(defn toggle-light-state [bridge username light-id on?]
  (let [{:keys [id internalipaddress]} bridge]
    (first (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                              {:body (json/write-str {:on on?})
                                               :content-type :json
                                               :accept :json}))))))


(defn get-light-status [{ip :internalipaddress} username light-id]
  (json/read-json (:body (client/get (str "http://" ip "/api/" username "/lights/" light-id)))))


(defn get-lights [bridge username]
  (let [{:keys [id internalipaddress]} bridge]
    (json/read-json (:body (client/get (str "http://" internalipaddress "/api/" username "/lights"))))))



(defn change-light-colour [bridge username light-id hue sat]
  (let [{:keys [id internalipaddress]} bridge]
    (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                       {:body (json/write-str {:hue hue
                                                               :sat sat})
                                        :content-type :json
                                        :accept :json})))))

(defn change-light-temperature [bridge username light-id temp]
  (let [{:keys [id internalipaddress]} bridge]
    (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                       {:body (json/write-str {:ct temp})
                                        :content-type :json
                                        :accept :json})))))


(defn send-alert [bridge username light-id]
  (let [{:keys [id internalipaddress]} bridge
        light-status (get-light-status bridge username light-id)]
    (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                       {:body (json/write-str {:on true
                                                               :hue 20
                                                               :sat 254
                                                               :alert :lselect})
                                        :content-type :json
                                        :accept :json})))
    (Thread/sleep 15000)
    (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                       {:body (json/write-str {:on (:on (:state light-status))
                                                               :hue (:hue (:state light-status))
                                                               :sat (:sat (:state light-status))})
                                        :content-type :json
                                        :accept :json})))))


(defn change-light-brightness [bridge username light-id bri]
  (let [{:keys [id internalipaddress]} bridge]
    (json/read-json (:body (client/put (str "http://" internalipaddress "/api/" username "/lights/" light-id "/state")
                                       {:body (json/write-str {:bri bri})
                                        :content-type :json
                                        :accept :json})))))



(change-light-temperature bridge user 1 350)

(change-light-brightness bridge user 1 254)

(send-alert bridge user 1)

(change-light-colour bridge user 1 5600 254)

(toggle-light-state bridge user 1 true)

(get-light-status bridge user 1)

(get-users bridge user)

(get-lights bridge user)



;; TESTING CODE:


;; (get-in x [:state :on])

;; [{:success {:username 7ATuEm2fRARZFaUPrQagul0OEMWCRjTDLfpR2b9-}}]

;; (defn store-username [username])

;; (defn- read-username [])

;; (def username (memoize read-username))

;; (def response (create-user bridge "clojure-app"))

;; (:username (:success response))
