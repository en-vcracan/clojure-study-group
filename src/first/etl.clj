(ns first.etl
  (:require [clojure.spec :as s]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [first.age :refer [valid-age? age->birthdate]]))

;; Define the spec
(s/def ::not-blank? (s/and string? (complement clojure.string/blank?)))
(s/def ::age valid-age?)
(s/def ::data-row (s/tuple
                    ::not-blank?
                    ::not-blank?
                    ::age))

;; Helper conform function
(defn my-conform
  "Wrapper around clojure.spec/conform.
   Returns:
    {:error error-str} or
    [firstname lastname birthdate]"
  [spec x]
  (let [c (s/conform spec x)]
    (if (= c :clojure.spec/invalid)
      {:error (apply str (s/explain-str spec x))}
      (update c 2 age->birthdate))))

;; File names
(def filename-in "Book1.csv")
(def filename-out "Processed.csv")
(def filename-error "Errors.csv")

;; ETL
(with-open [
            r (io/reader filename-in)
            w (io/writer filename-out :append false)
            w-error (io/writer filename-error :append false)]
  (doseq

    [[i row] (map
               list
               (range)
               (map #(my-conform ::data-row %) (csv/read-csv r)))]

    (let [error (:error row)]
      (if error
        (csv/write-csv w-error [[i error]])
        (csv/write-csv w [row])))))