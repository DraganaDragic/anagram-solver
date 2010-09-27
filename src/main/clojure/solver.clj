(ns solver)

(import '[java.io FileInputStream InputStreamReader BufferedReader])

(use 'clojure.contrib.duck-streams)
(use 'clojure.contrib.seq-utils)

(def filename "dictionary.txt")

(defn read-word [idxForWord] (with-open [rdr (reader "dictionary.txt")]
(nth (line-seq rdr)idxForWord)))

(defn read-word [idxForWord] (nth (read-lines "dictionary.txt")idxForWord))

(defn make-map [string] (frequencies (str string)))
  
(defn count-keys [word] (count (keys(frequencies word))) ) 
    
(def all-words  (count (read-lines (str filename))))  

(def solution (ref nil))

(defn solve-word [word input]
       (loop [idx 0]
       (let [mapa1 (make-map word)]
       (let [mapa2 (make-map input)]
       (if(= idx  (count-keys (make-map word)))
       (dosync (ref-set solution word))
       (when (contains? mapa2 (nth (keys mapa1)idx))
       (if (<= (val (find mapa1 (nth (keys mapa1)idx))) (val (find mapa2 (nth (keys mapa1)idx)))) 
       (recur (inc idx))))
           )))))
       
 (defn find-word [input]
    (dosync (ref-set solution nil))
    (loop [idxForWord 0]
     (if (< idxForWord all-words)
       (let [word (read-word idxForWord)]
       (solve-word word input)
       (if (nil? @solution)
       (recur (inc idxForWord)))))))  