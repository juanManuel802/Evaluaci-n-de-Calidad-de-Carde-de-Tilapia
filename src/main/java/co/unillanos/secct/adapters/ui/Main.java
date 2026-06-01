package co.unillanos.secct.adapters.ui;

import co.unillanos.secct.infrastructure.repositories.FakeClasificadorCnn;
import co.unillanos.secct.infrastructure.repositories.GeneradorCodigoLoteSecuencial;
import co.unillanos.secct.infrastructure.repositories.InMemoryLoteRepository;
import co.unillanos.secct.usecases.services.SecctApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        InMemoryLoteRepository repo = new InMemoryLoteRepository();
        FakeClasificadorCnn cnn     = new FakeClasificadorCnn();
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial();
        SecctApp app = new SecctApp(repo, cnn, gen);
        new InicializadorDatos(app).cargar();

        PantallaRegistrarLote pantallaRegistrar = new PantallaRegistrarLote(app);
        PantallaEvaluarCalidad pantallaEvaluar  = new PantallaEvaluarCalidad(app);

        Tab tabRegistrar = new Tab("Registrar Lote");
        tabRegistrar.setClosable(false);
        tabRegistrar.setContent(pantallaRegistrar.construirVista());

        Tab tabEvaluar = new Tab("Evaluar Lote");
        tabEvaluar.setClosable(false);
        tabEvaluar.setContent(pantallaEvaluar.construirVista());

        // Refresca la lista de lotes al cambiar a la pestaña de evaluación,
        // para reflejar cualquier lote recién registrado.
        tabEvaluar.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) pantallaEvaluar.actualizarLista();
        });

        TabPane tabPane = new TabPane(tabRegistrar, tabEvaluar);

        Scene scene = new Scene(tabPane, 860, 620);
        primaryStage.setTitle("SECCT — Evaluación de Calidad de Tilapia NTC 1443");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
