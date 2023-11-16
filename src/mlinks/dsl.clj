(ns mlinks.dsl)

(defn make-link [id author long short]
  {:id id
   :long long
   :short short
   :author author})
