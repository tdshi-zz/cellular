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

import de.study.app.controller.etc.Observer;
import de.study.app.model.Automaton;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

/**
 * Contains all logic for the Editor
 */
public class EditorController extends Observer {

    private static final Logger LOG = LogManager.getLogger(EditorController.class);
    private static final String AUTOMATA = "/automata";
    private static final String FILETYP = ".java";
    private final File userDirectoryPath = new File(System.getProperty("user.dir"));
    private final String actualFileName;
    private File saveFile;

    public EditorController(String initialName, String sourceCode) {
        this.actualFileName = initialName;
        instantiatingEditor(sourceCode);
    }

    public void saveToJavaFile(String text) {
        saveFile = new File(userDirectoryPath + AUTOMATA, actualFileName + FILETYP);
        try (FileWriter writer = new FileWriter(saveFile)) {
            writer.write(text);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    /**
     * Saves and compiles the modified java code.
     * If not successful it shows to the user the java compiler error messages in a new dialog box.
     *
     * @param text the modified code from the associated editor view
     * @return new instance of compiled class
     */
    public Automaton onCompile(String text) {
        saveToJavaFile(text);

        saveFile = new File(userDirectoryPath + AUTOMATA, actualFileName + FILETYP);

        File[] files1 = new File[]{saveFile};
        StringBuilder errorMessage = new StringBuilder();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> errorHandler = new DiagnosticCollector<>();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(new File(userDirectoryPath + AUTOMATA)));

            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files1));

            boolean successful = compiler.getTask(null, fileManager, errorHandler, null, null, compilationUnits1).call();

            if (!successful) {
                List<Diagnostic<? extends JavaFileObject>> diagnostics = errorHandler.getDiagnostics();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                    errorMessage.append(diagnostic.getMessage(null)).append("\n");
                    LOG.debug(errorMessage);
                }
            } else {
                try (URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("automata").toURI().toURL()})) {
                    Class<?> cls = classLoader.loadClass(actualFileName);
                    return (Automaton) cls.getDeclaredConstructor().newInstance();
                }
            }
            showDialogAfterCompilation(errorMessage.toString());
        } catch (Exception e) {
            LOG.error(e);
        }
        return null;
    }

    // ------------------------------------------------------------------------------------
    // Help Methods
    // ------------------------------------------------------------------------------------

    public String getActualAutomatonCode() {
        File infile = new File(userDirectoryPath + AUTOMATA, actualFileName + FILETYP);

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(infile))) {
            String line = reader.readLine();

            while (line != null) {
                content.append(line).append(System.lineSeparator());
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        return content.toString();
    }

    private void instantiatingEditor(String sourceCode) {

        saveFile = new File(userDirectoryPath + AUTOMATA, actualFileName + FILETYP);

        try (FileWriter writer = new FileWriter(saveFile)) {
            String newContent = sourceCode.replace("<NAME>", actualFileName);
            writer.write(newContent);
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void showDialogAfterCompilation(String dialogMessage) {
        if (dialogMessage.isEmpty()) {
            AlertController.alertPrompt(Alert.AlertType.INFORMATION, "Compilation", "Compilation successful!");
        } else {
            AlertController.alertPrompt(Alert.AlertType.INFORMATION, "Compilation", "Compilation not successful: \n" + dialogMessage);
        }
    }
}
