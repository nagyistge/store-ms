package sample.controller;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML ToggleButton btnHome, btnPunetoret, btnStat, btnKons, btnPaisjet, btnProfile, btnSettings, btnLogout, btnShit;
    @FXML BorderPane root;
    @FXML VBox btnVb;
    @FXML private Label user;
    @FXML private ImageView spinner;
    private String usr = "null";
    private int pntId = 0;

    private Stage stage;

    DB db = new DB();
    Connection con = db.connect();

    public void setPntId(int id) {
        this.pntId = id;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        stage.setOnCloseRequest(e -> {
            Server.stopServer();
            Platform.exit();
        });

        stage.setTitle("Sistemi per Menaxhim te Shitjeve");
        if (pntId > 0)
            setPrivileges();

        merrTvsh();
        disableButtons(VariablatPublike.shtepi, btnHome);
        disableButtons(VariablatPublike.punetoret, btnPunetoret);
        disableButtons(VariablatPublike.konsumatoret, btnKons);
        disableButtons(VariablatPublike.settings, btnSettings);
        disableButtons(VariablatPublike.shitjeBool, btnShit);
        disableButtons(VariablatPublike.raportet, btnStat);
        disableButtons(VariablatPublike.produktet, btnPaisjet);

        try {
            if (pntId > 0) root.setCenter(openFirst().load());
            else root.setCenter(FXMLLoader.load(getClass().getResource("/sample/gui/dashboard.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FXMLLoader openFirst(){
        FXMLLoader loader = null;
        if (VariablatPublike.shtepi) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/dashboard.fxml"));
            btnHome.setSelected(true);
        }
        else if (VariablatPublike.punetoret) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/punetoret.fxml"));
            btnPunetoret.setSelected(true);
        }
        else if (VariablatPublike.konsumatoret) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/konsumatoret.fxml"));
            btnKons.setSelected(true);
        }
        else if (VariablatPublike.produktet) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/produktet.fxml"));
            btnPaisjet.setSelected(true);
        }
        else if (VariablatPublike.settings) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/Settings.fxml"));
            btnSettings.setSelected(true);
        }
        else if (VariablatPublike.raportet) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/Raportet.fxml"));
            btnStat.setSelected(true);
        }
        else if (VariablatPublike.shitjeBool) {
            loader = new FXMLLoader(getClass().getResource("/sample/gui/Shitjet.fxml"));
            btnShit.setSelected(true);
        }
        return loader;
    }

    private void disableButtons(boolean bool, ToggleButton btn){
        if (!bool && pntId > 0) btn.setDisable(true);
    }

    private void addTooltip(Tooltip tp, ToggleButton btn, String text){
        btn.setOnMouseEntered(e -> {
            tp.setText(text);
            tp.show(stage, btn.getBoundsInLocal().getMaxX(), btn.getBoundsInParent().getMaxY()-3-tp.getMaxHeight()/2);
        });

        btn.setOnMouseExited(e -> tp.hide());
    }

    private void setPrivileges (){
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("select * from priv where dep_id = " + pntId)) {

            rs.next();
            VariablatPublike.shtepi = rs.getBoolean("shtepi");
            VariablatPublike.punetoret = rs.getBoolean("punetoret");
            VariablatPublike.konsumatoret = rs.getBoolean("konsumatoret");
            VariablatPublike.raportet = rs.getBoolean("raportet");
            VariablatPublike.shitjeBool = rs.getBoolean("shitje");
            VariablatPublike.produktet = rs.getBoolean("produktet");
            VariablatPublike.settings = rs.getBoolean("settings");

        }catch (Exception e){ e. printStackTrace(); }
    }

    private String cap(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1, text.length()).toLowerCase();
    }

    public void merrTvsh(){
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("select * from tvsh")) {
            while (rs.next()) {
                VariablatPublike.tvsh = rs.getDouble("tvsh");
            }
        }catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void changeScene(ActionEvent e) throws Exception {
        Parent pntRoot = null;

        if (((ToggleButton) e.getSource()).getText().equals("BALLINA")) {
            pntRoot = FXMLLoader.load(getClass().getResource("/sample/gui/dashboard.fxml"));
        }else if (((ToggleButton) e.getSource()).getText().equals("PUNETORET")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/punetoret.fxml"));
            pntRoot = loader.load();

            Punetoret punetoret = loader.getController();
            punetoret.setStage(stage);
            punetoret.setTransition(startSpinning(spinner));
            punetoret.setIv(spinner);
            punetoret.setBorderPane(root);

            punetoret.btnShtoPnt.setOnAction(a -> {
                try {
                    FXMLLoader l = new FXMLLoader(getClass().getResource("/sample/gui/ShtoPunetoret.fxml"));
                    ShtoPunetoret shp = new ShtoPunetoret();
                    l.setController(shp);
                    root.setCenter(l.load());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });

        }else if (((ToggleButton) e.getSource()).getText().equals("PRODUKTET")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/produktet.fxml"));
            pntRoot = loader.load();
            Produktet p = loader.getController();

            p.setStage(stage);
            p.setTransition(startSpinning(spinner));
            p.setIv(spinner);
            p.setBp(root);
        }else if (((ToggleButton) e.getSource()).getText().equals("KONSUMATORET")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/konsumatoret.fxml"));
            pntRoot = loader.load();
            Konsumatoret konsumatoret = loader.getController();
            konsumatoret.setIv(spinner);
            konsumatoret.setTransition(startSpinning(spinner));
            konsumatoret.setRoot(root);
        }else if (((ToggleButton) e.getSource()).getText().equals("RAPORTET")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/Raportet.fxml"));
            pntRoot = loader.load();
        }else if (((ToggleButton) e.getSource()).getText().equals("SHITJE")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/Shitjet.fxml"));
            pntRoot = loader.load();
        }else if (((ToggleButton) e.getSource()).getText().equals("PREFERENCAT")) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/Settings.fxml"));
            pntRoot = loader.load();
            Settings settings = loader.getController();
            settings.setIv(spinner);
            settings.setTransition(startSpinning(spinner));
        }

        root.setCenter(pntRoot);
    }

    @FXML
    private void exit(){
        Server.stopServer();
        Platform.exit();
    }

    public RotateTransition startSpinning(ImageView iv){
        RotateTransition transition = new RotateTransition(Duration.millis(700), iv);
        transition.setByAngle(360);
        transition.setCycleCount(-1);
        return transition;
    }

}
