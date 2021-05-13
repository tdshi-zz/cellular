/*
 * Cellular Automaton - study project winter term 2020/2021
 * Copyright (C) 2021 tdshi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. See LICENSE File in the main directory. If not, see <https://www.gnu.org/licenses/>.
 */
package de.study.app.controller;

import de.study.app.controller.etc.ObservableInterface;
import de.study.app.model.Automaton;
import de.study.app.model.ColorMappingStates;
import de.study.app.settings.Settings;
import de.study.app.view.EditorViewPresenter;
import de.study.app.view.I18N;
import de.study.app.view.MainViewPresenter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.sleep;

public class MenuController implements ObservableInterface {

    private static final Logger LOG = LogManager.getLogger(MenuController.class);
    private static final String AUTOMATA = "/automata";

    private static final String SQLORDER = "DELETE FROM settings WHERE settings.name = ?";
    private static final String CREATETABLE = "CREATE TABLE settings (id INT NOT NULL GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL," +
            "x_position_screen DOUBLE NOT NULL,y_position_screen DOUBLE NOT NULL, x_width DOUBLE NOT NULL ,y_height DOUBLE NOT NULL, " +
            "zoomfactor INT NOT NULL,simulation_speed INT NOT NULL,PRIMARY KEY (id))";
    private static final String SAVESETTINGS = "INSERT INTO settings ( name, x_position_screen, y_position_screen," +
            "x_width, y_height, zoomfactor, simulation_speed ) VALUES (?,?,?,?,?,?,?)";
    private static final String UPDATESETTINGS = "UPDATE settings SET x_position_screen = ?, y_position_screen = ?, x_width = ? , y_height = ?, zoomfactor = ?, simulation_speed = ? WHERE settings.name = ?";
    public static final String JAVA = ".java";

    private final PopulationPanelController populationPanelController;
    private final String userDirectoryPath = System.getProperty("user.dir");
    private TeacherController teacherController;
    private StudentController studentController;
    private Connection databaseConnection;
    private Stage associatedStage;
    private volatile int simulationSpeed = 175;
    private volatile boolean simulationRunning = false;
    private EditorViewPresenter associatedEditor;
    private ResourceBundle messages;


    public MenuController(String name, PopulationPanelController panel, ResourceBundle messages) {
        this.populationPanelController = panel;
        this.messages = messages;
        communicationInitialization(messages);
        createNewFile(name, "");
        createEditorView(name);
        checkIfDatabaseExists();
    }

    public void simulationStart() {
        Runnable runnable = this::run;
        new Thread(runnable).start();
    }

    public void simulationStop() {
        simulationRunning = false;
    }

    public void changeSimulationSpeed(int value) {
        if (value < 50 && value > 0) {
            if (value > 5)
                simulationSpeed = (175 * value) / (value / 2);
            else
                simulationSpeed = (175 * value) / (value - 1);
        } else if (value > 50 && value <= 100) {
            simulationSpeed = (175 * value) / (value * 2);
        }
    }

    /**
     * Creates on call the view and styles the view, but does not show the stage
     *
     * @param name model name for title bar
     */
    private void createEditorView(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditorView.fxml"), messages);
            associatedEditor = new EditorViewPresenter(name, populationPanelController, loadContextFromTXTFile("/dummy/DefaultAutomaton.txt"));

            loader.setController(associatedEditor);
            BorderPane newEditorRoot = loader.load();

            associatedEditor.loadActualCodeIntoTextArea();
            associatedEditor.onCompileCall();
            Scene scene1 = new Scene(newEditorRoot);

            associatedStage = new Stage();

            JMetro jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene1);
            associatedStage.setScene(scene1);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public Optional<String> loadExistingAutomatonPrompt() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(messages.getString("load_automaton_title"));

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("java Files (*.java)", "*.java");
        fileChooser.getExtensionFilters().add(extensionFilter);

        File userDirectory = new File(System.getProperty("user.dir") + AUTOMATA);
        fileChooser.setInitialDirectory(userDirectory);

        File chosenFile = fileChooser.showOpenDialog(null);

