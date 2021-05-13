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
package de.study.app.view;

import de.study.app.controller.MenuController;
import de.study.app.controller.PopulationPanelController;
import de.study.app.controller.SerialController;
import de.study.app.controller.XMLController;
import de.study.app.controller.etc.ObservableInterface;
import de.study.app.model.Automaton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainViewPresenter implements Initializable, ObservableInterface {

    private static final String fxml = "/fxml/MainView.fxml";
    private ColorPicker[] colorPickers;
    private RadioButton[] radioButtons;
    private PopulationPanelController populationPanelController;
    private MenuController menuController;
    private String automatonName;
    private Stage stage;
    private int footerText = 1;
    private int automatonStates;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populationPanelController = new PopulationPanelController(resources);


        this.menuController = new MenuController(automatonName, populationPanelController, resources);

        this.populationPanelController.getSimulator().getAutomaton().add(this.populationPanelController.getPopulationPanel());
        this.populationPanelController.getSimulator().getMappingStates().add(this.populationPanelController.getPopulationPanel());
        this.populationPanelController.getSimulator().getAutomaton().add(this);
        this.populationPanelController.getSimulator().getAutomaton().add(menuController);
        this.populationPanelController.getSimulator().getMappingStates().add(menuController);
        scrollPane.setContent(populationPanelController.getPopulationPanel());

        initializeLeftColorPanel();
        automatonStates = populationPanelController.getSimulator().getAutomaton().getNumberOfStates();
        activateStateBoxes(automatonStates);
        createLanguageBinding();
        activateRole(resources);
    }

    // ------------------------------------------------------------------------------------
    // FXML Methods
    // ------------------------------------------------------------------------------------

    /**
     * Changing the language on other selected language
     */
    @FXML
    public void changeLanguage(ActionEvent event) {
        MenuItem selectLanguage = (MenuItem) event.getSource();
        Locale changedLocale = new Locale(selectLanguage.getId(), selectLanguage.getId().toUpperCase(Locale.ROOT));
        I18N.setLocale(changedLocale);

        changeLanguage();
    }

    @FXML
    void print() {
        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null) {
            job.showPrintDialog(stage);
            job.printPage(populationPanelController.getPopulationPanel());
            job.endJob();
        }
    }

    @FXML
    void nextGenerationCall() throws Throwable {
        populationPanelController.onNextGenerationCall();
    }

    @FXML
    void changeSizeCall() {
        menuController.changeFieldsizePrompt();
        changeFooterText(2);
    }

    @FXML
    void resetToZeroCall() {
        populationPanelController.onResetToZeroCall();
        changeFooterText(9);
    }

    @FXML
    void randomPopulationCall() {
        populationPanelController.onRandomPopulationCall();
        changeFooterText(3);
    }

    @FXML
    void torusCall() {
        populationPanelController.onTorusCall();
        changeFooterText(4);
    }

    @FXML
    void plusMagnifyingGlassCall() {
        populationPanelController.onZoomInCall();
    }

    @FXML
    void minusMagnifyingGlassCall() {
        populationPanelController.onZoomOutCall();
    }

    @FXML
    void colorPickerCall() {
        populationPanelController.onColorPickerCall(colorPickers);
    }

    @FXML
    void toggleSelectedChangeCall() {
        populationPanelController.selectedToggleChangedCall(radioButtons);
    }

    @FXML
    void contextCall(ContextMenuEvent contextMenuEvent) {
        populationPanelController.showCellfieldContextMenu(contextMenuEvent);
    }

    @FXML
    void mousePressedOnPopulationPanel(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_PRESSED) && mouseEvent.getButton().equals(MouseButton.PRIMARY))
            populationPanelController.mousePressedOnPopulationPanel(mouseEvent);
        stage = (Stage) (button1.getScene().getWindow());
    }

    @FXML
    void mouseReleasedOnPopulationPanel(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_RELEASED) && mouseEvent.getButton().equals(MouseButton.PRIMARY))
            populationPanelController.mouseReleasedOnPopulationPanel(mouseEvent);
    }

    @FXML
    public void startSimulationCall() {
        menuController.simulationStart();

        button11.setDisable(false);
        menu_sim_stop.setDisable(false);

        button10.setDisable(true);
        menu_sim_start.setDisable(true);
        changeFooterText(7);
    }

    @FXML
    public void stopSimulationCall() {
        menuController.simulationStop();

        button10.setDisable(false);
        menu_sim_start.setDisable(false);

        button11.setDisable(true);
        menu_sim_stop.setDisable(true);
        changeFooterText(8);
    }

    @FXML
    public void saveXML() {
        XMLController.savePopulationAsXML(this.populationPanelController.getSimulator().getAutomaton(), (Stage) scrollPane.getScene().getWindow());
    }

    @FXML
    public void loadXML() {
        XMLController.loadXMLPopulation(this.populationPanelController.getSimulator().getAutomaton(), (Stage) scrollPane.getScene().getWindow());
    }

    @FXML
    public void saveSerial() {
        SerialController.savePopulationAsSerialFile(this.populationPanelController.getSimulator().getAutomaton(), (Stage) scrollPane.getScene().getWindow());
    }

    @FXML
    public void loadSerial() {
        SerialController.loadPopulationFromSerialFile(this.populationPanelController.getSimulator().getAutomaton(), (Stage) scrollPane.getScene().getWindow());
    }

    @FXML
    public void sliderCall() {
        menuController.changeSimulationSpeed((int) slider.getValue());
    }

    @FXML
    public void loadAutomatonCall() {
        Optional<String> name = menuController.loadExistingAutomatonPrompt();
        name.ifPresent(s -> populationPanelController.getSimulator().getAutomaton().setName(s));
    }

    @FXML
    public void newAutomatonCall() {
        Optional<String> name = menuController.newAutomatonNamePrompt();
        name.ifPresent(s -> populationPanelController.getSimulator().getAutomaton().setName(s));
    }

    @FXML
    public void openEditorCall() {
        menuController.showAssociatedEditor(automatonName);
    }

    @FXML
    public void onCloseCall() {
        closeStageEditorAndDatabaseConnection();
    }

    @FXML
    public void saveSettingsCall() {
        menuController.saveSettings(stage);
    }

    @FXML
    public void restoreSettingsCall() {
        menuController.restoreSettingsPrompt(stage);
    }

    @FXML
    public void deleteSettingsCall() {
        menuController.deleteSettings();
    }

    @FXML
    public void firstStepsCall() {
        menuController.showUsedLicenses();
    }

    @FXML
    public void aboutCall() {
        menuController.showAboutWindow();
    }

    @FXML
    public void sendAutomatonCall() {
        menuController.sendStudentAutomatonToTeacher();
    }

    @FXML
    public void receiveAutomatonCall() {
        menuController.getStudentSubmission();
    }

    // ------------------------------------------------------------------------------------
    // Help Methods
    // ------------------------------------------------------------------------------------

    public void createMenuModel(String name) {
        this.automatonName = name;
    }

    private void initializeLeftColorPanel() {
        colorPickers = new ColorPicker[]{color1, color2, color3, color4, color5, color6, color7, color8, color9, color10};
        radioButtons = new RadioButton[]{radio1, radio2, radio3, radio4, radio5, radio6, radio7, radio8, radio9, radio10};
        ToggleGroup toggleGroup = new ToggleGroup();
        for (RadioButton buttons : radioButtons) {
            buttons.setToggleGroup(toggleGroup);
        }
    }

    public void activateStateBoxes(int states) {
        automatonStates = states;
        Platform.runLater(() -> {
            Color[] array = populationPanelController.getSimulator().getMappingStates().getColorArray();
            for (int i = 0; i < automatonStates; i++) {
                radioButtons[i].setVisible(true);
                colorPickers[i].setValue(Color.color(array[i].getRed(), array[i].getGreen(), array[i].getBlue()));
                colorPickers[i].setVisible(true);
            }
        });
    }

    public void deActivateStateBoxes(int states) {
        automatonStates = states;
        Platform.runLater(() -> {
            for (int i = 9; i >= automatonStates; i--) {
                radioButtons[i].setVisible(false);
                colorPickers[i].setVisible(false);
            }
        });
    }

    public void closeStageEditorAndDatabaseConnection() {
        Stage editorStage = menuController.getAssociatedStage();
        editorStage.close();
        stage.close();
        menuController.closeDatabaseConnection();
    }

    @Override
    public void update(Object o) {
        int states = ((Automaton) o).getNumberOfStates();
        if (states >= automatonStates)
            activateStateBoxes(((Automaton) o).getNumberOfStates());
        else deActivateStateBoxes(((Automaton) o).getNumberOfStates());
    }

    /**
     * Creating the language binding for the tooltips and the menu (items)
     */
    private void createLanguageBinding() {
        button1.setTooltip(I18N.tooltipForKey("tooltip_new"));
        button2.setTooltip(I18N.tooltipForKey("tooltip_load"));
        button3.setTooltip(I18N.tooltipForKey("tooltip_size"));
        button4.setTooltip(I18N.tooltipForKey("tooltip_reset"));
        button5.setTooltip(I18N.tooltipForKey("tooltip_random"));
        button6.setTooltip(I18N.tooltipForKey("tooltip_print"));
        button7.setTooltip(I18N.tooltipForKey("resize_plus"));
        button8.setTooltip(I18N.tooltipForKey("resize_minus"));
        button9.setTooltip(I18N.tooltipForKey("tooltip_single_Step"));
        button10.setTooltip(I18N.tooltipForKey("tooltip_start"));
        button11.setTooltip(I18N.tooltipForKey("tooltip_stop"));
        toggleButton.setTooltip(I18N.tooltipForKey("tooltip_speed"));
        slider.setTooltip(I18N.tooltipForKey("tooltip_torus"));

        menu_sim_start.textProperty().bind(I18N.createStringBinding("start"));
        menu_sim_stop.textProperty().bind(I18N.createStringBinding("stop"));
        automaton_title.textProperty().bind(I18N.createStringBinding("automat"));
        menu_new.textProperty().bind(I18N.createStringBinding("new"));
        menu_load.textProperty().bind(I18N.createStringBinding("load"));
        menu_editor.textProperty().bind(I18N.createStringBinding("editor"));
        menu_close.textProperty().bind(I18N.createStringBinding("close"));
        population_title.textProperty().bind(I18N.createStringBinding("population"));
        menu_size.textProperty().bind(I18N.createStringBinding("change_size"));
        menu_delete.textProperty().bind(I18N.createStringBinding("delete"));
        menu_create.textProperty().bind(I18N.createStringBinding("create"));
        menu_toggle.textProperty().bind(I18N.createStringBinding("torus"));
        menu_bigger.textProperty().bind(I18N.createStringBinding("resize_plus"));
        menu_smaller.textProperty().bind(I18N.createStringBinding("resize_minus"));
        save_title.textProperty().bind(I18N.createStringBinding("save"));
        menu_xml_save.textProperty().bind(I18N.createStringBinding("xml"));
        menu_serial_save.textProperty().bind(I18N.createStringBinding("serial"));
        load_title.textProperty().bind(I18N.createStringBinding("load"));
        menu_xml_load.textProperty().bind(I18N.createStringBinding("xml"));
        menu_serial_load.textProperty().bind(I18N.createStringBinding("serial"));
        menu_print.textProperty().bind(I18N.createStringBinding("print"));
        simulation_title.textProperty().bind(I18N.createStringBinding("simul"));
        menu_sim_step.textProperty().bind(I18N.createStringBinding("step"));
        settings_title.textProperty().bind(I18N.createStringBinding("settings"));
        saveSetting.textProperty().bind(I18N.createStringBinding("save"));
        restoreSetting.textProperty().bind(I18N.createStringBinding("restore"));
        deleteSetting.textProperty().bind(I18N.createStringBinding("delete"));
        language.textProperty().bind(I18N.createStringBinding("languag"));
        en.textProperty().bind(I18N.createStringBinding("english"));
        de.textProperty().bind(I18N.createStringBinding("german"));
        help_title.textProperty().bind(I18N.createStringBinding("help"));
        menu_first_steps.textProperty().bind(I18N.createStringBinding("first_steps"));
        menu_about.textProperty().bind(I18N.createStringBinding("about"));
        communication.textProperty().bind(I18N.createStringBinding("communication"));
        sendAutomaton.textProperty().bind(I18N.createStringBinding("send_automaton"));
        receiveAutomaton.textProperty().bind(I18N.createStringBinding("receive_automaton"));

        footer.setText(I18N.get("footer_" + 1));
    }

    private void changeLanguage() {
        stage.setTitle(I18N.createStringBinding("stage_title").get());
        footer.setText(I18N.createStringBinding("footer_" + footerText).get());
        menuController.changeMenuLanguage();
    }

    /**
     * Checking the role of the client based on attribute in the resource bundle
     *
     * @param resourceBundle the loaded resource bundle
     */
    private void activateRole(ResourceBundle resourceBundle) {
        String role = resourceBundle.getString("role");
        if (role.equals("teacher")) {
            receiveAutomaton.setVisible(true);
        } else if (role.equals("student")) {
            sendAutomaton.setVisible(true);
            if (!menuController.isTeacherOnlineCheck()) {
                sendAutomaton.setDisable(true);
            }
        }
    }

    public void saveStageAfterInitialisation() {
        stage = (Stage) button1.getScene().getWindow();
        stage.setOnCloseRequest(e -> closeStageEditorAndDatabaseConnection());
    }

    private void changeFooterText(int value) {
        footerText = value;
        footer.setText(I18N.createStringBinding("footer_" + footerText).get());
    }

    // ------------------------------------------------------------------------------------
    // FXML Declaration
    // ------------------------------------------------------------------------------------

    @FXML
    Label footer;
    @FXML
    Button button1;
    @FXML
    Button button2;
    @FXML
    Button button3;
    @FXML
    Button button4;
    @FXML
    Button button5;
    @FXML
    Button button6;
    @FXML
    Button button7;
    @FXML
    Button button8;
    @FXML
    Button button9;
    @FXML
    Button button10;
    @FXML
    Button button11;
    @FXML
    ToggleButton toggleButton;
    @FXML
    Slider slider;
    @FXML
    ScrollPane scrollPane;
    @FXML
    RadioButton radio1;
    @FXML
    RadioButton radio2;
    @FXML
    RadioButton radio3;
    @FXML
    RadioButton radio4;
    @FXML
    RadioButton radio5;
    @FXML
    RadioButton radio6;
    @FXML
    RadioButton radio7;
    @FXML
    RadioButton radio8;
    @FXML
    RadioButton radio9;
    @FXML
    RadioButton radio10;
    @FXML
    ColorPicker color1;
    @FXML
    ColorPicker color2;
    @FXML
    ColorPicker color3;
    @FXML
    ColorPicker color4;
    @FXML
    ColorPicker color5;
    @FXML
    ColorPicker color6;
    @FXML
    ColorPicker color7;
    @FXML
    ColorPicker color8;
    @FXML
    ColorPicker color9;
    @FXML
    ColorPicker color10;
    @FXML
    MenuItem menu_sim_start;
    @FXML
    MenuItem menu_sim_stop;
    @FXML
    Menu automaton_title;
    @FXML
    MenuItem menu_new;
    @FXML
    MenuItem menu_load;
    @FXML
    MenuItem menu_editor;
    @FXML
    MenuItem menu_close;
    @FXML
    Menu population_title;
    @FXML
    MenuItem menu_size;
    @FXML
    MenuItem menu_delete;
    @FXML
    MenuItem menu_create;
    @FXML
    MenuItem menu_toggle;
    @FXML
    MenuItem menu_bigger;
    @FXML
    MenuItem menu_smaller;
    @FXML
    MenuItem save_title;
    @FXML
    MenuItem menu_xml_save;
    @FXML
    MenuItem menu_serial_save;
    @FXML
    MenuItem load_title;
    @FXML
    MenuItem menu_xml_load;
    @FXML
    MenuItem menu_serial_load;
    @FXML
    MenuItem menu_print;
    @FXML
    Menu simulation_title;
    @FXML
    MenuItem menu_sim_step;
    @FXML
    Menu settings_title;
    @FXML
    MenuItem saveSetting;
    @FXML
    MenuItem restoreSetting;
    @FXML
    MenuItem deleteSetting;
    @FXML
    Menu language;
    @FXML
    MenuItem en;
    @FXML
    MenuItem de;
    @FXML
    Menu help_title;
    @FXML
    MenuItem menu_first_steps;
    @FXML
    MenuItem menu_about;
    @FXML
    MenuItem receiveAutomaton;
    @FXML
    MenuItem sendAutomaton;
    @FXML
    Menu communication;
}
