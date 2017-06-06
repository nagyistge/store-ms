package sample.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by uran on 17-04-30.
 */
public class ShtoPunetoret implements Initializable {

    DB db = new DB();
    private Connection con = db.connect();
    @FXML private BorderPane shtoBp, childBp;
    @FXML Button btnRuaj;
    @FXML private Hyperlink addDp;
    @FXML private TextField emri, mbiemri, paga, titulli, telefoni, email, adresa, qyteti, shteti, txtUser;
    @FXML private PasswordField txtPw;
    @FXML private ComboBox departamenti, gjinia, cbStatusi;
    @FXML private DatePicker punesuar, ditelindja;
    @FXML private Label lblFoto;
    @FXML private Button btnFotoHape;

    int id = -1;
    char mode = 'i';

    public void setId (int id)
    {
        this.id = id;
    }
    public void setMode (char mode) {
        this.mode = mode;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s");

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        emri.focusedProperty().addListener((o, ov, nv) -> {
            if (nv.booleanValue() == false && !mbiemri.getText().isEmpty()) {
                txtUser.setText(emri.getText().toLowerCase() + "." + mbiemri.getText().toLowerCase());
            }
        });

        mbiemri.focusedProperty().addListener((o, ov, nv) -> {
            if (nv.booleanValue() == false && !emri.getText().isEmpty()) {
                txtUser.setText(emri.getText().toLowerCase() + "." + mbiemri.getText().toLowerCase());
            }
        });

        ditelindja.setConverter(VariablatPublike.converter);
        punesuar.setConverter(VariablatPublike.converter);

        btnRuaj.setOnAction(e -> {
            shtoPnt(mode, id);
        });

        try {
            merrDeps("Select * from departamenti");
        } catch (Exception e) {
            e.printStackTrace();
        }
        rregulloPnt(id);
    }

    private void merrDeps(String q) throws Exception{
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(q);

        departamenti.getItems().clear();
        while (rs.next()) {
            departamenti.getItems().add(rs.getString("departamenti"));
            VariablatPublike.dep.put(rs.getString("departamenti"), rs.getInt("id"));
            System.out.println(rs.getString("departamenti"));
        }
        departamenti.getSelectionModel().select(0);

        for (Map.Entry<String, Integer> m : VariablatPublike.dep.entrySet()) {
            VariablatPublike.revDep.put(m.getValue(), m.getKey());
        }

    }

