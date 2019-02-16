(ns clohue.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]))

(defn- discover-bridge []
  "Discover a Philips Hue Bridge on the local network"
  (first (json/read-json (:body (client/get "https://www.meethue.com/api/nupnp")))))

(def ^:private bridge-memo (memoize discover-bridge))

(def bridge (bridge-memo))

(defn create-user [{ip :internalipaddress} device-type]
  "Create a new user to use to authenticate against the Hue Bridge"
  (first (json/read-json (:body (client/post (str "http://" ip "/api")
                                             {:body (json/write-str {:devicetype device-type})
                                              :content-type :json
                                              :accept :json})))))

(defn toggle-light-state [{ip :internalipaddress} username light-id on?]
  (first (json/read-json (:body (client/put (str "http://" ip "/api/" username "/lights/" light-id "/state")
                                            {:body (json/write-str {:on on?})
                                             :content-type :json
                                             :accept :json})))))

(defn get-light-status [{ip :internalipaddress} username light-id]
  (json/read-json (:body (client/get (str "http://" ip "/api/" username "/lights/" light-id)))))


(toggle-light-state bridge user 1 true)

(get-light-status bridge user 1)





;; TESTING CODE:


;; (get-in x [:state :on])

;; [{:success {:username 7ATuEm2fRARZFaUPrQagul0OEMWCRjTDLfpR2b9-}}]

(defn store-username [username])

(defn- read-username [])

(def username (memoize read-username))

;; (def response (create-user bridge "clojure-app"))

;; (:username (:success response))
