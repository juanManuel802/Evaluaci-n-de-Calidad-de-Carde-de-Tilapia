package co.unillanos.secct.adapters.ui;

import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.services.SecctApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class PantallaEvaluarCalidad {

    private final SecctApp app;

    private String loteSeleccionadoId;

    private ListView<String> listViewLotes;
    private Label lblLoteId;
    private Label lblEstado;
    private Label lblProgreso;
    private Label lblClasificacion;
    private TextField txtRutaImagen;
    private Button btnEvaluar;
    private Button btnCerrarEvaluacion;
    private TextArea txtResultado;

    public PantallaEvaluarCalidad(SecctApp app) {
        this.app = app;
    }

    
    public BorderPane construirVista() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setTop(construirCabecera());
        root.setLeft(construirPanelLotes());
        root.setCenter(construirPanelEvaluacion());
        return root;
    }

    
    
    

    private VBox construirCabecera() {
        Label titulo = new Label("SECCT — Evaluación de Calidad de Tilapia NTC 1443");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        VBox cabecera = new VBox(titulo);
        cabecera.setPadding(new Insets(0, 0, 12, 0));
        return cabecera;
    }

    private VBox construirPanelLotes() {
        Label lblTitulo = new Label("Lotes disponibles");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 13));

        listViewLotes = new ListView<>();
        listViewLotes.setPrefWidth(230);
        listViewLotes.setPrefHeight(300);
        VBox.setVgrow(listViewLotes, Priority.ALWAYS);

        Button btnSeleccionar = new Button("Seleccionar lote");
        btnSeleccionar.setMaxWidth(Double.MAX_VALUE);
        btnSeleccionar.setOnAction(e -> onSeleccionarLote());

        Button btnActualizar = new Button("Actualizar lista");
        btnActualizar.setMaxWidth(Double.MAX_VALUE);
        btnActualizar.setOnAction(e -> cargarLotesEnLista());

        VBox panel = new VBox(8, lblTitulo, listViewLotes, btnSeleccionar, btnActualizar);
        panel.setPadding(new Insets(0, 16, 0, 0));

        cargarLotesEnLista();
        return panel;
    }

    private GridPane construirPanelEvaluacion() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 0, 8));

        
        Label secInfo = new Label("Información del lote seleccionado");
        secInfo.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(secInfo, 0, 0, 2, 1);

        grid.add(new Label("Lote:"), 0, 1);
        lblLoteId = new Label("—");
        grid.add(lblLoteId, 1, 1);

        grid.add(new Label("Estado:"), 0, 2);
        lblEstado = new Label("—");
        grid.add(lblEstado, 1, 2);

        grid.add(new Label("Progreso:"), 0, 3);
        lblProgreso = new Label("—");
        grid.add(lblProgreso, 1, 3);

        grid.add(new Label("Clasificación:"), 0, 4);
        lblClasificacion = new Label("—");
        grid.add(lblClasificacion, 1, 4);

        grid.add(new Separator(), 0, 5, 2, 1);


        Label secEval = new Label("Evaluar unidad");
        secEval.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(secEval, 0, 6, 2, 1);

        grid.add(new Label("Imagen:"), 0, 7);
        txtRutaImagen = new TextField();
        txtRutaImagen.setPromptText("Seleccione una imagen…");
        Button btnExaminar = new Button("Examinar…");
        btnExaminar.setOnAction(e -> onExaminarImagen());
        HBox imagenBox = new HBox(6, txtRutaImagen, btnExaminar);
        HBox.setHgrow(txtRutaImagen, Priority.ALWAYS);
        grid.add(imagenBox, 1, 7);

        btnEvaluar = new Button("Evaluar unidad");
        btnEvaluar.setOnAction(e -> onEvaluarUnidad());

        btnCerrarEvaluacion = new Button("Cerrar evaluación del lote");
        btnCerrarEvaluacion.setOnAction(e -> onCerrarEvaluacion());

        HBox botonesEval = new HBox(8, btnEvaluar, btnCerrarEvaluacion);
        grid.add(botonesEval, 1, 8);

        grid.add(new Separator(), 0, 9, 2, 1);


        Label secResultado = new Label("Resultado");
        secResultado.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(secResultado, 0, 10, 2, 1);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(5);
        txtResultado.setWrapText(true);
        grid.add(txtResultado, 0, 11, 2, 1);

        ColumnConstraints col0 = new ColumnConstraints(95);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        return grid;
    }

    
    
    

    public void actualizarLista() {
        cargarLotesEnLista();
    }

    private void cargarLotesEnLista() {
        List<Lote> disponibles = app.listarLotesDisponibles();
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Lote lote : disponibles) {
            items.add(lote.getId()
                    + "  (" + lote.cantidadEvaluaciones()
                    + " / " + lote.getNumeroUnidadesMuestra() + ")");
        }
        listViewLotes.setItems(items);
    }

    
    private void onSeleccionarLote() {
        String selected = listViewLotes.getSelectionModel().getSelectedItem();
        String loteId = selected != null ? selected.split("  \\(")[0] : null;
        OperationResult result = app.seleccionarLote(loteId);
        if (result.isSuccess()) {
            loteSeleccionadoId = loteId;
            mostrarInfoLote(buscarLote(loteSeleccionadoId));
        } else {
            loteSeleccionadoId = null;
            limpiarInfoLote();
        }
        mostrarMensaje(result.getMessage());
    }

    
    private void onExaminarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen de tilapia");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
        Stage stage = (Stage) txtRutaImagen.getScene().getWindow();
        File archivo = fileChooser.showOpenDialog(stage);
        if (archivo != null) {
            txtRutaImagen.setText(archivo.getAbsolutePath());
        }
    }

    
    private void onEvaluarUnidad() {
        String rutaTexto = txtRutaImagen.getText().trim();
        if (rutaTexto.isEmpty()) {
            mostrarMensaje("Seleccione una imagen antes de evaluar.");
            return;
        }
        Path path = Paths.get(rutaTexto);
        byte[] contenido;
        try {
            contenido = Files.readAllBytes(path);
        } catch (IOException e) {
            mostrarMensaje("Error al leer la imagen: " + e.getMessage());
            return;
        }
        String nombre = path.getFileName() != null
                ? path.getFileName().toString()
                : path.toString();
        OperationResult result = app.evaluarUnidad(loteSeleccionadoId, nombre, contenido);
        if (result.isSuccess()) {
            txtRutaImagen.clear();
            cargarLotesEnLista();
            mostrarInfoLote(buscarLote(loteSeleccionadoId));
        }
        mostrarMensaje(result.getMessage());
    }


    private void onCerrarEvaluacion() {
        OperationResult result = app.evaluarLote(loteSeleccionadoId);
        if (result.isSuccess()) {
            cargarLotesEnLista();
            mostrarInfoLote(buscarLote(loteSeleccionadoId));
        }
        mostrarMensaje(result.getMessage());
    }




    private Lote buscarLote(String loteId) {
        if (loteId == null) return null;
        for (Lote lote : app.listarLotesDisponibles()) {
            if (lote.getId().equals(loteId)) return lote;
        }
        for (Lote lote : app.listarLotesEvaluados()) {
            if (lote.getId().equals(loteId)) return lote;
        }
        return null;
    }

    private void mostrarInfoLote(Lote lote) {
        if (lote == null) { limpiarInfoLote(); return; }
        lblLoteId.setText(lote.getId());
        lblEstado.setText(formatearEstado(lote.getEstado().name()));
        lblProgreso.setText(lote.cantidadEvaluaciones() + " / " + lote.getNumeroUnidadesMuestra());
        lblClasificacion.setText(lote.getClasificacionFinal() > 0.0
                ? String.format("%.2f", lote.getClasificacionFinal())
                : "—");
    }

    private String formatearEstado(String estado) {
        switch (estado) {
            case "ABIERTO":       return "Abierto";
            case "EN_EVALUACION": return "En evaluación";
            case "EVALUADO":      return "Evaluado";
            case "REPORTADO":     return "Reportado";
            default:              return estado;
        }
    }

    private void limpiarInfoLote() {
        lblLoteId.setText("—");
        lblEstado.setText("—");
        lblProgreso.setText("—");
        lblClasificacion.setText("—");
    }

    private void mostrarMensaje(String mensaje) {
        txtResultado.setText(mensaje);
    }
}
