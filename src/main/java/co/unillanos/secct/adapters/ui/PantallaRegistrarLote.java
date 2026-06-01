package co.unillanos.secct.adapters.ui;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.services.SecctApp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class PantallaRegistrarLote {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SecctApp app;

    private TextField txtCodigo;
    private TextField txtEstacion;
    private DatePicker dpFecha;
    private TextField txtPeso;
    private TextField txtUnidades;
    private ComboBox<PuntoEvaluacion> cmbPunto;
    private TextArea taObservaciones;
    private TextArea txtResultado;

    public PantallaRegistrarLote(SecctApp app) {
        this.app = app;
    }

    public BorderPane construirVista() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setTop(construirCabecera());
        root.setCenter(construirFormulario());
        inicializarFormulario();
        return root;
    }

    // ------- secciones de la pantalla -------

    private VBox construirCabecera() {
        Label titulo = new Label("Registrar Nuevo Lote");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 16));
        VBox cab = new VBox(titulo);
        cab.setPadding(new Insets(0, 0, 12, 0));
        return cab;
    }

    private GridPane construirFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;

        // --- Sección: código ---
        Label lblCodSec = new Label("Código del lote");
        lblCodSec.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(lblCodSec, 0, row++, 2, 1);

        grid.add(new Label("Código:"), 0, row);
        txtCodigo = new TextField();
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #f4f4f4;");
        Button btnNuevoCodigo = new Button("Nuevo código");
        btnNuevoCodigo.setOnAction(e -> generarNuevoCodigo());
        HBox codBox = new HBox(8, txtCodigo, btnNuevoCodigo);
        HBox.setHgrow(txtCodigo, Priority.ALWAYS);
        grid.add(codBox, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        // --- Sección: datos del lote ---
        Label lblDatosSec = new Label("Datos del lote");
        lblDatosSec.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(lblDatosSec, 0, row++, 2, 1);

        grid.add(new Label("Estación de origen:"), 0, row);
        txtEstacion = new TextField();
        txtEstacion.setPromptText("Ej. Estación Piscícola Meta");
        grid.add(txtEstacion, 1, row++);

        grid.add(new Label("Fecha de captura:"), 0, row);
        dpFecha = construirDatePicker();
        grid.add(dpFecha, 1, row++);

        grid.add(new Label("Peso total (kg):"), 0, row);
        txtPeso = new TextField();
        txtPeso.setPromptText("Ej. 120.50");
        grid.add(txtPeso, 1, row++);

        grid.add(new Label("Núm. unidades:"), 0, row);
        txtUnidades = new TextField();
        txtUnidades.setPromptText("Ej. 15");
        grid.add(txtUnidades, 1, row++);

        grid.add(new Label("Punto evaluación:"), 0, row);
        cmbPunto = construirComboPunto();
        grid.add(cmbPunto, 1, row++);

        grid.add(new Label("Observaciones:"), 0, row);
        taObservaciones = new TextArea();
        taObservaciones.setPromptText("Opcional — condiciones de transporte, temperatura, etc.");
        taObservaciones.setPrefRowCount(3);
        taObservaciones.setWrapText(true);
        grid.add(taObservaciones, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        // --- Botón guardar ---
        Button btnGuardar = new Button("Guardar lote");
        btnGuardar.setDefaultButton(true);
        btnGuardar.setOnAction(e -> onGuardarLote());
        HBox btnBox = new HBox(btnGuardar);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(btnBox, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        // --- Sección: resultado ---
        Label lblResult = new Label("Resultado");
        lblResult.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(lblResult, 0, row++, 2, 1);

        txtResultado = new TextArea();
        txtResultado.setEditable(false);
        txtResultado.setPrefRowCount(3);
        txtResultado.setWrapText(true);
        grid.add(txtResultado, 0, row, 2, 1);

        ColumnConstraints col0 = new ColumnConstraints(145);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        return grid;
    }

    private DatePicker construirDatePicker() {
        DatePicker dp = new DatePicker();
        dp.setPromptText("dd/MM/yyyy");
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? FORMATO_FECHA.format(date) : "";
            }
            @Override
            public LocalDate fromString(String s) {
                try {
                    return (s != null && !s.isBlank()) ? LocalDate.parse(s, FORMATO_FECHA) : null;
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        dp.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });
        return dp;
    }

    private ComboBox<PuntoEvaluacion> construirComboPunto() {
        ComboBox<PuntoEvaluacion> cmb = new ComboBox<>();
        cmb.getItems().addAll(PuntoEvaluacion.values());
        cmb.setMaxWidth(Double.MAX_VALUE);
        cmb.setConverter(new StringConverter<PuntoEvaluacion>() {
            @Override
            public String toString(PuntoEvaluacion pe) {
                if (pe == null) return "";
                switch (pe) {
                    case ESTACION_PISCICOLA:    return "Estación Piscícola";
                    case CENTRO_ACOPIO:         return "Centro de Acopio";
                    case DISTRIBUIDOR_MAYORISTA: return "Distribuidor Mayorista";
                    case PLAZA_MERCADO:         return "Plaza de Mercado";
                    default:                    return pe.name();
                }
            }
            @Override
            public PuntoEvaluacion fromString(String s) { return null; }
        });
        return cmb;
    }

    // ------- inicialización -------

    private void inicializarFormulario() {
        dpFecha.setValue(LocalDate.now());
        cmbPunto.getSelectionModel().selectFirst();
        generarNuevoCodigo();
    }

    // ------- manejadores de eventos -------

    private void generarNuevoCodigo() {
        try {
            CodigoLote codigo = app.obtenerCodigoNuevoLote();
            txtCodigo.setText(codigo.getValor());
        } catch (IllegalStateException e) {
            txtCodigo.setText("Límite alcanzado");
            mostrarMensaje("El generador de códigos ha alcanzado su límite. Reinicie la aplicación.");
        }
    }

    private void onGuardarLote() {
        String estacion = txtEstacion.getText().trim();
        LocalDate fecha  = dpFecha.getValue();
        PuntoEvaluacion punto = cmbPunto.getValue();

        if (estacion.isEmpty()) {
            mostrarMensaje("La estación de origen es obligatoria (RN-002).");
            return;
        }
        if (fecha == null) {
            mostrarMensaje("Seleccione una fecha de captura (RN-003).");
            return;
        }
        if (punto == null) {
            mostrarMensaje("Seleccione el punto de evaluación (RN-006).");
            return;
        }

        BigDecimal peso;
        try {
            peso = new BigDecimal(txtPeso.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarMensaje("Peso total inválido. Ingrese un número, por ejemplo 120.50 (RN-004).");
            return;
        }

        int numUnidades;
        try {
            numUnidades = Integer.parseInt(txtUnidades.getText().trim());
        } catch (NumberFormatException e) {
            mostrarMensaje("Número de unidades inválido. Ingrese un entero positivo (RN-005).");
            return;
        }

        OperationResult result = app.registrarLote(new DatosNuevoLote(
                txtCodigo.getText().trim(),
                estacion,
                fecha,
                peso,
                numUnidades,
                punto.name(),
                taObservaciones.getText().trim()));

        mostrarMensaje(result.getMessage());

        if (result.isSuccess()) {
            limpiarCampos();
            generarNuevoCodigo();
        }
    }

    // ------- utilidades -------

    private void limpiarCampos() {
        txtEstacion.clear();
        dpFecha.setValue(LocalDate.now());
        txtPeso.clear();
        txtUnidades.clear();
        cmbPunto.getSelectionModel().selectFirst();
        taObservaciones.clear();
    }

    private void mostrarMensaje(String mensaje) {
        txtResultado.setText(mensaje);
    }
}
