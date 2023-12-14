(ns mlinks.dsl)

(defn make-link [id author long short]
  {:id id
   :ll long
   :sl short
   :author author})
