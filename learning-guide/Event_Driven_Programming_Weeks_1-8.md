# Event-Driven Programming: Learning Guide for Weeks 1–8

This guide explains the unit using the lecturer’s Week 1–8 activities and the Java files in this repository. It is a study resource; the weekly folders remain the coursework submissions.

## How to use this guide

Read the weeks in order. For each program, try to say aloud: **what starts the work, what event or value is passed, which code reacts, and what state or display changes**. Then open the named source file and trace it yourself.

## A small vocabulary

- **Event-driven programming (EDP):** a program waits for something to happen, then runs the code registered for that event. A button click, key press, property change, or timer tick can be an event.
- **Event source:** the object where the event begins, such as a `JButton`, JavaFX `Button`, text field, or `WeatherData` subject.
- **Event object:** information about one occurrence. Examples include Swing `ActionEvent`, JavaFX `MouseEvent`, and a custom `LoginSuccessEvent`.
- **Listener/handler:** code registered to react. A listener may be an object implementing an interface; JavaFX also calls its event callbacks handlers.
- **Event loop:** the framework’s loop that waits for events, dispatches them to the right callbacks, and then waits again.
- **State:** information kept between events, such as a calculator’s first number or a counter’s current value.
- **Loose coupling:** one component communicates through a small interface or event contract and does not need to know the concrete classes reacting to it.
- **Property/binding:** in JavaFX, an observable value and a rule that keeps another value synchronized with it.

## Week 1 — What EDP is and how it differs from procedural code

### What the activities teach

The “Spot the Components” and “Build the Event Loop” activities are conceptual. They ask you to find event sources, events, and reactions in familiar systems, then model how an event loop selects and dispatches an event. These are written/design activities; they are not Java programs to invent.

The coding comparison is in `Week1_EDP/procedural/ProceduralCalculator.java` and `Week1_EDP/eventdriven/EventDrivenCalculator.java`.

### Procedural calculator walkthrough

`ProceduralCalculator.main` runs in a fixed sequence:

1. Create a `Scanner` connected to keyboard input.
2. Read `num1`, then `operator`, then `num2`.
3. Use `switch` to calculate.
4. Print the result and close the scanner.

The user follows the program’s prompts. The program does not wait for a window button; the next instruction is already known.

### Event-driven calculator walkthrough

`EventDrivenCalculator` is a `JFrame`. Its constructor builds the display and a `JPanel` with a `GridLayout`, then registers an `ActionListener` on each button. When a number button is clicked, its callback updates the display. Operator buttons save `firstOperand` and `operator`; the equals button reads the second operand, calculates, and changes the display. `startNewNumber` tells the next digit whether to replace the display or append to it.

The callback runs later, after the framework receives a click. The calculator therefore keeps state in fields between clicks. This is the first important EDP difference: the sequence is driven by events and user choices, not by one top-to-bottom script.

### Smart-home prototype and debugging challenge

`Week1_EDP/SmartHomePrototype.java` maps this idea to a GUI: the light/security toggle buttons and door button are sources; `ActionEvent` callbacks update `statusLabel`, append to `eventLog`, and change the window background when the alarm is active. `logEvent` is a helper shared by those reactions.

The four `BugFixChallenge` programs focus on typical GUI failures:

- **Blocking the EDT:** long work inside a Swing callback prevents the event dispatch thread from repainting or handling clicks.
- **Missing listener:** a button may display correctly yet do nothing if no callback was registered.
- **Wrong thread:** Swing component changes must be made on the EDT; background work should return its result to the EDT.
- **Memory leak:** a long-lived source retaining listeners can also retain objects that should be collectible. Remove listeners when their lifetime ends.

### Common mistakes

- Treating a UI as a list of instructions instead of a collection of components waiting for events.
- Keeping a temporary value only in a local variable when it must survive until a later click.
- Doing a slow operation directly inside a GUI event callback.
- Confusing “the button was created” with “the button has a listener.”

### Check yourself

1. In the procedural calculator, what controls the order of input? **The written sequence of prompts and reads.**
2. In the event-driven calculator, what causes the addition code to run? **The user activates the equals button, which generates an action event handled by its registered listener.**
3. Why are `firstOperand` and `operator` fields? **Their values must remain available across separate button events.**

