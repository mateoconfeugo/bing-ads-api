(ns bing-ads-api.core
  ^{:author "Matt Burns"
      :doc "Clojure version of the KeywordPerformance example provided with the api.  This version features the use
            of function graphs so that setup type code can be easily shared, merged and modified amongts"
  (:require [plumbing.core :refer [fnk]]
            [plumbing.graph :as graph :refer [eager-compile]]
            [clojure.edn]
            [clojure.java.io :as io]
            [clojure-csv.core :refer [parse-csv]]
            [clojure.string :refer [split]]
            [clj-http.client :as client]
            [me.raynes.fs.compression :refer [unzip]])
  (:import [bingads.reporting KeywordPerformanceReportFilter DeviceTypeReportFilterNull SubmitGenerateReportRequest
            PollGenerateReportRequest ReportingServiceLocator KeywordPerformanceReportColumn
            KeywordPerformanceReportSort SortOrder KeywordPerformanceReportRequest ReportAggregation ReportFormat
            AccountThroughAdGroupReportScope ReportTime ReportTimePeriod CampaignReportScope ApiFaultDetail]
           [bingads.reporting.adapi ]
           [java.io File FileOutputStream]))

(defn wait-for
 "Invoke predicate every interval (default 10) seconds until it returns true,
  or timeout (default 150) seconds have elapsed. E.g.:
      (wait-for #(< (rand) 0.2) :interval 1 :timeout 10)
  Returns nil if the timeout elapses before the predicate becomes true, otherwise
  the value of the predicate on its last evaluation."
 [predicate & {:keys [interval timeout]
               :or {interval 10
                    timeout 150}}]
 (let [end-time (+ (System/currentTimeMillis) (* timeout 1000))]
   (loop []
     (if-let [result (predicate)]
       result
       (do
         (Thread/sleep (* interval 1000))
         (if (< (System/currentTimeMillis) end-time)
           (recur)))))))


(defn submit-generate-report
  "Request the report and returns the ReportRequestId that can be used to check report
   status and then used to download the report."
  [report service]
  (let [request (doto (SubmitGenerateReportRequest.) (.setReportRequest report))
        response (.submitGenerateReport service request) ]
    (.getReportRequestId response)))

(defn poll-generate-report
  "Checks the status of a report request. Returns a data object that contains both report status and download URL."
  [report-request-id service]
  (let [request (doto (PollGenerateReportRequest. ) (.setReportRequestId report-request-id))
        response (.pollGenerateReport service request)]
    (.getReportRequestStatus response)))

(def report-api-settings-graph {:credentials (fnk [{account-settings "testinfo.edn"}] (clojure.edn/read-string (slurp (format "%s/%s" (System/getProperty "user.dir") account-settings))))
                                :download-path (fnk [{output-path nil}] (format "%s/%s" (System/getProperty "user.dir") (or output-path "report_downloads")))
                                :locator (fnk [] (ReportingServiceLocator.))
                                :api-namespace-uri (fnk [locator] (-> locator .getServiceName .getNamespaceURI))
                                :service (fnk [locator credentials api-namespace-uri]  (doto (.getBasicHttpBinding_IReportingService locator)
                                                                                 (.setHeader api-namespace-uri "DeveloperToken" (:token credentials))
                                                                                 (.setHeader api-namespace-uri "UserName" (:username credentials))
                                                                                 (.setHeader api-namespace-uri "Password" (:password credentials))))
                                :name (fnk [{report-title "keyword performance"}] report-title)
                                :aggregation-style (fnk [] (ReportAggregation/Daily))
                                :format (fnk [] (ReportFormat/Csv))
                                :scope  (fnk [credentials] (doto (AccountThroughAdGroupReportScope.)
                                                             (.setAccountIds nil)
                                                             (.setAdGroups  nil)
                                                             (.setCampaigns (into-array CampaignReportScope [(CampaignReportScope. (:account-id credentials) (:campaign-id credentials))]))
                                                             ))
                                :report-time (fnk [] (doto (ReportTime.)  (.setPredefinedTime (ReportTimePeriod/Yesterday))))
                                :devices (fnk []  ["Computer" "SmartPhone"])
                                :filter (fnk [devices] (doto (KeywordPerformanceReportFilter.) (.setDeviceType (into-array String devices))))
                                :columns (fnk [] (into-array  KeywordPerformanceReportColumn [KeywordPerformanceReportColumn/TimePeriod
                                                                                              KeywordPerformanceReportColumn/AccountId
                                                                                              KeywordPerformanceReportColumn/CampaignId
                                                                                              KeywordPerformanceReportColumn/Keyword
                                                                                              KeywordPerformanceReportColumn/KeywordId
                                                                                              KeywordPerformanceReportColumn/DeviceType
                                                                                              KeywordPerformanceReportColumn/BidMatchType
                                                                                              KeywordPerformanceReportColumn/Clicks
                                                                                              KeywordPerformanceReportColumn/Impressions
                                                                                              KeywordPerformanceReportColumn/Ctr
                                                                                              KeywordPerformanceReportColumn/AverageCpc
                                                                                              KeywordPerformanceReportColumn/Spend
                                                                                              KeywordPerformanceReportColumn/QualityScore]))
                                :sorting (fnk [] (doto (KeywordPerformanceReportSort.)
                                                   (.setSortColumn  KeywordPerformanceReportColumn/Clicks)
                                                   (.setSortOrder (SortOrder/Ascending))))
                                :report (fnk [format name aggregation-style scope  report-time filter sorting columns]
                                             (doto (KeywordPerformanceReportRequest.)
                                               (.setFormat format)
                                               (.setReportName name)
                                               (.setReturnOnlyCompleteData false)
                                               (.setAggregation aggregation-style)
                                               (.setScope scope)
                                               (.setTime report-time)
                                               (.setFilter filter)
                                               (.setColumns  columns)
                                               (.setSort  (into-array KeywordPerformanceReportSort [sorting]))))})

(defn report-driver
    "Generate a keyword performance report this encapusatures the asynchronous nature of call and all
     mutable state manipulation necessary to set up getting a keyword report"
    []
    (let [api-report-fixture-fn (graph/eager-compile report-api-settings-graph)
          model (api-report-fixture-fn {})
          report (:report model)
          service (:service model)
          report-request-id (future (submit-generate-report report service))
          report-status (future (poll-generate-report @report-request-id service))
          _ (io/copy (:body (client/get (.getReportDownloadUrl @report-status) {:as :byte-array}))
                     (File. (format "%s/%s.zip" (:download-path settings) report-request-id)))
          _ (unzip (format "%s/%s.zip" (:download-path settings) req-id) (format "%s/%s" (:download-path settings) report-request-id) )
          csv (slurp (format "%s/%s/%s.csv" (:download-path settings) req-id req-id ))
          raw-data (parse-csv csv)
          meta (filter #(= 1 (count %)) raw)
          header-and-data (filter #(< 1 (count %)) raw-data)
          header (first header-and-data)
          data (rest header-and-data)
          tuples (map #(zipmap header %) data)]
      {:meta-data meta :records tuples}))
