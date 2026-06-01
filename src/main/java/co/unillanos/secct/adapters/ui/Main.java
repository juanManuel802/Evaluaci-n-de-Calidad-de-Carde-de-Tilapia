package co.unillanos.secct.adapters.ui;

import atlantafx.base.theme.NordLight;
import co.unillanos.secct.infrastructure.repositories.FakeClasificadorCnn;
import co.unillanos.secct.infrastructure.repositories.GeneradorCodigoLoteSecuencial;
import co.unillanos.secct.infrastructure.repositories.InMemoryLoteRepository;
import co.unillanos.secct.usecases.services.SecctApp;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        FakeClasificadorCnn    cnn  = new FakeClasificadorCnn();
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial();
        SecctApp app = new SecctApp(repo, cnn, gen);
        new InicializadorDatos(app).cargar();

        PantallaRegistrarLote pantallaRegistrar = new PantallaRegistrarLote(app);
        PantallaEvaluarCalidad pantallaEvaluar  = new PantallaEvaluarCalidad(app);

        Tab tabRegistrar = new Tab("  📋  Registrar Lote  ");
        tabRegistrar.setClosable(false);
        tabRegistrar.setContent(pantallaRegistrar.construirVista());

        Tab tabEvaluar = new Tab("  🔬  Evaluar Calidad  ");
        tabEvaluar.setClosable(false);
        tabEvaluar.setContent(pantallaEvaluar.construirVista());

        tabEvaluar.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) pantallaEvaluar.actualizarLista();
        });

        TabPane tabPane = new TabPane(tabRegistrar, tabEvaluar);
        tabPane.getStyleClass().add("secct-tab-pane");

        Label appTitle  = new Label("SECCT");
        appTitle.getStyleClass().addAll("app-title");

        Label appSubtitle = new Label("Sistema de Evaluación de Calidad de Carne de Tilapia · NTC 1443");
        appSubtitle.getStyleClass().add("app-subtitle");

        Label badge = new Label("Unillanos");
        badge.getStyleClass().add("app-badge");

        HBox titleRow = new HBox(10, appTitle, appSubtitle);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(appSubtitle, Priority.ALWAYS);

        HBox header = new HBox(titleRow, badge);
        HBox.setHgrow(titleRow, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("app-header");
        header.setPadding(new Insets(12, 20, 12, 20));

        VBox root = new VBox(header, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 900, 660);
        scene.getStylesheets().add(
                Main.class.getResource("/co/unillanos/secct/adapters/ui/secct.css")
                          .toExternalForm()
        );

        primaryStage.setTitle("SECCT — Evaluación de Calidad de Tilapia");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(780);
        primaryStage.setMinHeight(580);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}