## Week 2 — Observer Pattern and event notification

### Subject and observers

The Observer Pattern is a way for one object to announce a change to multiple interested objects without naming their concrete classes. A **subject** stores/registers observers and notifies them; an **observer** implements the agreed update method.

In `week02-observer-pattern/activity1-weather-station`:

- `Subject` defines registration, removal, and notification operations.
- `Observer` defines `update(float temperature, float humidity, float pressure)`.
- `WeatherData` stores measurements and a list of observers. `setMeasurements` updates all three fields then calls `notifyObservers`.
- `CurrentConditionsDisplay`, `StatisticsDisplay`, and `ForecastDisplay` each respond differently to the same measurement event.
- `WeatherStationMain` creates the subject, registers displays, changes measurements, and removes the forecast display to show that it no longer receives later notifications.

The flow is: main sets measurements → `WeatherData` changes its state → `WeatherData` loops through registered `Observer` objects → each `update` prints its own result. `WeatherData` depends on the `Observer` interface, not on each display class.

### Comparing notification mechanisms

The Week 2 legacy activity shows `java.util.Observable` and `java.util.Observer`, which the guide identifies as deprecated. The custom `Subject`/`Observer` interfaces make the contract visible and under your control. The comparison activity also demonstrates `PropertyChangeSupport` and `PropertyChangeListener`, a standard Java way to announce named property changes.

Activity 3 begins with `TightlyCoupledSystem` and refactors to `DataSubject`, `DataObserver`, and `DataModel`. The key improvement is that `DataModel` calls `onDataChanged(value)` on the interface; it does not construct or update a particular label, chart, or logger. `LabelDisplay`, `BarChartDisplay`, and `TextLogger` can react independently.

### Common mistakes

- Forgetting to register an observer, or expecting a removed observer to keep receiving changes.
- Updating the subject’s data but forgetting to notify observers.
- Storing concrete display types in the subject instead of the observer interface.
- Confusing the **Observer Pattern** (a design arrangement) with a specific Java class or GUI framework.

### Check yourself

1. Who owns the observer list? **The subject (`WeatherData` or `DataModel`).**
2. Does a subject need to know that one observer is a forecast display? **No; it knows the observer interface.**
3. What changes when an observer is removed? **That observer is no longer included in later notification loops.**

## Week 3 — Building GUI structure with Swing

### The Swing building blocks

`JFrame` is a top-level window; `JPanel` groups components; `JLabel`, `JButton`, `JTextField`, `JCheckBox`, and similar controls are components users see or operate. Swing controls are in `javax.swing`; layout classes and AWT support classes are in `java.awt`.

`week03-swing-gui/activity1-first-window/FirstWindow.java` introduces the frame. `activity2-basic-components/ComponentShowcase.java` displays common controls. These activities teach construction and arrangement; interaction is the focus of Week 4.

### Layout managers

The Week 3 demos show that a layout manager positions components as the window changes size:

- `FlowLayoutDemo`: places items in a row and wraps them when needed.
- `BorderLayoutDemo`: uses `NORTH`, `SOUTH`, `EAST`, `WEST`, and `CENTER` regions.
- `GridLayoutDemo`: gives each cell equal size, useful for the calculator-like grid.
- `CombinedLayoutDemo`: nests panels with different layouts to build a more complete screen.

`StudentPortal.java` combines these ideas into a static portal mockup. A nested layout is common: each panel solves one local arrangement problem, and the frame combines the panels.

### The EDT rule

Swing has one main UI thread, the **Event Dispatch Thread (EDT)**. It dispatches events and updates Swing components. `FreezeDemo.java` demonstrates the problem caused by sleeping or doing slow work in a button callback: while that callback is busy, repainting and other events wait. `FixedDemo.java` moves the slow work away from the EDT and returns UI updates to the EDT.

`SwingUtilities.invokeLater(...)` schedules UI setup or updates on the EDT. The core rule is: **keep the EDT responsive; do lengthy work elsewhere; update Swing widgets on the EDT.**

### Common mistakes

- Adding every component directly to one frame and losing control of the layout.
- Using absolute coordinates for a window that should resize.
- Running `Thread.sleep` or a long loop on the EDT.
- Updating a Swing label from a worker thread.

### Check yourself

