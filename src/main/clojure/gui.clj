(ns gui)



(import (java.awt BorderLayout Event GridLayout Toolkit)
           (java.awt.event KeyEvent)
           (javax.swing AbstractAction Action BorderFactory
           JFrame JPanel JButton JMenu JMenuBar JTextField JLabel
           KeyStroke)
           (javax.swing.event DocumentListener)
           )
          
(use 'clojure.contrib.seq-utils)

(use 'solver)
      

(defmacro lazy-init [f & args]
  `(let [x# (delay (~f ~@args))]
    #(force x#)))
    

       
 (defn has-text? [s]
  (and (not (nil? s)) (> (count s) 0)))

(defn field-has-text? [text-field]
  (has-text? (.getText text-field)))

(defn has-valid-string-text? [field]
    (let [
      text (.toLowerCase (.getText field))]
      (= (count text) (count (re-find #"[a-z]+" text)))))

(defn create-simple-document-listener [behavior]  
  (proxy [DocumentListener][]
    (changedUpdate [event] (behavior event))
    (insertUpdate [event] (behavior event))
    (removeUpdate [event] (behavior event))))

(defn create-a-text-field [] 
  (doto (JTextField. 15)))

(def get-letters-text-field (lazy-init create-a-text-field))

(def get-solution-text-field (lazy-init create-a-text-field))

(defn create-converters-panel []
  (let [
    create-an-inner-panel #(JPanel. (GridLayout. 0 1 5 5))
    label-panel (create-an-inner-panel)
    text-field-panel (create-an-inner-panel)
    outer-panel (JPanel. (BorderLayout.))]
    (doto label-panel
      (.add (JLabel. "Letters: "))
      (.add (JLabel. "Solution: ")))
    (doto text-field-panel
      (.add (get-letters-text-field))
      (.add (get-solution-text-field)))
    (doto outer-panel
      (.add label-panel BorderLayout/WEST)
      (.add text-field-panel BorderLayout/CENTER))))

(defn create-action [name behavior options]
  (let [
    action (proxy [AbstractAction] [name]
      (actionPerformed [event] (behavior event)))]
    (if options
      (doseq [key (keys options)] (.putValue action key (options key))))
    action))

(def clear-action (create-action "Clear"
    (fn [_]
      (.setText (get-letters-text-field) "")
      (.setText (get-solution-text-field) ""))
    { Action/SHORT_DESCRIPTION "Reset to empty fields",
        Action/ACCELERATOR_KEY
            (KeyStroke/getKeyStroke KeyEvent/VK_L Event/CTRL_MASK) }))

(def exit-action (create-action "Exit"
    (fn [_] (System/exit 0))
    { Action/SHORT_DESCRIPTION "Exit this program",
      Action/ACCELERATOR_KEY
            (KeyStroke/getKeyStroke KeyEvent/VK_X Event/CTRL_MASK) }))

(def l-action (create-action "Solve"
    (fn [_]
      (let [
        text (.getText (get-letters-text-field))
        input (str text)]
       (find-word input)
       (.setText (get-solution-text-field) (str @solution))))
    { Action/SHORT_DESCRIPTION "Find the longest word from given letters",
        Action/ACCELERATOR_KEY
            (KeyStroke/getKeyStroke KeyEvent/VK_S Event/CTRL_MASK) }))

(defn clear-enabler [_]
  (let [
    l-has-text (field-has-text? (get-letters-text-field))
    s-has-text (field-has-text? (get-solution-text-field))
    should-enable (or l-has-text s-has-text)]
    (.setEnabled clear-action should-enable)))

 (defn setup-text-field-listeners []
  (let [
    clear-enabler (create-simple-document-listener clear-enabler)
    l-enabler (create-simple-document-listener
        (fn [_] (.setEnabled l-action
        (has-valid-string-text? (get-letters-text-field)))))]
    (doto (.getDocument (get-letters-text-field))
      (.addDocumentListener clear-enabler)
      (.addDocumentListener l-enabler))
    (doto (.getDocument (get-solution-text-field))
      (.addDocumentListener clear-enabler))))

(defn create-buttons-panel []
  (let [
    inner-panel (JPanel. (GridLayout. 1 0 5 5))
    outer-panel (JPanel. (BorderLayout.))]
    (doto inner-panel
      (.add (JButton. l-action))
      (.add (JButton. clear-action))
      (.add (JButton. exit-action)))
    (doto outer-panel
      (.add inner-panel BorderLayout/EAST)
      (.setBorder (BorderFactory/createEmptyBorder 10 0 0 0)))))


(defn center-on-screen
[component]
  (let [
    screen-size (.. Toolkit getDefaultToolkit getScreenSize)
    screen-width (.getWidth screen-size)
    screen-height (.getHeight screen-size)
    comp-width (.getWidth component)
    comp-height (.getHeight component)
    new-x (/ (- screen-width comp-width) 2)
    new-y (/ (- screen-height comp-height) 2)]
    (.setLocation component new-x new-y))component)

(defn setup-initial-action-states []
  (doseq [a [clear-action l-action]] (.setEnabled a false)))
  (defn create-frame []
  (let [
    f (JFrame. "Anagram-solver")
    content-pane (.getContentPane f)]
    (doto content-pane
      (.add (create-converters-panel) BorderLayout/CENTER)
      (.add (create-buttons-panel) BorderLayout/SOUTH)
      (.setBorder (BorderFactory/createEmptyBorder 12 12 12 12)))
    (doto f
      (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.pack))
    (setup-text-field-listeners)
    (setup-initial-action-states)
    (center-on-screen f)))

(defn main []
    (def main-frame (create-frame))
    (.setVisible main-frame true))


