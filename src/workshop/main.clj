(ns workshop.main
  (:require [criterium.core :refer [quick-bench]]))

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
;; - typed
;; - lazy & non-caching

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

;; Wow! That's unusual. What's oging on here?  

;; How can we know what type of thing we are working with then?
(dtype/datatype [1 2 3])

(dtype/datatype a-buffer)

(dtype/elemwise-datatype a-buffer)

;; Hmmm why object? What's happening here? 

(dtype/elemwise-datatype (dtype/as-buffer (int-array [1 2 3])))

;; Better yet some pathways in dtype-next for making things with specific types.

(def an-int-buffer (dtype/->reader [1 2 3] :int32))

(dtype/elemwise-datatype an-int-buffer)

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

(def realized-br (dtype/make-container big-rdr))
(take 5 realized-br)

(dtype/writer? rdr)
(dtype/writer? (dtype/make-container rdr))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 3 Working with Buffers 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; So we now know what buffers are. How do we interact with them?

;; First we can often use clojure functions like map & reduce.
(reduce + (dtype/->reader (range 1000000 :int32)))
(keep #(when (odd? %) %) (dtype/->reader (range 10) :int32))

;; But we can't expect the operations to be particular efficient
;; and we leave dtype-next world when we use them.
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

(class (fun/- a b))

(fun/pow b 2)

(fun/log a)

;; Upcasting - dtype-next will upcast 
(def a-ints (dtype/->reader (range 10) :int32))

(def b-floats (dtype/make-reader :float32 10 (rand 10)))

(dtype/elemwise-datatype a-ints)

(dtype/elemwise-datatype b-floats)

(fun/* a-ints b-floats)

(dtype/elemwise-datatype (fun/* a-ints b-floats))

;; Mapping
(dtype/emap (constantly 0) :int64 (range 10))

(dtype/emap (fn [x] (+ x (/ x 10))) :float64 (range 10))

;; Subsetting
(dtype/sub-buffer (dtype/->reader (range 10) :int64) 5 3)


;; Filtering in index space
(require '[tech.v3.datatype.argops :as dtype-argops])

(let [rdr (dtype/->reader (range 10) :int32)
      indices (->> (dtype/->reader (range 10) :int64)
                   (dtype-argops/argfilter odd? {}))]
  (dtype/indexed-buffer indices rdr))


(require '[clojure.java.io :as io]
         '[clojure.data.csv :as csv]
         '[tech.v3.tensor :as tensor])

(def data-url
  "https://archive.ics.uci.edu/ml/machine-learning-databases/iris/iris.data" )


(def raw-data (-> data-url slurp csv/read-csv))

(def data (->> (vec raw-data)
               (dtype/emap first :object)
               (dtype/emap #(Float/parseFloat %) :float32)))

(take 5 data)


(let [smin (apply fun/min data)
      smax (apply fun/max data)]
  (fun// (fun/- data smin) (- smax smin)))







