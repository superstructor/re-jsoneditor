(ns re-jsoneditor.interop)

(defn ->clj
  [js-x]
  (js->clj js-x :keywordize-keys true))

(defn ->js
  [x]
  (clj->js x))