    private void rregulloPnt (int id) {
        try {

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from punetoret where id = " + id + " limit 1");

            while (rs.next()) {
                emri.setText(rs.getString("emri"));
                mbiemri.setText(rs.getString("mbiemri"));
                paga.setText(rs.getDouble("paga") + "");
                titulli.setText(rs.getString("titulli"));
                telefoni.setText(rs.getString("telefoni"));
                email.setText(rs.getString("email"));
                adresa.setText(rs.getString("adresa"));
                qyteti.setText(rs.getString("qyteti"));
                shteti.setText(rs.getString("shteti"));
                lblFoto.setText(rs.getString("foto"));
                cbStatusi.getSelectionModel().select(rs.getInt("statusi"));
                if (!lblFoto.getText().isEmpty()) btnFotoHape.setDisable(false);
                departamenti.getSelectionModel().select(VariablatPublike.revDep.get(rs.getInt("dep_id")));
                gjinia.getSelectionModel().select(rs.getInt("gjinia"));
                ditelindja.setValue(rs.getDate("ditelindja").toLocalDate());
                punesuar.setValue(LocalDate.parse(rs.getString("data_punesimit"), VariablatPublike.dtf));
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shtoPnt(int mode, int id){
        try {
            PreparedStatement pstmt = null;
            PreparedStatement pstmt2 = null;
            if (mode == 'i') {
                pstmt2 = con.prepareStatement("insert into perdoruesi values (null, (select max(id) from punetoret), ?, ?)");

                pstmt = con.prepareStatement("INSERT INTO punetoret" +
                "(id, dep_id, emri, mbiemri, gjinia, ditelindja, titulli, paga, data_punesimit, statusi, " +
                        "telefoni, modifikuar, adresa, qyteti, shteti, email, data_krijimit, foto)" +
                "VALUES" +
                "(NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_date(), ?)");
            }else if (mode == 'u' && id > 0) {
                pstmt2 = con.prepareStatement("update perdoruesi set usr = ?, pw = ? where pnt_id = " + id);
                pstmt = con.prepareStatement("update punetoret set " +
                "dep_id = ?, emri = ?, mbiemri = ?, gjinia = ?, ditelindja = ?, titulli = ?, paga = ?, " +
                "data_punesimit = ?, statusi = ?, telefoni = ?, modifikuar = ?, adresa = ?, qyteti=?," +
                        "shteti=?,email=?,foto = ? " +
                "where id = " + id);
            }

            if (!emri.getText().equals("") && !mbiemri.getText().equals("")
                    && !paga.getText().equals("") && !punesuar.getEditor().getText().equals("") && !titulli.getText().equals("")
                    && !telefoni.getText().equals("") && !email.getText().equals("")) {
                if (!paga.getText().trim().matches("[0-9]+.") || !telefoni.getText().trim().matches("[0-9 ]+")) {
                    MesazhetPublike.Lajmerim("Fushat 'Paga' dhe 'Telefoni' duhet te permajne vetem numra!",
                            MesazhetPublike.ButtonType.OK_BUTTON, MesazhetPublike.NotificationType.ERROR, 8);
                }else {
                    pstmt.setInt(1, VariablatPublike.dep.get(departamenti.getSelectionModel().getSelectedItem()));
                    pstmt.setString(2, emri.getText());
                    pstmt.setString(3, mbiemri.getText());
                    pstmt.setInt(4, gjinia.getSelectionModel().getSelectedItem() == "Mashkull" ? 1 : 0);
                    pstmt.setDate(5, java.sql.Date.valueOf(formatter.format(ditelindja.getValue())));
                    pstmt.setString(6, titulli.getText());
                    pstmt.setDouble(7, Double.parseDouble(paga.getText()));
                    pstmt.setDate(8, java.sql.Date.valueOf(formatter.format(punesuar.getValue())));
                    pstmt.setInt(9, cbStatusi.getSelectionModel().getSelectedIndex());
                    pstmt.setString(10, telefoni.getText());
                    pstmt.setString(11, formatterTime.format(LocalDateTime.now()));
                    pstmt.setString(12, adresa.getText());
                    pstmt.setString(13, qyteti.getText());
                    pstmt.setString(14, shteti.getText());
                    pstmt.setString(15, email.getText());
                    pstmt.setString(16, lblFoto.getText());
                    pstmt.execute();

                    pstmt2.setString(1, txtUser.getText());
                    pstmt2.setString(2, txtPw.getText());
                    pstmt2.execute();

                    pastro();
                    MesazhetPublike.Lajmerim("Te dhenat u ruajten me sukses!", MesazhetPublike.ButtonType.NO_BUTTON,
                            MesazhetPublike.NotificationType.SUCCESS, 5);
                }
            }else
                MesazhetPublike.Lajmerim("Fushat e kerkuara duhet te permbajne te dhena!",
                        MesazhetPublike.ButtonType.OK_BUTTON, MesazhetPublike.NotificationType.ERROR, 8);

        }catch (Exception ex) {ex.printStackTrace();}

    }

    private void pastro (){
        email.setText("");
        emri.setText("");
        mbiemri.setText("");
        paga.setText("");
        adresa.setText("");
        qyteti.setText("");
        shteti.setText("");
        titulli.setText("");
        telefoni.setText("");
        pastroFoto();
        punesuar.setValue(LocalDate.now());
        ditelindja.getEditor().clear();
        departamenti.getSelectionModel().select(0);
        gjinia.getSelectionModel().select(0);
    }

    @FXML
    private void openAddDep() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/gui/ShtoDepartamente.fxml"));
            Parent parent = loader.load();
            ShtoDepartamente dep = loader.getController();
            dep.btnKthehu.setOnAction(e -> {
                stage.close();
            });

            Scene scene = new Scene(parent, 400, 500);
            stage.setScene(scene);
            scene.getStylesheets().add(getClass().getResource("/sample/style/style.css").toExternalForm());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void zgjedhFoto (){
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Foto (*.jpg, *.jpeg, *.png)", "*.jpg", "*.jpeg", "*.png");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zgjedh foton");
        fileChooser.getExtensionFilters().add(extFilter);

        lblFoto.setText(fileChooser.showOpenDialog(stage).getAbsolutePath());
        btnFotoHape.setDisable(false);

        if (lblFoto.getText().equals("null")) {
            lblFoto.setText("");
            btnFotoHape.setDisable(true);
        }
    }

    @FXML
    private void pastroFoto (){
        lblFoto.setText("");
        btnFotoHape.setDisable(true);
    }

    @FXML
    private void hapeFoto (){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Desktop.getDesktop().open(new File(lblFoto.getText()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();

    }

}
