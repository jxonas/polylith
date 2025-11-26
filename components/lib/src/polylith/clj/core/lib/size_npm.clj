(ns ^:no-doc polylith.clj.core.lib.size-npm
  (:require [polylith.clj.core.file.interface :as file]))

(defn keyword->package-name
  "Convert a keyword to an npm package name string, preserving scoped package names.
   For example, :@mantine/core becomes '@mantine/core', and :lodash becomes 'lodash'."
  [kw]
  (if (keyword? kw)
    (subs (str kw) 1)
    (str kw)))

(defn with-size [[lib version] node-modules-path]
  (let [lib-name (keyword->package-name lib)]
    [lib-name
     {:version version
      :type "npm"
      :size (file/directory-size (str node-modules-path "/" lib-name))}]))

(defn with-sizes-vec [ws-dir libraries]
  (let [node-module-path (str ws-dir "/node_modules")]
    (mapv #(with-size  % node-module-path)
          libraries)))
