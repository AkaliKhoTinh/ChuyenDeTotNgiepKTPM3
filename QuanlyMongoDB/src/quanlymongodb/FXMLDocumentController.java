/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quanlymongodb;

import java.net.URL;
// Loại bỏ java.sql.* (Connection, PreparedStatement, ResultSet)
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.Document; // Thư viện MongoDB: Document
import com.mongodb.client.MongoCollection; // Thư viện MongoDB: Collection
import com.mongodb.client.MongoDatabase; // Thư viện MongoDB: Database
import com.mongodb.client.model.Filters; // Thư viện MongoDB: Filters


public class FXMLDocumentController implements Initializable {

    @FXML
    private Button close;

    @FXML
    private Button loginBtn;

    @FXML
    private AnchorPane main_form;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    // Biến để kéo thả cửa sổ
    private double x = 0;
    private double y = 0;

    
    public void login() {
        // Kết nối tới MongoDB và lấy collection 'admin'
        MongoDatabase db = Database.connectDB();
        if (db == null) return;
        
        MongoCollection<Document> adminCollection = db.getCollection("admin");

        String user = username.getText();
        String pass = password.getText();

        try {
            Alert alert;
            
            // 1. Kiểm tra input rỗng
            if (user.isEmpty() || pass.isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi!!!");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng nhập đầy đủ thông tin");
                alert.showAndWait();
                return; 
            }
            
            // 2. TẠO FILTER VÀ TÌM KIẾM TRONG MONGODB
            // Filters.and(Filters.eq("username", user), Filters.eq("password", pass))
            Document foundUser = adminCollection.find(
                Filters.and(
                    Filters.eq("username", user),
                    Filters.eq("password", pass)
                )
            ).first(); // Lấy Document đầu tiên khớp

            // 3. XỬ LÝ KẾT QUẢ
            if (foundUser != null) { 
                
                // Đăng nhập thành công
                data.username = user;
                
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Đăng nhập thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đăng nhập thành công");
                alert.showAndWait();

                // Ẩn giao diện login
                loginBtn.getScene().getWindow().hide();

                // Liên kết với dashboard
                Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));

                Stage stage = new Stage();
                Scene scene = new Scene(root);

                // Logic kéo thả cửa sổ
                root.setOnMousePressed((MouseEvent event) -> {
                    x = event.getSceneX();
                    y = event.getSceneY();
                });

                root.setOnMouseDragged((MouseEvent event) -> {
                    stage.setX(event.getScreenX() - x);
                    stage.setY(event.getScreenY() - y);
                    stage.setOpacity(.7f);
                });

                root.setOnMouseReleased((MouseEvent event) -> {
                    stage.setOpacity(0.9);
                });

                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.show();

            } else {
                // Sai tài khoản hoặc mật khẩu
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi!!!");
                alert.setHeaderText(null);
                alert.setContentText("Sai tài khoản hoặc mật khẩu!! ");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Đóng phần mềm
    public void close() {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
