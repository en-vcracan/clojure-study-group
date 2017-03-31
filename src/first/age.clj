(ns first.age
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(def number-pattern "(?:^|\\D)(\\d+)(?:\\s*)")

(defn- extract-number-before-word
  [string word]

  (if-let [[_ number] (re-find (re-pattern (str number-pattern word)) string)]
    (Integer/parseInt number)
    0))

(defn- remove-number-word
  [string word]

  (if-let [[match _] (re-find (re-pattern (str number-pattern word)) string)]
    (s/replace-first string match "")
    string))

(defn- not-blank?
  [s]
  (-> s (s/replace "," "") (s/replace "s" "") ((complement s/blank?))))

(defn- parse-age
  "Parses strings like 23 years, 6 months, 1 day.
   Returns the age as [years months days],
   or false if invalid."
  [age-string]
  (let [age-units ["year" "month" "day"]]
    (if (not-blank? (reduce remove-number-word age-string age-units))
      false
      (mapv #(extract-number-before-word age-string %) age-units))))

(defn valid-age? [s]
  (if (parse-age s) true false))

(defn age->birthdate [s]
  (when-let [[years months days] (parse-age s)]
    (f/unparse
      (f/formatters :date)
      (t/minus (t/now) (t/days days) (t/months months) (t/years years)))))
