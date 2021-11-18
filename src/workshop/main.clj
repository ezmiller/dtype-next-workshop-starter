(ns workshop.main
  (:require [criterium.core :refer [quick-bench]]
            [clojure.string :as s]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 1 Intro
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Prelude slides: https://tinyurl.com/usdf5twj

;; The power of the library in a nutshell.
;; We might ask: why do we need dtype-next?
;; Why not just use Clojure data structures?

(require '[tech.v3.datatype :as dtype]
         '[tech.v3.datatype.functional :as fun])

(quick-bench (reduce + (range 1000000)))

(quick-bench (fun/sum (dtype/->reader (range 1000000) :int64)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 2 The Buffer in dtype-Next
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Buffers are
;; - random-access
;; - countable
;; - typed - all elements of same type
;; - **lazy & non-caching** 

;; Let's create one. A few ways to do this. Here's one.
;; We will see some other soon.
(def a-buffer (dtype/as-buffer [1 2 3 4]))

a-buffer

;; Random access
(nth a-buffer 2)

;; Countable
(count a-buffer)

;; Interesting that it prints as a regular persistent vector.
;; What if check its class?
(class a-buffer)

;; Wow! That's unusual. What's going on here?  

;; How can we know what type of thing we are working with then?
(dtype/datatype [1 2 3])

(dtype/datatype a-buffer)

(dtype/elemwise-datatype a-buffer)

;; Hmmm why object? What's happening here? 

(dtype/elemwise-datatype (dtype/as-buffer (int-array [1 2 3])))
(dtype/elemwise-datatype (dtype/as-buffer (vector-of :int 1 2 3)))

;; Better yet some pathways in dtype-next for making things with specific types.

(def an-int-buffer (dtype/->reader [1 2 3] :int32))

an-int-buffer

(dtype/elemwise-datatype an-int-buffer)

(tech.v3.datatype.casting/all-datatypes)

;; What is this thing the reader?

;; In fact there are two kinds of buffers in dtype-next
;; - A reader buffer - we can read values
;; - A writer buffer - we can write (i.e. mutate values)

;; You cannot write to a reader

(dtype/set-value! (dtype/->reader [1 2 3]) 1 0)

(dtype/set-value! (dtype/->writer (int-array [1 2 3])) 1 0)

;; Lazy & Non-caching

(def big-rdr (dtype/make-reader :int64 1000000 (* idx (rand-int 100))))

(take 5 big-rdr)

(dtype/sub-buffer big-rdr 999000 5)

(def realized-br (dtype/make-container big-rdr)) ;; also: `dtype/clone`

(take 5 realized-br)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3 Working with Buffers 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; So we now know what buffers are. How do we interact with them?
;; We can often use clojure functions like map & reduce.
(reduce + (dtype/->reader (range 1000000) :int32))

(keep #(when (odd? %) %) (dtype/->reader (range 10) :int32))

(count (dtype/->reader (range 1000000) :int32))

; But we can't expect the operations to be particular efficient
; and we leave dtype-next world when we use them.
(class
 (keep #(when (odd? %) %) (dtype/->reader (range 10) :int32)))


;; Most of the time we want to use dtype-next's "functional"
;; namespace: tech.v3.datatype.functional. Usually aliased as `fun`.
;; This namespace provides a number of arithemetic operations that
;; will return a buffer!
;;
;; https://cnuernber.github.io/dtype-next/tech.v3.datatype.functional.html

;; With this namespace we can perform basic arithemetic on buffers.
(def a (dtype/->reader [20 30 40 50] :int32))

(def b (dtype/->reader (range 4) :int32))

(fun/- a b)
(fun/- a 2)
(fun/- 3 2)

(dtype/datatype (fun/- a b))

(fun/pow a 2)

(fun/log a)

;; Upcasting - dtype-next will upcast 
(def a-ints (dtype/->reader (range 10) :int32))

(def b-floats (dtype/make-reader :float32 10 (rand 10)))

(dtype/elemwise-datatype a-ints)

(dtype/elemwise-datatype b-floats)

;; What do we expect here?
(fun/* a-ints b-floats)

(dtype/elemwise-datatype (fun/* a-ints b-floats))

;; Mapping
(dtype/emap (constantly 0) :int64 (range 10))

(dtype/emap (fn [x] (+ x (/ x 10))) :float64 (range 10))

;; Subsetting
(dtype/sub-buffer (dtype/->reader (range 10) :int64) 5 3)

;; Filtering in index space

; Let's say we want to grab only odd values?
(fun/odd? (dtype/->reader (range 10) :int32))

(require '[tech.v3.datatype.argops :as dtype-argops])

(dtype-argops/argfilter odd? (dtype/->reader (range 100)))

(let [rdr (dtype/->reader (range 100) :int32)
      indices (dtype-argops/argfilter odd? rdr)]
  (dtype/indexed-buffer indices rdr))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3 Small exercise to put this together
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Goal: Create a normalized form of iris's sepal length whose values
;; range exactly between 0 and 1 so that the minimum has value 0 and
;; maximum has value 1.

(def data-url
  "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data" )

(require '[clojure.data.csv :as csv])

(def raw-data (-> data-url slurp csv/read-csv))

(take 2 raw-data)

(def data (->> (vec raw-data)
               (dtype/emap first :object)
               (dtype/emap #(Float/parseFloat %) :float32)))

(take 5 data)

(let [smin (fun/reduce-min data)
      smax (fun/reduce-max data)]
  (fun// (fun/- data smin) (- smax smin)))

;; ☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠☠

(def bad-data (->> (vec raw-data)
               (dtype/emap first :object)
               #_(dtype/emap #(Float/parseFloat %) :float32)))

;; We see we have one empty string
(dtype-argops/argfilter #(= % "") bad-data)

;; So let's clean the data using the index-space operation
;; from above (e.g. `argfilter`)
(defn clean [rdr]
  (let [indices (dtype-argops/argfilter (complement #(= % "")) rdr)]
    (dtype/indexed-buffer indices rdr)))

(def good-data (->> (vec raw-data)
                    (dtype/emap first :object)
                    (clean)
                    (dtype/emap #(Float/parseFloat %) :float32)))

(dtype-argops/argfilter #(= % "") good-data)

(def normalized-data
  (let [smin (fun/reduce-min good-data)
        smax (fun/reduce-max good-data)]
    (fun// (fun/- good-data smin) (- smax smin))))

normalized-data

;; Validate result
(fun/sum (dtype/concat-buffers
          :int32
          [(dtype/->int-array (fun/> 1 good-data))
           (dtype/->int-array (fun/< good-data 0))]))



;;
;;    Integration with tech.ml.dataset
;;
(require '[tech.v3.dataset :as tmd])

(tmd/->dataset data-url)

(tmd/->dataset data-url {:file-type :csv})

(tmd/->dataset data-url {:file-type :csv :header-row? false})


(def ds (tmd/->dataset data-url {:file-type :csv :header-row? false}))

;;    Confirm column labels
(tmd/head ds)

(dtype/->reader (ds "column-0") :float64)

(def sepal (dtype/->reader (ds "column-0")))
(dtype/elemwise-datatype sepal)

sepal
(class sepal)
(dtype/datatype sepal)

(def column (ds "column-0"))
(dtype/elemwise-datatype column)
column

(.data column)

(fun/+ sepal 10)
(fun/+ column 10)


