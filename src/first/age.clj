(ns first.age
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(defn- extract-number-before-word
  "Returns [number string-without-that-number] or [0 unaltered-string]"
  [word string]

  (if-let [[match number] (re-find (re-pattern (str "(?:^|\\D)(\\d+)(?:\\s*)" word)) string)]
    [(Integer/parseInt number) (s/replace-first string match "")]
    [0 string]))

(defn- not-blank?
  [s]
  (-> s (s/replace "," "") (s/replace "s" "") ((complement s/blank?))))

(defn- parse-age
  "Parses strings like 23 years, 6 months, 1 day.
   Returns the age as [years months days],
   or false if invalid."
  [age-string]
  (let [[age-int leftover-string] (reduce
                                    (fn [[vnums string] word]
                                      (let [[num new-string] (extract-number-before-word word string)]
                                        [(conj vnums num) new-string]))
                                    [[] age-string]
                                    ["year" "month" "day"])]

    (if (not-blank? leftover-string) false age-int)))

(defn valid-age? [s]
  (if (parse-age s) true false))

(defn age->birthdate [s]
  (when-let [[years months days] (parse-age s)]
    (f/unparse
      (f/formatters :date)
      (t/minus (t/now) (t/days days) (t/months months) (t/years years)))))