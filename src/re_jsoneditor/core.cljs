(ns re-jsoneditor.core
  (:require
    [oops.core                    :refer [oget oset! ocall oapply ocall! oapply!
                                          oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
    [reagent.core                 :as r]
    [reagent.dom                  :as rdom]
    [re-com.util                  :refer [deref-or-value]]
    ["jsoneditor/dist/jsoneditor" :as JSONEditor]
    [re-jsoneditor.interop        :refer [->clj ->js]]))

(defn argv->map
  [[_ & {:as m}]] m)

(defn create
  [^js/Element container ^js js-options]
  (new JSONEditor container js-options))

(defn handle-on-change
  [this]
  (let [{:keys [model on-change on-error]} (argv->map (r/argv this))]
    (when (fn? on-change)
      (try
        (let [derefed-model         (deref-or-value model)
              {:keys [js-instance]} (r/state this)
              text                  (ocall js-instance :getText)]
          (if (empty? text)
            (when (not (nil? derefed-model))
              (on-change nil))
            (let [js-value (ocall js-instance :get)
                  value    (->clj js-value)]
              (when-not (= derefed-model value)
                (on-change value)))))
        (catch js/Error error
          (js/console.error error)
          (when (fn? on-error)
            (on-error error))))))) ;; TODO: wrap error

(defn ->js-options
  [this]
  (let [{:keys [model on-change on-class-name on-editable on-error on-mode-change on-node-name
                on-validate on-validation-error on-create-menu escape-unicode? sort-by-keys?
                limit-dragging? history? mode modes root-name schema schema-refs search? indentation
                theme templates autocomplete main-menu-bar? navigation-bar? status-bar?
                on-text-selection-change on-selection-change on-event on-focus on-blur color-picker?
                on-color-picker timestamp-tag timestamp-format language languages sort? transform?
                max-visible-children create-query execute-query query-description]
         :or   {escape-unicode?      false
                sort-by-keys?        false
                history?             true
                search?              true
                indentation          2
                main-menu-bar?       true
                navigation-bar?      true
                status-bar?          true
                color-picker?        true
                sort?                true
                transform?           true
                max-visible-children 100}
         :as   args} (argv->map (r/argv this))]
    ;; TODO limit-dragging? default to true when no schema, false otherwise
    ;; TODO theme
    ;; TODO autocomplete
    ;; TODO timestampTag
    ;; TODO timestampFormat
    ;; TODO createQuery
    ;; TODO executeQuery
    ;; TODO queryDescription
    (->js
      (cond->
        {:onChange         (partial handle-on-change this)
         :escapeUnicode    escape-unicode?
         :sortObjectKeys   sort-by-keys?
         :limit-dragging?  limit-dragging?
         :history          history?
         :search           search?
         :indentation      indentation
         :mainMenuBar      main-menu-bar?
         :navigationBar    navigation-bar?
         :statusBar        status-bar?
         :colorPicker      color-picker?
         :enableSort       sort?
         :enableTransform  transform?
         :maxVisibleChilds max-visible-children}
        (fn? on-class-name)
        (assoc :onClassName #(->js (on-class-name (->clj %))))
        (fn? on-editable)
        (assoc :onEditable #(->js (on-editable (->clj %))))
        (fn? on-mode-change)
        (assoc :onModeChange #(on-mode-change (keyword %2) (keyword %1)))
        (fn? on-node-name)
        (assoc :onNodeName #(on-node-name (->clj %)))
        (fn? on-validate)
        (assoc :onValidate (fn [json]
                             (let [result (on-validate (->clj json))]
                               (if (instance? js/Promise result)
                                 result
                                 (->js result)))))
        (fn? on-validation-error)
        (assoc :onValidationError #(on-validation-error (->clj %)))
        (fn? on-create-menu)
        (assoc :onCreateMenu #(->js (on-create-menu (->clj %1) (->clj %2))))
        (keyword? mode)
        (assoc :mode (name mode))
        modes
        (assoc :modes modes)
        root-name
        (assoc :name root-name)
        schema
        (assoc :schema schema)
        schema-refs
        (assoc :schemaRefs schema-refs)
        templates
        (assoc :templates (->js templates))
        (fn? on-text-selection-change)
        (assoc :onTextSelectionChange #(on-text-selection-change (->clj %1) (->clj %2) %3))
        (fn? on-selection-change)
        (assoc :onSelectionChange #(on-selection-change (->clj %1) (->clj %2)))
        (fn? on-event)
        (assoc :onEvent #(on-event (->clj %1) (->clj %2)))
        (fn? on-focus)
        (assoc :onFocus #(on-focus (->clj %)))
        (fn? on-blur)
        (assoc :onBlur #(on-blur (->clj %)))
        (fn? on-color-picker)
        (assoc :onColorPicker #(on-color-picker %1 %2 %3))
        (keyword? language)
        (assoc :language (name language))
        (map? languages)
        (assoc :languages (->js languages))))))

(defn create-js-instance
  [this {:keys [selection text-selection] :as snapshot}]
  (let [^js/Element container (rdom/dom-node this)
        ^js js-options        (->js-options this)
        ^js js-instance       (new JSONEditor container js-options)
        {:keys [model]}       (argv->map (r/argv this))
        deref-model           (deref-or-value model)]
    (ocall js-instance :set (->js deref-model))
    (r/set-state this {:js-instance js-instance})
    (let [mode (keyword (oget js-instance :getMode))]
      (when (and (= :tree mode) selection)
        (ocall js-instance :setSelection (oget selection :start) (oget selection :end)))
      (when (and (or (= :text mode) (= :code mode)) text-selection)
        (ocall js-instance :setTextSelection (oget text-selection :start) (oget text-selection :end)))
      js-instance)))

(defn destroy-js-instance
  [this]
  (let [{:keys [js-instance]} (r/state this)]
    (when js-instance
      (ocall js-instance :destroy))))

(defn get-initial-state
  [this]
  {})

(defn get-derived-state-from-props
  [props state]
  {})

(defn get-snapshot-before-update
  [this old-argv new-argv]
  (let [{:keys [js-instance]} (r/state this)
        mode                  (ocall js-instance :getMode)]
    (let [snapshot
          (cond
            (= "tree" mode)
            {:selection (ocall js-instance :getSelection)}

            (or (= "text" mode)
                (= "code" mode))
            {:text-selection (ocall js-instance :getTextSelection)}

            :default
            {})]
      snapshot)))

(defn should-update
  [this old-argv new-argv]
  true)

(defn did-mount
  [this]
  (create-js-instance this nil))

(defn should-recreate-js-instance?
  [this old-argv]
  (let [[_ & {:as old-options}] old-argv
        new-options             (argv->map (r/argv this))]
    (not= (dissoc old-options :model :on-change :mode)
          (dissoc new-options :model :on-change :mode))))

(defn update-js-instance
  [this old-argv]
  (let [{:keys [js-instance
                external-model]} (r/state this)
        old-options              (argv->map old-argv)
        new-options              (argv->map (r/argv this))
        {old-mode  :mode}        old-options
        {new-model :model
         new-mode  :mode}        new-options
        latest-external-model    (deref-or-value new-model)]
    (when (not= external-model latest-external-model)
      (r/set-state this {:external-model latest-external-model})
      (ocall js-instance :update (->js latest-external-model)))
    (when (and (keyword? new-mode) (not= old-mode new-mode))
      (ocall js-instance :setMode (name new-mode)))))

(defn did-update
  [this old-argv old-state snapshot]
  (if (should-recreate-js-instance? this old-argv)
    (do
      (destroy-js-instance this)
      (create-js-instance this snapshot))
    (update-js-instance this old-argv)))

(defn will-unmount
  [this]
  (when-let [{:keys [js-instance]} (r/state this)]
    (ocall js-instance :destroy)
    (r/set-state this {:js-instance nil})))

(defn did-catch
  [this error info]
  (js/console.warn "did-catch" error info))

(defn render
  [this]
  (let [{:keys [model]} (argv->map (r/argv this))]
    (r/set-state this {:external-model (deref-or-value model)}))
  [:div])

(defn jsoneditor
  []
  (let []
    (r/create-class
      {:get-initial-state            get-initial-state
       :get-derived-state-from-props get-derived-state-from-props
       ;:get-derived-state-from-error
       :get-snapshot-before-update   get-snapshot-before-update
       :should-component-update      should-update
       :component-did-mount          did-mount
       :component-did-update         did-update
       :component-will-unmount       will-unmount
       ;:component-did-catch         did-catch
       :render                       render})))