package co.unillanos.secct.adapters.ui;

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
        GeneradorCodigoLoteSecuencial gen = new GeneradorCodigoLoteSecuencial();
        SecctApp app = new SecctApp(repo, gen);
        new InicializadorDatos(app).cargar();

        PantallaRegistrarLote pantallaRegistrar = new PantallaRegistrarLote(app);

        Tab tabRegistrar = new Tab("Registrar Lote");
        tabRegistrar.setClosable(false);
        tabRegistrar.setContent(pantallaRegistrar.construirVista());

        TabPane tabPane = new TabPane(tabRegistrar);

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
