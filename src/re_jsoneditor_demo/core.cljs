(ns re-jsoneditor-demo.core
  (:require
    [cljs.reader        :refer [read-string]]
    [reagent.core       :as r]
    [reagent.dom        :as rdom]
    [re-com.core        :as rc :refer [at]]
    [re-jsoneditor.core :refer [jsoneditor]]))

(defn main
  []
  (let [model                (r/atom {:apiVersion "v1"
                                      :kind       "Pod"
                                      :metadata   {:name   "static-web"
                                                   :labels {:role "myrole"}}
                                      :spec       {:containers [{:name  "web"
                                                                 :image "nginx"
                                                                 :ports [{:name          "web"
                                                                          :containerPort 80
                                                                          :protocol      "TCP"}]}]}})
        on-class-name?       (r/atom false)
        on-class-name        (fn [x]
                               (js/console.log x))
        on-editable?         (r/atom false)
        on-editable          (fn [x]
                               (js/console.log x)
                               true)
        on-error?            (r/atom false)
        on-error             (fn [err]
                               (js/console.warn err))
        on-mode-change?      (r/atom false)
        on-mode-change       (fn [old-mode new-mode]
                               (js/console.info "old-mode" old-mode)
                               (js/console.info "new-mode" new-mode))
        on-node-name?        (r/atom false)
        on-node-name         (fn [node]
                               (js/console.info "on-node-name %o" node)
                               "custom")
        on-validate?         (r/atom false)
        on-validate          (fn [data]
                               (js/console.info "on-validate" data)
                               true)
        on-validation-error? (r/atom false)
        on-validation-error  (fn [error]
                               (js/console.warn "on-validation-error %o" error))
        on-create-menu?      (r/atom false)
        on-create-menu       (fn [items node]
                               (js/console.info "on-create-menu items %o node %o" items node)
                               (let [on-click (fn [] (js/console.log (reduce (fn [ret segment] (if (number? segment) (str ret "[" segment "]") (str ret ".\"" segment "\""))) "" (:path node))))]
                                 (if (:path node)
                                   (conj items
                                         {:text  "jq Path"
                                          :title "Show the jq path for this node"
                                          :click on-click})
                                   items)))
        escape-unicode?      (r/atom false)
        sort-by-keys?        (r/atom false)
        limit-dragging?      (r/atom true)
        history?             (r/atom true)
        mode?                (r/atom false)
        mode                 (r/atom :tree)
        modes?               (r/atom false)
        modes                (r/atom #{:tree})
        root-name?           (r/atom false)
        root-name            (r/atom "")
        search?              (r/atom true)
        indentation          (r/atom 2)
        main-menu-bar?       (r/atom true)
        navigation-bar?      (r/atom true)
        status-bar?          (r/atom true)]
    (fn main-render
      []
      [rc/v-box
       :src      (at)
       :height   "100vh"
       :children [[rc/title
                   :src   (at)
                   :level :level1
                   :label "re-jsoneditor demo"]
                  [rc/box
                   :child (cond->
                            [jsoneditor
                             :model           model
                             :on-change       #(reset! model %)
                             :escape-unicode? @escape-unicode?
                             :sort-by-keys?   @sort-by-keys?
                             :limit-dragging? @limit-dragging?
                             :history?        @history?
                             :search?         @search?
                             :indentation     @indentation
                             :main-menu-bar?  @main-menu-bar?
                             :navigation-bar? @navigation-bar?
                             :status-bar?     @status-bar?]
                            @on-class-name?
                            (into [:on-class-name on-class-name])
                            @on-editable?
                            (into [:on-editable on-editable])
                            @on-error?
                            (into [:on-error on-error])
                            @on-mode-change?
                            (into [:on-mode-change on-mode-change])
                            @on-node-name?
                            (into [:on-node-name on-node-name])
                            @on-validate?
                            (into [:on-validate on-validate])
                            @on-validation-error?
                            (into [:on-validation-error on-validation-error])
                            @on-create-menu?
                            (into [:on-create-menu on-create-menu])
                            @mode?
                            (into [:mode @mode])
                            @modes?
                            (into [:modes @modes])
                            @root-name?
                            (into [:root-name @root-name]))]
                  [rc/scroller
                   :child [rc/v-box
                           :src      (at)
                           :gap      "19px"
                           :style    {:min-width        "150px"
                                      :padding          "15px"
                                      :border-top       "1px solid #DDD"
                                      :background-color "#f7f7f7"}
                           :children [[rc/title
                                       :src   (at)
                                       :level :level2
                                       :label "Interactive Parameters"
                                       :style {:margin-top "0"}]
                                      [rc/h-box
                                       :align    :center
                                       :gap      "7px"
                                       :children [[rc/box
                                                   :src   (at)
                                                   :width "212px"
                                                   :child [:code ":model"]]
                                                  [rc/input-textarea
                                                   :src             (at)
                                                   :width           "556px"
                                                   :height          "131px"
                                                   :model           (pr-str @model)
                                                   :change-on-blur? false
                                                   :on-change       #(reset! model (read-string %))]]]
                                      [rc/line]
                                      [rc/h-box
                                        :gap      "7px"
                                        :children [[rc/box
                                                    :width "212px"
                                                    :child [:code ":on-change"]]
                                                   [:code "#(reset! model %)"]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src (at)
                                       :gap      "7px"
                                       :width    "450px"
                                       :align :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src (at)
                                                           :label [:code ":on-class-name"]
                                                           :model on-class-name?
                                                           :on-change #(reset! on-class-name? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Set a callback function to add custom CSS classes to the rendered nodes. Only applicable when" [:code ":mode"] "is" [:code ":tree"] ", " [:code ":form"] ", or" [:code ":view"] "."]
                                                              [rc/p "The function is invoked with a map containing keys" [:code "path"] ", " [:code "field"] " and " [:code "value"] ":"]
                                                              [:pre "{:path [\"path1\" \"path2\"]\n :field \"fieldname\"\n :value \"value\"}"]
                                                              [rc/p "The function must either return a string containing CSS class names, or return" [:code "nil"] "in order to do nothing for a specific node."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src (at)
                                                           :label [:code ":on-editable"]
                                                           :model on-editable?
                                                           :on-change #(reset! on-editable? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Set a callback function to determine whether individual nodes are editable or read-only. Only applicable when option " [:code ":mode"] " is " [:code ":tree"] "," [:code ":text"] ", or " [:code ":code"] "."]
                                                              [rc/p "In case of mode " [:code ":tree"] ", the function is invoked with a map containing keys " [:code "path"] ", " [:code "field"] " and " [:code "value"] ":"]
                                                              [:pre "{:path [\"path1\" \"path2\"]\n :field \"fieldname\"\n :value \"value\"}"]
                                                              [rc/p "The function must either return a boolean value to set both the node's field and value editable or read-only, or return a map " [:code "{:field boolean :value boolean}"]
                                                               "to set the read-only attribute for field and value individually."]
                                                              [rc/p "In modes " [:code ":text"] " and " [:code ":code"] ", the function is invoked with an empty map. In that case the function can return " [:code "false"] " to make the text or code editor completely read-only."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-error"]
                                                           :model     on-error?
                                                           :on-change #(reset! on-error? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Set a callback function triggered when an error occurs. Invoked with the error as first argument. The callback is only invoked for errors triggered by a users action, like switching from code mode to tree mode or clicking the Format button whilst the editor doesn't contain valid JSON."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-mode-change"]
                                                           :model     on-mode-change?
                                                           :on-change #(reset! on-mode-change? %)]]
                                                  [rc/p "Set a callback function triggered right after the mode is changed by the user. Only applicable when the mode can be changed by the user (i.e. when " [:code ":modes"] "is set)."]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-node-name"]
                                                           :model     on-node-name?
                                                           :on-change #(reset! on-node-name? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Customize the name of object and array nodes. By default the names are brackets with the number of children inside, like "
                                                               [:code "{5}"] " and " [:code "{32}"] ". The argument is a map containing the following properties:"]
                                                              [:pre "{:path [\"path1\" \"path2\"]\n :type \"array\"\n :size 5}"]
                                                              [rc/p "The function should return a string containing the name for the node. If nothing is returned, the size (number of children) will be displayed."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-validate"]
                                                           :model     on-validate?
                                                           :on-change #(reset! on-validate? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Set a callback function for custom validation. Available in all modes."]
                                                              [rc/p "On a change of the JSON, the callback function is invoked with the changed data. The function should return a vector with errors or "
                                                               [:code "nil"] " if there are no errors. The function can also return a " [:code "js/Promise"]
                                                               "resolving with the errors retrieved via an asynchronous validation (like sending a request to a server for validation). The returned errors must have the following structure: "
                                                               [:code "{:path [ ... ] :message \"\"}"] ". Example:"]
                                                              [:pre ":on-validate\n(fn [json] (if (and (:customer json) (not (:address (:customer json))))\n[{:path"]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-validation-error"]
                                                           :model     on-validation-error?
                                                           :on-change #(reset! on-validation-error? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Set a callback function for validation and parse errors. Available in all modes."]
                                                              [rc/p "On validation of the json, if errors of any kind were found this function is invoked with the errors' data."]
                                                              [rc/p "On change, the function will be invoked only if errors also changed."]
                                                              [:pre ":on-validate-error\n  (fn [errors]\n    (run! (fn [error]\n            (case (:type error)\n              \"validation\" ... ;; schema validation error\n              \"customValidation\" ... ;; custom validation error\n              \"error\" ... ;; json parse error \n ))))"]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":on-create-menu"]
                                                           :model     on-create-menu?
                                                           :on-change #(reset! on-create-menu? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "Customize context menus in tree mode."]
                                                              [rc/p "Sets a callback function to customize the context menu in tree mode."
                                                               "Each time the user clicks on the context menu button, an array of menu items is created."
                                                               "If this callback is configured, the array with menu items is passed to this function."
                                                               "The menu items can be customized in this function in any aspect, including deleting them and/or adding new menu items."
                                                               "The function should return the final array of menu items to be displayed to the user."]
                                                              [rc/p "Each menu item is represented by a map, which may also contain a submenu vector of items."]
                                                              [rc/p "The second argument " [:code "node"] " is map containing the following keys:"]
                                                              [:pre "{:type \"single\" | \"multiple\" | \"append\"\n  :path [ ... ]\n  :paths [ ... ]}"]
                                                              [rc/p "The key " [:code ":path"] "contains the path of the node, and " [:code ":paths"]
                                                               "contains the paths of all selected nodes. When the user opens the context menu of an append node (in an empty object or array), the "
                                                               [:code ":type"] " will be " [:code "\"append\""] "and the " [:code ":path"] " will contain the path of the parent node."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":escape-unicode?"]
                                                           :model     escape-unicode?
                                                           :on-change #(reset! escape-unicode? %)]]
                                                  [rc/v-box
                                                   :src      (at)
                                                   :children [[rc/p "If " [:code "true"] ", unicode characters are escaped and displayed as their hexadecimal code (like " [:code "\\u260E"]
                                                               ") instead of the character itself (like " [:code "☎"] "). " [:code "false"] " by default."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":sort-by-keys?"]
                                                           :model     sort-by-keys?
                                                           :on-change (fn [val] (reset! sort-by-keys? val))]]
                                                  [rc/v-box
                                                   :src      (at)
                                                   :children [[rc/p "If " [:code "true"] ", object keys in "
                                                               [:code ":tree"] ", " [:code ":view"] " or " [:code ":form"]
                                                               " mode are listed alphabetically instead of by their insertion order."
                                                               " Sorting is performed using a natural sort algorithm, which makes it easier to see objects that have string numbers as keys. "
                                                               [:code "false"] " by default."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":limit-dragging?"]
                                                           :model     limit-dragging?
                                                           :on-change #(reset! limit-dragging? %)]]
                                                  [rc/v-box
                                                   :children [[rc/p "If " [:code "false"] ", nodes can be dragged form any parent node to any other parent node."]
                                                              [rc/p "If " [:code "true"] ", nodes can only be dragged inside the same parent node, with effectively only allows reordering of nodes."]
                                                              [rc/p "By default," [:code "limit-dragging?"] " is " [:code "true"] " when no JSON schema is defined, and " [:code "false"] " otherwise."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":history?"]
                                                           :model     history?
                                                           :on-change #(reset! history? %)]]
                                                  [rc/v-box
                                                   :src   (at)
                                                   :children [[rc/p "Enables history, adds a button Undo and Redo to the menu of the JSONEditor. " [:code "true"] " by default. Only applicable when " [:code "mode"] " is " [:code ":tree"] ", " [:code ":form"] ", or " [:code ":preview"] "."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src (at)
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":mode"]
                                                           :model     mode?
                                                           :on-change #(reset! mode? %)]]
                                                  (when @mode?
                                                    [rc/single-dropdown
                                                     :width     "131px"
                                                     :choices   [{:id :tree :label [:code ":tree"]}
                                                                 {:id :view :label [:code ":view"]}
                                                                 {:id :form :label [:code ":form"]}
                                                                 {:id :code :label [:code ":code"]}
                                                                 {:id :text :label [:code ":text"]}
                                                                 {:id :preview :label [:code ":preview"]}]
                                                     :model     mode
                                                     :on-change #(reset! mode %)])]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":modes"]
                                                           :model     modes?
                                                           :on-change #(reset! modes? %)]]
                                                  (when @modes?
                                                    [rc/selection-list
                                                     :parts     {:list-group-item {:style {:height "31px"}}}
                                                     :choices   [{:id :tree :label "Tree"}
                                                                 {:id :view :label "View"}
                                                                 {:id :form :label "Form"}
                                                                 {:id :code :label "Code"}
                                                                 {:id :text :label "Text"}
                                                                 {:id :preview :label "Preview"}]
                                                     :model     modes
                                                     :on-change #(reset! modes %)])]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":root-name"]
                                                           :model     root-name?
                                                           :on-change #(reset! root-name? %)]]
                                                  (when @root-name?
                                                    [rc/input-text
                                                     :src       (at)
                                                     :model     root-name
                                                     :on-change #(reset! root-name %)])]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                            :src       (at)
                                                            :label     [:code ":search?"]
                                                            :model     search?
                                                            :on-change (fn [val] (reset! search? val))]]
                                                  [rc/v-box
                                                   :children [[rc/p "Enables a search box in the upper right corner of the JSONEditor. " [:code "true"] " by default. Only applicable when " [:code ":mode"] " is " [:code ":tree"] ", " [:code ":view"] ", or " [:code ":form"] "."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :width "212px"
                                                   :child [:code ":indentation"]]
                                                  [:code @indentation]
                                                  [rc/gap :size "7px"]
                                                  [rc/slider
                                                   :src       (at)
                                                   :model     indentation
                                                   :on-change #(reset! indentation %)
                                                   :min       0
                                                   :max       8]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :src   (at)
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":main-menu-bar?"]
                                                           :model     main-menu-bar?
                                                           :on-change (fn [val] (reset! main-menu-bar? val))]]
                                                  [rc/v-box
                                                   :children [[rc/p "Adds main menu bar - Contains format, sort, transform, search etc. functionality. " [:code ":true"] " by default. Applicable in all types of " [:code ":mode"] "."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :src   (at)
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":navigation-bar?"]
                                                           :model     navigation-bar?
                                                           :on-change (fn [val] (reset! navigation-bar? val))]]
                                                  [rc/v-box
                                                   :children [[rc/p "Adds navigation bar to the menu - the navigation bar visualize the current position on the tree structure as well as allows breadcrumbs navigation. " [:code ":true"] " by default. Only applicable when " [:code ":mode"] " is " [:code ":tree"] ", " [:code ":form"] " or " [:code ":view"] "."]]]]]
                                      [rc/line]
                                      [rc/h-box
                                       :src      (at)
                                       :align    :center
                                       :children [[rc/box
                                                   :src   (at)
                                                   :width "212px"
                                                   :child [rc/checkbox
                                                           :src       (at)
                                                           :label     [:code ":status-bar?"]
                                                           :model     status-bar?
                                                           :on-change (fn [val] (reset! status-bar? val))]]
                                                  [rc/v-box
                                                   :children [[rc/p "Adds status bar to the bottom of the editor - the status bar shows the cursor position and a count of the selected characters. " [:code ":true"] " by default. Only applicable when " [:code ":mode"] " is " [:code ":code"] ", " [:code ":text"] ", or " [:code ":preview"] "."]]]]]]]]]])))

(defn get-element-by-id
  [id]
  (.getElementById js/document id))

(defn ^:dev/after-load mount-root
  []
  (rdom/render [main] (get-element-by-id "app")))
