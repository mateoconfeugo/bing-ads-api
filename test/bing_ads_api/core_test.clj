(ns bing-ads-api.core-test
  (:require [clojure.test :refer :all]
            [bing-ads-api.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(comment
(def credentials (clojure.edn/read-string (slurp (format "%s/%s" (System/getProperty "user.dir") "testinfo.edn"))))
(def foo (report-driver))
(pprint foo)


(def constructor-fn  (graph/eager-compile report-api-settings-graph))
(def settings (constructor-fn {}))
(def r (:report settings))
(def s (:service settings))
(def req-id @(future (submit-generate-report r s)))
(def report-status (future (poll-generate-report req-id s)))
(io/copy (:body (client/get (.getReportDownloadUrl @report-status) {:as :byte-array}))
         (File. (format "%s/%s.zip" (:download-path settings) req-id)))

(unzip (format "%s/%s.zip" (:download-path settings) req-id) (format "%s/%s" (:download-path settings) req-id) )
(def csv (slurp (format "%s/%s/%s.csv" (:download-path settings) req-id req-id )))
(def raw (parse-csv csv))


(def meta (filter #(= 1 (count %)) raw))
(def meta-transient (map (fn [x] (split (nth x 0) #":")) meta))
(def header-and-data (filter #(< 1 (count %)) raw))
(def header (first header-and-data))
(def data (rest header-and-data))
(def tuples (map #(zipmap header %) data))
(def output {:meta meta
             :records tuples})




          download-channel (chan)
          res (atom 1)]
      (go (while res
            (>! download-channel (:status (poll-generate-report report-request-id service)))))
      (go (while res
            (let [[val ch] (alts! [download-channel])]
              (match [(.getReportRequestStatus val)]
                     [true] (download-report (:report-url status-obj) (:download-path model))
                     ))



(def request (doto (SubmitGenerateReportRequest.) (.setReportRequest r)))
(pprint request)
(def response (.submitGenerateReport  s request))
(def two (.getReportRequestId response))
  )