1. What does a layout manager decide? **Where and how components are sized/arranged in a container.**
2. Why does a slow EDT callback freeze the window? **The same thread is responsible for events and UI painting.**
3. When would you nest panels? **When different regions need different layout rules.**

## Week 4 — Swing event handling

### ActionListener and ActionEvent

In `FirstButton.java`, the `JButton` is the event source. `addActionListener` registers the reaction. When activated, Swing creates an `ActionEvent` and calls `actionPerformed`. The lambda `e -> { ... }` is a concise implementation of the single-method `ActionListener` interface.

The guide compares three listener styles: a separate named listener class, an anonymous class, and a lambda. They can all implement the same callback contract; the lambda is concise for a short reaction, while a named class can be clearer for larger logic. `EventSourceDemo.java` shows how `e.getSource()` can distinguish which control fired when several share one listener.

### Other listener types

- `ItemListener` is suited to selection changes such as checkboxes and radio buttons. Its `ItemEvent` indicates selected or deselected state.
- `DocumentListener` observes edits to a Swing text document with `insertUpdate`, `removeUpdate`, and `changedUpdate`. It is used for immediate feedback and live search, not only after Enter is pressed.
- Mouse and keyboard listeners handle lower-level interactions, such as drawing or moving an object. Key events require focus.

### Counter and registration form

`CounterApp.java` stores `count` as instance state. Increment and decrement listeners enforce `MIN_VALUE` and `MAX_VALUE`, then call `updateDisplay`; reset changes the state to zero. This is the same state-and-event cycle as the calculator, with the range checks made visible.

`RegistrationForm.java` wires form controls to event handlers. The Week 4 guide also includes mouse and keyboard examples in `DrawingApp.java` and `KeyboardMover.java`.

### Common mistakes

- Replacing `DocumentListener` with a button listener when the requirement is live updates while typing.
- Changing a document directly inside its own document notification callback; instead schedule the follow-up change or change a different component.
- Assuming a key listener fires when the component does not have focus.
- Forgetting to update the display after updating a field such as `count`.

### Check yourself

1. Which Swing method registers an action listener? **`addActionListener`.**
2. Which listener observes text-document edits? **`DocumentListener`.**
3. Why keep a counter in an instance field? **Each click is a separate callback, but the count must persist between them.**

## Week 5 — Advanced Swing components

Week 5 moves from simple controls to components that display collections of data.

- `ContactListDemo.java` uses `JList`; a custom cell renderer changes how each item is drawn. A renderer is a reusable painting description, not a separate component permanently created for every row.
- `StudentTableDemo.java` uses `JTable` with a table model. The model describes rows and columns; a renderer can customize cell appearance.
- `FileExplorerDemo.java` combines `JTree` for hierarchical data with `JTabbedPane` for multiple views/settings.
- `MiniEditor.java` builds menus, toolbar actions, file operations, and dialogs. Menu items and toolbar buttons can share the same `Action` so the behavior is not duplicated.
- `FileManager.java` is the capstone combining a file tree/list, tabs, menus, and dialogs.

The recurring design idea is **model versus view**: collection data has a model, while Swing components render or edit that model. When the model changes, the component can refresh from it.

### Common mistakes

- Treating a renderer as the data model; it controls appearance, not the stored list/table data.
- Assuming a tree is a flat list; tree nodes have parent/child relationships.
- Duplicating menu and toolbar logic instead of sharing an action.
- Confusing a selected row’s displayed text with the underlying model value.

### Check yourself

1. What does a table model provide? **The table’s data shape and values.**
2. What does a renderer provide? **How a value is drawn.**
3. Which component represents hierarchical data? **`JTree`.**

## Week 6 — JavaFX application structure, layouts, and FXML

### Stage, Scene, Node

JavaFX uses a window called a `Stage`. A `Scene` holds the visible content, and the content is a tree of `Node` objects such as layouts, labels, and buttons. `App.java` creates a `Label` and `Button`, places them in a `VBox`, creates a `Scene`, assigns it to the stage, and calls `show()`.

The button’s `setOnAction` handler changes the label and prints the thread name. JavaFX UI callbacks run on the **JavaFX Application Thread**. Long work should not block that thread, just as long work should not block Swing’s EDT.

### Layout panes

