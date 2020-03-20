/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.demo;

import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.sld.view.app.AbstractSingleLineDiagramViewer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ClientSingleLineDiagramViewer extends AbstractSingleLineDiagramViewer {

    private NetworkStoreService service;

    private final ComboBox<UUID> comboBoxIds = new ComboBox<>();

    @Override
    public void init() {
        service = new NetworkStoreService("http://localhost:" + 8080 + "/");
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        BorderPane.setMargin(comboBoxIds, new Insets(3, 3, 3, 3));
        comboBoxIds.setMaxWidth(Double.MAX_VALUE);
        comboBoxIds.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> loadNetwork(newValue));
        comboBoxIds.getItems().setAll(new ArrayList<>(service.getNetworkIds().keySet()));
        if (!comboBoxIds.getItems().isEmpty()) {
            comboBoxIds.getSelectionModel().select(0);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        service.close();
    }

    protected Node createCasePane(Stage primaryStage) {
        return comboBoxIds;
    }

    private void loadNetwork(UUID uuid) {
        Network network = service.getNetwork(uuid);
        setNetwork(network);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
