(ns contrib.deptector
  "Dectect available namespaces without requiring them.")

(def root-resource @#'clojure.core/root-resource)
(def root-directory @#'clojure.core/root-directory)

(defn resource-path [path] ; from clojure.lang.RT/load
  (let [classfile (str path clojure.lang.RT/LOADER_SUFFIX ".class")
        cljfile   (str path ".clj")
        cljcfile  (str path ".cljc")
        classURL  (clojure.lang.RT/getResource (clojure.lang.RT/baseLoader) classfile)
        cljURL    (or (clojure.lang.RT/getResource (clojure.lang.RT/baseLoader) cljfile)
                    (clojure.lang.RT/getResource (clojure.lang.RT/baseLoader) cljcfile))]
    (or classURL cljURL)))

(defn find-ns-file [ns-sym]
  (let [path (root-resource ns-sym)
        path (if (.startsWith path "/")
               path
               (str (root-directory (ns-name *ns*)) \/ path))]
    (resource-path (.substring path 1))))

(defn locate-namespace
  "Try to find a namespace. If the namespace is loaded, return the NS object like `find-ns`.
  If the namespace is not loaded but available on the classpath, return a
  java.net.URL locating the file that could be loaded for this ns. Return nil
  otherwise."
  [ns-sym]
  (or (find-ns ns-sym) (find-ns-file ns-sym)))

(defn available?
  "State if a namespace is available WITHOUT requiring it.
  A namespace is available if it is already loaded or if it can be loaded from the classpath."
  [ns-sym]
  (boolean (locate-namespace ns-sym)))
