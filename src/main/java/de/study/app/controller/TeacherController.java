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

import de.study.app.controller.etc.RMIInterface;
import javafx.util.Pair;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * TeacherController implements the RMI server side for the role teacher
 */
class TeacherController extends UnicastRemoteObject implements RMIInterface {

    private final Queue<Pair<String, String>> submittedCodeQueue = new LinkedList<>();

    private final transient Registry registry;

    /**
     * Creates a server and rebinds it
     *
     * @throws RemoteException if not possible to create a registry and rebind it
     */
    public TeacherController() throws RemoteException {
        super();
        LocateRegistry.createRegistry(3579);
        registry = LocateRegistry.getRegistry(3579);
        registry.rebind("Server", this);
    }

    /**
     * Submit method for student
     *
     * @param name       dummy name
     * @param sourceCode dummy source code
     * @throws RemoteException if submitting fails
     */
    @Override
    public void sendToTeacher(String name, String sourceCode) throws RemoteException {
        Pair<String, String> codeNamePair = new Pair<>(name, sourceCode);
        submittedCodeQueue.add(codeNamePair);
    }

    public Optional<Pair<String, String>> loadStudentSubmissions() {
        return Optional.ofNullable(submittedCodeQueue.poll());
    }

    public void closeRegistry() throws NoSuchObjectException {
        if (registry != null) {
            UnicastRemoteObject.unexportObject(registry, true);
        }
    }
}