        String name = null;
        if (chosenFile != null) {
            createNewStageAndShow(chosenFile.getName().substring(0, chosenFile.getName().length() - 5), "");
            name = chosenFile.getName();
        }
        return Optional.ofNullable(name);
    }

    public Optional<String> newAutomatonNamePrompt() {
        Optional<String> result = newNameAlert(messages.getString("new_automaton_title_prompt"));
        result.ifPresent(a -> createNewStageAndShow(result.get(), ""));
        return result;
    }

    private Optional<String> newNameAlert(String title) {
        Optional<String> theInputName;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        TextField newAutomatonName = new TextField(messages.getString("new_automaton_default_input"));
        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(newAutomatonName, 1, 0);

        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty()
                .bind(Bindings.createBooleanBinding(
                        () -> !newAutomatonName.getText().matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*"),
                        newAutomatonName.textProperty()));
        Platform.runLater(newAutomatonName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return newAutomatonName.getText();
            }
            return null;
        });
        theInputName = dialog.showAndWait();

        return theInputName;
    }

    private void createNewStageAndShow(String name, String content) {
        StringBuilder newName = new StringBuilder(name);
        while (checkIfFileExists(name)) {
            newName.append(name);
        }
        try {
            createNewFile(newName.toString(), content);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), messages);

            MainViewPresenter childFromActualModel = new MainViewPresenter();
            childFromActualModel.createMenuModel(newName.toString());

            loader.setController(childFromActualModel);
            BorderPane newAutomatonRoot = loader.load();

            Scene scene = new Scene(newAutomatonRoot);
            Stage newStage = new Stage();

            JMetro jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);

            newStage.setScene(scene);
            newStage.setTitle(newName.toString());
            newStage.show();
            childFromActualModel.saveStageAfterInitialisation();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    // ------------------------------------------------------------------------------------
    // Database Methods
    // ------------------------------------------------------------------------------------

    public void saveSettings(Stage window) {
        Optional<String> automatonName = newNameAlert(messages.getString("settings_header_0"));

        if (automatonName.isPresent()) {
            Settings settings = new Settings(automatonName.get(), window.getX(), window.getY(), window.getWidth(), window.getHeight(), populationPanelController.getZoomCount(), simulationSpeed);
            String check = "SELECT * FROM settings WHERE settings.name = ?";
            ResultSet checkSet;
            try (PreparedStatement preparedStatementCheck = databaseConnection.prepareStatement(check)) {

                preparedStatementCheck.setString(1, automatonName.get());
                preparedStatementCheck.execute();
                checkSet = preparedStatementCheck.getResultSet();
                /* Check if Settings is already included */
                if (checkSet.next()) {
                    try (PreparedStatement update = databaseConnection.prepareStatement(UPDATESETTINGS)) {
                        update.setDouble(1, settings.getXPosition());
                        update.setDouble(2, settings.getYPosition());
                        update.setDouble(3, settings.getWindowWidth());
                        update.setDouble(4, settings.getWindowHeight());
                        update.setDouble(5, settings.getZoomCount());
                        update.setInt(6, settings.getSimulationSpeed());
                        update.setString(7, settings.getName());
                        update.executeUpdate();
                        databaseConnection.commit();
                    }
                }
            } catch (SQLException e) {
                LOG.error(e);
            }
            try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(SAVESETTINGS)) {
                preparedStatement.setString(1, settings.getName());
                preparedStatement.setDouble(2, settings.getXPosition());
                preparedStatement.setDouble(3, settings.getYPosition());
                preparedStatement.setDouble(4, settings.getWindowWidth());
                preparedStatement.setDouble(5, settings.getWindowHeight());
                preparedStatement.setDouble(6, settings.getZoomCount());
                preparedStatement.setInt(7, settings.getSimulationSpeed());

                preparedStatement.execute();
                databaseConnection.commit();
            } catch (SQLException e) {
                LOG.error(e);
            }
        }

    }

    /**
     * Prompts the user to choose from included settings, selected by the name
     */
    public void restoreSettingsPrompt(Stage window) {
        String sql = "SELECT * FROM settings WHERE settings.name = ?";
        try (PreparedStatement query = databaseConnection.prepareStatement(sql)) {
            Optional<String> theName = getSettingList(messages.getString("settings_header_1"));
            if (theName.isPresent()) {

                query.setString(1, theName.get());

                ResultSet checkIfPresent = query.executeQuery();

                while (checkIfPresent.next()) {
                    /* Settings the window parameter */
                    window.setX(Double.parseDouble(checkIfPresent.getString("x_position_screen")));
                    window.setY(Double.parseDouble(checkIfPresent.getString("y_position_screen")));
                    window.setWidth(Double.parseDouble(checkIfPresent.getString("x_width")));
                    window.setHeight(Double.parseDouble(checkIfPresent.getString("y_height")));

                    /* Settings the model parameter */
                    this.simulationSpeed = Integer.parseInt(checkIfPresent.getString("simulation_speed"));
                    populationPanelController.getSimulator().getAutomaton().setZoomCount(Integer.parseInt(checkIfPresent.getString("zoomfactor")));
                    populationPanelController.setZoomCount(Integer.parseInt(checkIfPresent.getString("zoomfactor")));
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private Optional<String> getSettingList(String headerText) {
        AtomicReference<String> theChoice = new AtomicReference<>();
        String query = "SELECT name FROM settings";
        try (Statement statement = databaseConnection.createStatement()) {

            ResultSet rs = statement.executeQuery(query);
            if (rs.next()) {
                ChoiceDialog<String> dialog = new ChoiceDialog<>();
                dialog.setTitle(messages.getString("settings_title"));
                dialog.setHeaderText(headerText);
                dialog.setContentText(messages.getString("settings_text"));

                dialog.getItems().add(rs.getString("name"));
                while (rs.next()) {
                    dialog.getItems().add(rs.getString("name"));
                }
                Optional<String> result = dialog.showAndWait();

                result.ifPresent(a -> theChoice.set(result.get()));
            } else
                AlertController.alertPrompt(Alert.AlertType.INFORMATION, messages.getString("settings_title"), messages.getString("settings_text_empty"));
        } catch (Exception e) {
            LOG.error(e);
        }

        return Optional.ofNullable(theChoice.get());
    }

    public void deleteSettings() {
        try {
            Optional<String> theName = getSettingList(messages.getString("settings_header_2"));
            if (theName.isPresent()) {
                try (PreparedStatement sqldelete = databaseConnection.prepareStatement(SQLORDER)) {
                    sqldelete.setString(1, theName.get());
                    sqldelete.execute();
                    databaseConnection.commit();
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void checkIfDatabaseExists() {
        try {
            databaseConnection = DriverManager.getConnection("jdbc:derby:automatonDB;create=true");
            databaseConnection.setAutoCommit(false);

            try (Statement statement = databaseConnection.createStatement()) {
                ResultSet res = databaseConnection.getMetaData().getTables(null, null, "SETTINGS", null);

                // Creates table settings if database is newly created
                if (!res.next()) {
                    statement.execute(CREATETABLE);
                    databaseConnection.commit();
                }
            }
        } catch (SQLException e) {
            LOG.error("<--- The following error is caused because only one derby connection is allowed and you started a second connection. --->");
            LOG.error(e);
        }
    }

// ------------------------------------------------------------------------------------
// Help Methods
// ------------------------------------------------------------------------------------

    private boolean checkIfFileExists(String name) {
        File existence = new File(userDirectoryPath + AUTOMATA, name + JAVA);
        return existence.exists();
    }

    private void createNewFile(String name, String content) {
        File dir = new File(userDirectoryPath + AUTOMATA);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File outputFile = new File(userDirectoryPath + AUTOMATA, name + JAVA);
        try (FileWriter writer = new FileWriter(outputFile)) {

            String oldContent = loadContextFromTXTFile("/dummy/DefaultAutomaton.txt");
            String newContent = oldContent.replace("<NAME>", name);
            if (!content.isEmpty())
                newContent = content;
            writer.write(newContent);
        } catch (IOException e) {
            LOG.error(e);
        }

    }

    public void showAssociatedEditor(String name) {
        try {
            associatedStage.setTitle(name);
            associatedStage.show();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void changeFieldsizePrompt() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(messages.getString("new_size_title"));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField row = new TextField(messages.getString("new_row"));
        TextField col = new TextField(messages.getString("col"));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));

        gridPane.add(I18N.labelForKey("row"), 0, 0);
        gridPane.add(row, 1, 0);
        gridPane.add(I18N.labelForKey("col"), 2, 0);
        gridPane.add(col, 3, 0);

        dialog.getDialogPane().setContent(gridPane);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty()
                .bind(Bindings.createBooleanBinding(
                        () -> !row.getText().matches("^(^500|^4[0-9][0-9]$|^3[0-9][0-9]$|^2[0-9][0-9]$|^1[0-9][0-9]$|^[1-9][0-9]$|[1-9]$)$") ||
                                !col.getText().matches("^(^500|^4[0-9][0-9]$|^3[0-9][0-9]$|^2[0-9][0-9]$|^1[0-9][0-9]$|^[1-9][0-9]$|[1-9]$)$"),
                        row.textProperty(),
                        col.textProperty()
                ));

        Platform.runLater(row::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) return new Pair<>(row.getText(), col.getText());
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(this::changeSizeInSimulator);
    }

    private void changeSizeInSimulator(Pair<String, String> pair) {
        populationPanelController.getSimulator().getAutomaton().changeCellfieldSizeAndCopyValues(Integer.parseInt(pair.getKey()), Integer.parseInt(pair.getValue()));
    }

    private void communicationInitialization(ResourceBundle message) {
        if (message.getString("role").equals("student")) {
            studentController = new StudentController();
        } else if (message.getString("role").equals("teacher")) {
            try {
                teacherController = new TeacherController();
            } catch (RemoteException e) {
                LOG.error(e);
            }
        }
    }

    public boolean isTeacherOnlineCheck() {
        return studentController.isConnected();
    }

    public void showUsedLicenses() {
        String theText = loadContextFromTXTFile("/about/licenses.txt");
        AlertController.alertPrompt(Alert.AlertType.INFORMATION, messages.getString("license_title"), theText);
    }

    private String loadContextFromTXTFile(String name) {
        String line;
        StringBuilder loadedText = new StringBuilder();
        BufferedReader bufferReader;
        try {
            InputStream inputStream = getClass().getResourceAsStream(name);
            InputStreamReader reader = new InputStreamReader(inputStream);
            bufferReader = new BufferedReader(reader);
            line = bufferReader.readLine();

            while (line != null) {
                loadedText.append(line).append(System.lineSeparator());
                line = bufferReader.readLine();
            }

        } catch (IOException e) {
            LOG.error(e);
        }
        return loadedText.toString();
    }

    public void showAboutWindow() {
        String theText = loadContextFromTXTFile("/about/about.txt");
        AlertController.alertPrompt(Alert.AlertType.INFORMATION, messages.getString("about_title"), theText);
    }

    public void changeMenuLanguage() {
        messages = I18N.getBundle();
    }

    public Stage getAssociatedStage() {
        return associatedStage;
    }

    /**
     * Updates itself when the model or ColorMappingStates has changed
     *
     * @param obj model object or ColorMappingStates object
     */
    @Override
    public void update(Object obj) {
        if (obj instanceof Automaton)
            this.populationPanelController.getSimulator().setAutomaton((Automaton) obj);
        else if (obj instanceof ColorMappingStates)
            this.populationPanelController.getSimulator().setMappingStates((ColorMappingStates) obj);
    }

    public void closeDatabaseConnection() {
        try {
            databaseConnection.commit();
            databaseConnection.close();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");

            LOG.info("Database was successfully shutdown.");
            teacherController.closeRegistry();
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void getStudentSubmission() {
        if (teacherController != null) {
            Optional<Pair<String, String>> studentAutomaton = teacherController.loadStudentSubmissions();
            if (studentAutomaton.isPresent())
                createNewStageAndShow(studentAutomaton.get().getKey(), studentAutomaton.get().getValue());
            else
                AlertController.alertPrompt(Alert.AlertType.INFORMATION, messages.getString("teacher_alert_title"), messages.getString("teacher_alert_message"));
        }
    }

    public void sendStudentAutomatonToTeacher() {
        try {
            studentController.send(associatedEditor.getName(), associatedEditor.getTextAreaContent());
        } catch (RemoteException e) {
            LOG.error(e);
        }
    }

    private void run() {
        if (!simulationRunning)
            simulationRunning = true;
        while (simulationRunning) {
            try {
                populationPanelController.onNextGenerationCall();
                sleep(simulationSpeed);
            } catch (Throwable e) {
                LOG.debug(e);
                LOG.error("Something went wrong in the run method.");
            }
        }
    }
}
