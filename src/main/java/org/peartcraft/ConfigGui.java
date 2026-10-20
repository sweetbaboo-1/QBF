package org.peartcraft;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.sandrohc.schematic4j.schematic.LitematicaSchematic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigGui extends Application {

    private static final String BLOCK_PREFIX = "minecraft:";
    public static final String[] DEFAULT_BLOCK_BLACKLIST = {
        "barrel",
        "bee_nest",
        "beehive",
        "blast_furnace",
        "brewing_stand",
        "chest",
        "crying_obsidian",
        "dispenser",
        "ender_chest",
        "furnace",
        "grindstone",
        "hopper",
        "lectern",
        "obsidian",
        "reinforced_deepslate",
        "sculk_catalyst",
        "sculk_sensor",
        "sculk_shrieker",
        "smoker",
        "sponge",
        "spawner",
        "trapped_chest",
        "trial_spawner",
        "vault"
    };

    private final CompletableFuture<ConfigData> future = new CompletableFuture<>();
    private final Set<String> blockSet = new LinkedHashSet<>(List.of(DEFAULT_BLOCK_BLACKLIST));

    @Override
    public void start(Stage primaryStage) {
        Preferences prefs = Preferences.userNodeForPackage(ConfigGui.class);

        // Declare selected file property
        ObjectProperty<LitematicaSchematic> selectedFile = new SimpleObjectProperty<>();

        Label fileLabel = new Label("No file selected");
        // Keep the main window compact. Region selection lives in a separate dialog.
        Set<Integer> selectedRegionIndices = new LinkedHashSet<>();
        Button chooseRegions = new Button("Choose Regions...");
        chooseRegions.setDisable(true);

        // Coordinates
        Label section1 = new Label("Coordinates");
        TextField x1Field = new TextField(), y1Field = new TextField(), z1Field = new TextField();
        TextField x2Field = new TextField(), y2Field = new TextField(), z2Field = new TextField();

        x1Field.setPromptText("x1");
        y1Field.setPromptText("y1");
        z1Field.setPromptText("z1");
        x2Field.setPromptText("x2");
        y2Field.setPromptText("y2");
        z2Field.setPromptText("z2");

        // Restore saved coords
        x1Field.setText(prefs.get("x1", ""));
        y1Field.setText(prefs.get("y1", ""));
        z1Field.setText(prefs.get("z1", ""));
        x2Field.setText(prefs.get("x2", ""));
        y2Field.setText(prefs.get("y2", ""));
        z2Field.setText(prefs.get("z2", ""));

        // Restrict to integers
        Stream.of(x1Field, y1Field, z1Field, x2Field, y2Field, z2Field).forEach(this::restrictToIntegers);

        VBox corner1Box = new VBox(5, new Label("Corner 1"), new HBox(5, new Label("X:"), x1Field), new HBox(5, new Label("Y:"), y1Field), new HBox(5, new Label("Z:"), z1Field));

        VBox corner2Box = new VBox(5, new Label("Corner 2"), new HBox(5, new Label("X:"), x2Field), new HBox(5, new Label("Y:"), y2Field), new HBox(5, new Label("Z:"), z2Field));

        HBox coordsBox = new HBox(40, corner1Box, corner2Box);

        Label section2 = new Label("Waypoint Type");
        ComboBox<String> providerDropdown = new ComboBox<>();
        providerDropdown.getItems().addAll(MapMod.VOXEL.name(), MapMod.XAERO.name());
        String savedProvider = prefs.get("provider", null);
        if (savedProvider != null && providerDropdown.getItems().contains(savedProvider)) {
            providerDropdown.getSelectionModel().select(savedProvider);
        } else {
            providerDropdown.getSelectionModel().selectFirst();
        }

        Label section3 = new Label("Dimension");
        ComboBox<String> dimensionDropdown = new ComboBox<>();
        dimensionDropdown.getItems().addAll(Dimension.OVERWORLD.getId(), Dimension.NETHER.getId(), Dimension.END.getId());
        String savedDimension = prefs.get("dimension", null);
        if (savedDimension != null && dimensionDropdown.getItems().contains(savedDimension)) {
            dimensionDropdown.getSelectionModel().select(savedDimension);
        } else {
            dimensionDropdown.getSelectionModel().selectFirst();
        }

        Label section4 = new Label("Blacklist");
        TextField blockInput = new TextField();
        blockInput.setPromptText("Add block name");

        ListView<String> blockListView = new ListView<>();
        ObservableList<String> blockList = FXCollections.observableArrayList();
        blockList.setAll(blockSet);
        blockListView.setItems(blockList);
        blockListView.setPrefHeight(85);
        blockListView.setMinHeight(75);
        blockListView.setMaxHeight(95);

        blockInput.setOnAction(e -> {
            String input = blockInput.getText().trim().toLowerCase();
            if (!input.isEmpty() && blockSet.add(input)) {
                blockList.setAll(blockSet);
                blockInput.clear();
            }
        });

        VBox blockBox = getBlacklistVBox(blockInput, blockList, blockListView);

        Button loadLitematic = new Button("Load Litematic");
        Button submitButton = new Button("Submit");
        submitButton.setDisable(true);

        loadLitematic.setOnAction(e -> {
            LitematicaSchematic litematic = LitematicSelector.pickFile(primaryStage);
            if (litematic != null) {
                selectedFile.set(litematic);
                fileLabel.setText(litematic.name());
                selectedRegionIndices.clear();
                LitematicaSchematic.Region[] regions = litematic.regions();
                for (int i = 0; i < regions.length; i++) {
                    selectedRegionIndices.add(i);
                }
                chooseRegions.setDisable(regions.length == 0);
                chooseRegions.setText("Regions (" + regions.length + "/" + regions.length + ")");
                submitButton.setDisable(regions.length == 0);
            }
        });

        chooseRegions.setOnAction(e -> {
            LitematicaSchematic litematic = selectedFile.get();
            if (litematic == null) return;
            LitematicaSchematic.Region[] regions = litematic.regions();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(primaryStage);
            dialog.setTitle("Choose Regions");
            dialog.setHeaderText("Choose which Litematica regions to export");

            List<CheckBox> options = new ArrayList<>();
            VBox choices = new VBox(7);
            for (int i = 0; i < regions.length; i++) {
                LitematicaSchematic.Region region = regions[i];
                String name = region.name == null || region.name.isBlank()
                        ? "Region " + (i + 1) : region.name;
                CheckBox option = new CheckBox(String.format(
                        "%d. %s (%d x %d x %d)",
                        i + 1, name, Math.abs((long) region.size.x),
                        Math.abs((long) region.size.y), Math.abs((long) region.size.z)));
                option.setTooltip(new Tooltip(String.format(
                        "Relative origin: (%d, %d, %d) | Signed size: (%d, %d, %d)",
                        region.position.x, region.position.y, region.position.z,
                        region.size.x, region.size.y, region.size.z)));
                option.setSelected(selectedRegionIndices.contains(i));
                options.add(option);
                choices.getChildren().add(option);
            }

            Button selectAll = new Button("Select all");
            Button selectNone = new Button("Select none");
            selectAll.setOnAction(event -> options.forEach(option -> option.setSelected(true)));
            selectNone.setOnAction(event -> options.forEach(option -> option.setSelected(false)));

            ScrollPane regionScroll = new ScrollPane(choices);
            regionScroll.setFitToWidth(true);
            regionScroll.setPrefViewportWidth(440);
            regionScroll.setPrefViewportHeight(190);

            VBox content = new VBox(10, new Label(
                    "Unselected regions are skipped. World coordinates stay unchanged."),
                    new HBox(8, selectAll, selectNone), regionScroll);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            Runnable updateOk = () -> ok.setDisable(
                    options.stream().noneMatch(CheckBox::isSelected));
            options.forEach(option -> option.selectedProperty().addListener(
                    (obs, before, after) -> updateOk.run()));
            updateOk.run();

            if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                selectedRegionIndices.clear();
                for (int i = 0; i < options.size(); i++) {
                    if (options.get(i).isSelected()) selectedRegionIndices.add(i);
                }
                chooseRegions.setText(String.format("Regions (%d/%d)",
                        selectedRegionIndices.size(), regions.length));
            }
        });

        submitButton.setOnAction(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int z1 = Integer.parseInt(z1Field.getText());

                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());
                int z2 = Integer.parseInt(z2Field.getText());

                String dimension = dimensionDropdown.getValue();
                String provider = providerDropdown.getValue();

                if (selectedRegionIndices.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING,
                            "Select at least one region to export.").showAndWait();
                    return;
                }

                // Persist inputs for next session
                prefs.put("x1", x1Field.getText());
                prefs.put("y1", y1Field.getText());
                prefs.put("z1", z1Field.getText());
                prefs.put("x2", x2Field.getText());
                prefs.put("y2", y2Field.getText());
                prefs.put("z2", z2Field.getText());
                prefs.put("dimension", dimension);
                prefs.put("provider", provider);

                Set<String> newBlockSet = blockSet.stream().map(s -> BLOCK_PREFIX + s).collect(Collectors.toCollection(LinkedHashSet::new));

                ConfigData config = new ConfigData(x1, y1, z1, x2, y2, z2,
                        dimension, provider, newBlockSet, selectedFile.get(), selectedRegionIndices);
                future.complete(config);
                primaryStage.close();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid coordinates").showAndWait();
            }
        });

        VBox fileSection = new VBox(5, new Label("Litematic File"), fileLabel,
                new HBox(8, loadLitematic, chooseRegions));

        VBox root = new VBox(15, section1, coordsBox, section2, providerDropdown,
                section3, dimensionDropdown, section4, blockBox, fileSection, submitButton);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("BlockFinder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox getBlacklistVBox(TextField blockInput, ObservableList<String> blockList, ListView<String> blockListView) {
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            String newBlock = blockInput.getText().trim();
            if (!newBlock.isEmpty() && blockSet.add(newBlock)) {
                blockList.setAll(blockSet);
                blockInput.clear();
            }
        });

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            String selected = blockListView.getSelectionModel().getSelectedItem();
            if (selected != null && blockSet.remove(selected)) {
                blockList.setAll(blockSet);
            }
        });

        Button removeAllButton = new Button("Remove All");
        removeAllButton.setOnAction(e -> {
            blockSet.clear();
            blockList.setAll(blockSet);
        });

        blockListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                String selected = blockListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    blockSet.remove(selected);
                    blockListView.getItems().setAll(blockSet);
                }
            }
        });

        HBox blockControls = new HBox(10, blockInput, addButton, removeButton, removeAllButton);
        return new VBox(5, blockControls, blockListView);
    }

    private void restrictToIntegers(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("-?\\d*")) {
                field.setText(oldVal);
            }
        });
    }

    public CompletableFuture<ConfigData> showAndWait() {
        Platform.runLater(() -> {
            try {
                start(new Stage());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