`LayoutDemo.java` combines `HBox` (horizontal), `VBox` (vertical), `GridPane` (rows and columns), and `BorderPane` (top/left/center regions). `DashboardApp.java` uses a top bar, sidebar, welcome message, and statistic cards. These are layout nodes; the layout pane manages where its children go.

### FXML, Scene Builder, and controller

`login.fxml` describes the login view as XML: the root `VBox`, its padding and spacing, and a label, username field, password field, and button. The `fx:id` values identify controls that the controller needs. `FXMLApp.java` loads the resource with `FXMLLoader` and puts the loaded root into a `Scene`.

`LoginController.java` uses `@FXML` fields for injected controls and `handleLoginButtonAction` for the button reaction. The button’s `onAction` in FXML connects to that method. `@FXML` does not find a control by variable name alone; its `fx:id` must match the controller field, and the controller method name must match the FXML action.

### Maven and JavaFX setup

The Week 6 project uses Maven dependencies for `javafx-controls` and `javafx-fxml`, plus the `javafx-maven-plugin`. The lecturer’s JavaFX 21 dependencies compiled and ran on the installed Java 24 here. The Maven property `javafx.mainClass` defaults to `com.example.App`; the command-line override selects another activity.

### Common mistakes

- Forgetting to add the JavaFX dependency or using a JDK without JavaFX modules configured.
- Putting FXML outside `src/main/resources` or using the wrong resource path.
- Mismatching `fx:id`, `fx:controller`, or `onAction` names.
- Updating UI controls from a background thread instead of the JavaFX Application Thread.

### Check yourself

1. What does a `Stage` represent? **The top-level application window.**
2. What does `FXMLLoader` do? **Reads the FXML view and constructs its node tree/controller connection.**
3. What connects the FXML button to the controller method? **Its `onAction` attribute naming the `@FXML` method.**

## Week 7 — JavaFX event propagation, input, drag-and-drop, and custom events

### Filters and handlers

JavaFX event routing has a **capturing** phase and a **bubbling** phase. Filters run from an ancestor toward the target; handlers run at the target and then up through ancestors if the event continues. `event.consume()` stops further routing.

`EventPropagationDemo.java` registers a root filter, a mouse-click handler on the button, and a root handler. The filter and button handler run in the current checks. The guide predicts a third parent-handler line, but that third line was not observed when the target was a JavaFX `Button`; preserve this as a tested behavior difference in the supplied example rather than claiming the three-line trace was verified.

### Mouse and keyboard

`MouseKeyDemo.java` reads `MouseEvent.getX()` and `getY()` to show pointer coordinates. It checks `KeyEvent.getCode()` against `KeyCode.UP`, `DOWN`, `LEFT`, and `RIGHT`, then changes the circle’s center. JavaFX uses `setFocusTraversable(true)` to make a node eligible for keyboard focus; the root requests focus after the window appears. (The supplied guide uses `setFocusable`, which is not the JavaFX API; the code uses the compiling JavaFX method.)

### Drag-and-drop

`DragDropDemo.java` begins the gesture in `setOnDragDetected`, places text on a `Dragboard`, accepts the `MOVE` transfer mode in `setOnDragOver`, changes the target’s fill on drag entry/exit, and reads the dragboard in `setOnDragDropped`. `setDropCompleted(success)` tells JavaFX whether the target accepted the item. A working drag needs a source, transferable data, a target that accepts it, and completion reporting.

### Custom events

`LoginSuccessEvent` extends JavaFX `Event`, declares an `EventType<LoginSuccessEvent>`, and carries a username. `CustomEventDemo` has the button fire that event on the root. A separate root handler prints the username, updates the welcome label, and clears the text field. The button code does not need to know which services react to a successful login.

### Common mistakes

- Registering a filter when a handler was intended, or vice versa.
- Forgetting to consume an event when it must not continue.
- Expecting a key event without focus.
- Forgetting `acceptTransferModes` or `setDropCompleted` in a drag-and-drop flow.
- Creating a custom event but firing a different event type or listening on a node that is not on its route.

### Check yourself

1. Which phase is top-down? **Capturing/filter phase.**
2. What ends further propagation? **`event.consume()`.**
3. What decouples the custom login button from its reaction? **The button fires a custom event; registered event handlers respond independently.**

