import javafx.application.Application;
import javafx.stage.Stage;

public class ConqueresApplication extends Application {

    GameController gameController;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        gameController = new GameController();
        gameController.getGameUX().setStage(primaryStage);
        primaryStage.show();
    }
}
