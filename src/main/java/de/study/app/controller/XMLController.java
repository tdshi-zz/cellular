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

import de.study.app.model.Automaton;
import de.study.app.model.included.GameOfLifeAutomaton;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * XML Model, contains the logic for storing a population as a XML File
 */
public class XMLController {

    private static final File userHomeDirectoryPath = new File(System.getProperty("user.home"));
    private static final FileChooser fileChooser;
    private static final Logger LOG = LogManager.getLogger(XMLController.class);

    static {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(userHomeDirectoryPath);
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("*.xml", "*.xml");
        fileChooser.getExtensionFilters().add(filter);
    }

    private XMLController() {
        throw new IllegalStateException("Error 404 - You can't instantiate a Utility Class :'(");
    }

    // FYI: The following two methods could be implemented much easier, maybe i'll find someday time for that.
    public static void savePopulationAsXML(Automaton automaton, Stage specificStage) {
        fileChooser.setTitle("Store: Population (XML)");
        File saveNewFile = fileChooser.showSaveDialog(specificStage);
        if (saveNewFile != null) {
            if (!saveNewFile.getName().endsWith(".xml")) {
                saveNewFile = new File(saveNewFile.getAbsolutePath().concat(".xml"));
            }
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                // Root Element
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("model");
                doc.appendChild(rootElement);

                Element settings = doc.createElement("Settings");
                rootElement.appendChild(settings);

                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(String.valueOf(automaton.getName())));
                settings.appendChild(name);

                Element numberOfStates = doc.createElement("states");
                numberOfStates.appendChild(doc.createTextNode(String.valueOf(automaton.getNumberOfStates())));
                settings.appendChild(numberOfStates);

                Element isMoore = doc.createElement("isMoore");
                isMoore.appendChild(doc.createTextNode(String.valueOf(automaton.isMooreNeighborHood())));
                settings.appendChild(isMoore);

                Element rows = doc.createElement("rows");
                rows.appendChild(doc.createTextNode(String.valueOf(automaton.getNumberOfRows())));
                settings.appendChild(rows);

                Element cols = doc.createElement("cols");
                cols.appendChild(doc.createTextNode(String.valueOf(automaton.getNumberOfColumns())));
                settings.appendChild(cols);

                Element isTorus = doc.createElement("isTorus");
                isTorus.appendChild(doc.createTextNode(String.valueOf(automaton.isTorus())));
                settings.appendChild(isTorus);

                Element width = doc.createElement("width");
                width.appendChild(doc.createTextNode(String.valueOf(specificStage.getWidth())));
                settings.appendChild(width);

                Element heigt = doc.createElement("height");
                heigt.appendChild(doc.createTextNode(String.valueOf(specificStage.getHeight())));
                settings.appendChild(heigt);

                Element cellfield = doc.createElement("cellfield");
                rootElement.appendChild(cellfield);

                for (int i = 0; i < automaton.getNumberOfRows(); i++) {
                    for (int j = 0; j < automaton.getNumberOfColumns(); j++) {
                        Element cell;
                        cell = doc.createElement("cell" + i + j);
                        cell.setAttribute("row", String.valueOf(i));
                        cell.setAttribute("col", String.valueOf(j));
                        cell.appendChild(doc.createTextNode(String.valueOf(automaton.getSpecificCell(i, j).getState())));
                        cellfield.appendChild(cell);
                    }
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(saveNewFile);

                transformer.transform(source, result);

            } catch (Exception e) {
                LOG.error(e);
                AlertController.alertPrompt(Alert.AlertType.ERROR, "Error (Saving)", "Population konnte nicht als XML gespeichert werden. Bitte erneut versuchen.");
            }
        }
    }

    public static void loadXMLPopulation(Automaton automaton, Stage specificStage) {
        fileChooser.setTitle("Load: Population (XML)");
        File loadNewPopulationFile = fileChooser.showOpenDialog(specificStage);
        if (loadNewPopulationFile != null) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(loadNewPopulationFile);

                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("Settings");
                Node nNode = nList.item(0);
                NodeList n2List = doc.getElementsByTagName("cellfield");
                Node n2Node = n2List.item(0);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;
                    int rows = Integer.parseInt(eElement.getElementsByTagName("rows").item(0).getTextContent());
                    int cols = Integer.parseInt(eElement.getElementsByTagName("cols").item(0).getTextContent());
                    int states = Integer.parseInt(eElement.getElementsByTagName("states").item(0).getTextContent());
                    boolean moore = Boolean.parseBoolean(eElement.getElementsByTagName("isMoore").item(0).getTextContent());
                    boolean torus = Boolean.parseBoolean(eElement.getElementsByTagName("isTorus").item(0).getTextContent());

                    Automaton newAutomaton = new GameOfLifeAutomaton(rows, cols, states, moore, torus);

                    Element nodeElement = (Element) n2Node;
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            int value = Integer.parseInt(nodeElement.getElementsByTagName("cell" + i + j).item(0).getTextContent());
                            newAutomaton.setStateForCell(i, j, value);
                        }
                    }
                    automaton.changePopulationOnCellfield(newAutomaton.getCellField());
                }
            } catch (Exception e) {
                LOG.error(e);
                AlertController.alertPrompt(Alert.AlertType.ERROR, "Error (Loading)", "Ungültige Datei");
            }
        }
    }
}