## Week 8 — JavaFX properties, listeners, and bindings

### Properties as observable model values

`Person.java` stores `name` as a `StringProperty` and `age` as an `IntegerProperty`. The normal getters/setters provide ordinary Java access; `nameProperty()` and `ageProperty()` return the observable objects so listeners and bindings can attach. A plain value gives you the current data; a property also provides a way to observe or connect to future changes.

### Change and invalidation listeners

`ListenerDemo.java` registers an `InvalidationListener`, which says that a value became invalid/changed, and a `ChangeListener`, which provides old and new values. Setting the name to Alice and then Bob produces notifications; setting it to Bob again produces none because the value did not change. This demo is console-only and calls `Platform.exit()` after printing so Maven can terminate normally.

### One-way and two-way binding

`UnidirectionalDemo.java` binds the greeting label’s `textProperty()` to the text field’s text plus `", welcome!"`. The field is the source; the label is the target. Do not call `setText` on a property while it is bound unless you first unbind it.

`BidirectionalDemo.java` connects a slider’s numeric `valueProperty` and a text field’s string `textProperty`. `NumberStringConverter` converts between those types. Changing either control updates the other.

### Reactive registration form

`ReactiveFormApp.java` binds three things:

1. Character count to the bio length.
2. Preview text to the name and bio.
3. Submit button disabled state to the validity rules (nonempty name/email, email contains `@`, bio length no more than 150).

Only the final submit action needs a click handler. The displays and enable/disable rule update from the property graph. The lecturer sample’s `StringProperty.contains("@")` does not compile with the provided API type, so the project uses `Bindings.createBooleanBinding` with `emailField.textProperty()` as its dependency. The rule stays reactive while compiling correctly.

### Common mistakes

- Returning `getName()` where a binding needs `nameProperty()`.
- Attempting to set a one-way-bound target directly.
- Binding different data types without a converter.
- Forgetting to list a property dependency when creating a custom binding.
- Writing a manual click/key listener for a value that can be expressed as a property binding.

### Check yourself

1. What does a property getter return? **The observable property object, not only its current value.**
2. Which direction does `bind()` update? **From source to target.**
3. Why is a converter used for slider/text binding? **The slider stores a number and the text field stores characters.**

## Putting the weeks together

The course moves through three connected ideas:

1. **An event starts work.** Weeks 1, 3, 4, 6, and 7 show sources and handlers.
2. **Components communicate without depending on each other’s concrete classes.** Week 2 introduces observers; Week 7 creates a custom event.
3. **The UI reflects state changes.** Week 5 separates collection models from renderers; Week 8 makes JavaFX models observable and binds views to them.

When debugging, check in this order: (a) does the source exist? (b) was the listener/handler actually registered? (c) is the event/value the type you expect? (d) is the callback running? (e) is state updated? (f) is the correct UI thread updating the view? For Maven JavaFX programs, also check that the selected main class, FXML resource path, controller, and JavaFX dependencies agree.

## Mixed review questions

1. Trace one weather measurement from `setMeasurements` until all three displays react.
2. Explain one difference between `ActionListener` and `DocumentListener` using a concrete control.
3. Why does sleeping in a Swing button listener freeze the window?
4. In FXML login, what must match between the XML and the controller?
5. Describe the data flow for a one-way binding and a bidirectional binding.
6. When would you use an observer interface instead of making a model call a specific display class?
7. Explain the difference between a JavaFX event filter and event handler, including `consume()`.
8. In the registration form, which properties determine whether Submit is disabled?

### Short answer key

1. The subject stores the values, calls `notifyObservers`, and invokes each registered observer’s `update` method.
2. `ActionListener` reacts to an action such as button activation; `DocumentListener` reacts to text document insert/remove/change operations.
3. The callback blocks the EDT, which is also responsible for repainting and dispatching later events.
4. The `fx:id` must match the injected field, the controller class must match `fx:controller`, and the button action must match the controller method.
5. One-way binding updates target from source; bidirectional binding synchronizes changes from either side.
6. Use an observer contract when multiple or replaceable components need to react without the model depending on a concrete view class.
7. A filter captures on the way down; a handler runs at the target/bubbles upward. `consume()` prevents further propagation.
8. Name nonempty, email nonempty, email containing `@`, and bio length at most 150.
