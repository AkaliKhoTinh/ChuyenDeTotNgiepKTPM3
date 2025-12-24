package quanlymongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Sorts;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class DashboardController implements Initializable {

    // [KHAI BÁO FXML GIỮ NGUYÊN]
    @FXML
    private TextField availableID_ID;
    @FXML
    private TextField availableID_Name;
    @FXML
    private TextField availableID_Price;
    @FXML
    private ComboBox<?> availableID_Size;
    @FXML
    private ComboBox<?> availableID_Status;
    @FXML
    private TableView<categories> availableID_tableview;
    @FXML
    private TextField availableID_search;
    @FXML
    private TableView<product> order_tableview;
    @FXML
    private Label dashboard_NC;
    @FXML
    private Label dashboard_TI;
    @FXML
    private Label dashboard_Tincome;
    @FXML
    private Label order_total;
    @FXML
    private Label order_balance;
    @FXML
    private TextField order_amount;
    @FXML
    private ComboBox<?> order_productID;
    @FXML
    private ComboBox<?> order_productName;
    @FXML
    private Spinner<Integer> order_quanitity;
    @FXML
    private Button dashboard_btn;
    @FXML
    private Button available_btn;
    @FXML
    private Button order_btn;
    @FXML
    private AnchorPane dashboard_form;
    @FXML
    private AnchorPane availableID_form;
    @FXML
    private AnchorPane order_form;
    @FXML
    private Button logout;
    @FXML
    private AnchorPane main_form;
    @FXML
    private BarChart<?, ?> dashboard_NCChart;
    @FXML
    private BarChart<?, ?> dashboard_ICChart;
    @FXML
    private TableColumn<categories, String> availableID_colID;
    @FXML
    private TableColumn<categories, String> availableID_colFd;
    @FXML
    private TableColumn<categories, String> availableID_col_size;
    @FXML
    private TableColumn<categories, String> availableID_colPrice;
    @FXML
    private TableColumn<categories, String> availableID_col_Status;
    @FXML
    private TableColumn<product, String> order_col_ID;
    @FXML
    private TableColumn<product, String> order_col_FD;
    @FXML
    private TableColumn<product, String> order_col_size;
    @FXML
    private TableColumn<product, String> order_col_price;
    @FXML
    private TableColumn<product, String> order_col_quantity;
    @FXML
    private Label username;

    // [KHAI BÁO BIẾN ĐÃ SỬA LỖI]
    private double totalP = 0;
    private double amount;
    private double balance;
    private int customerId;
    private int qty;
    private ObjectId selectedOrderItemId;

    private ObservableList<categories> availableIDList;
    private ObservableList<product> orderData;
    private SpinnerValueFactory<Integer> spinner;

    // [CONSTANTS]
    private final String[] STATUS_LIST = {"Còn hàng ", "Hết hàng"};
    private final String[] SIZE_LIST = {"S", "M", "L"};

    // =========================================================================
    // MONGODB UTILS (Đã chuyển đổi logic JDBC sang MongoDB)
    // =========================================================================
    public void orderCustomerId() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        int maxIdProduct = 0;
        int maxIdOrders = 0;

        try {
            MongoCollection<Document> productCollection = db.getCollection("products");
            Document maxProdDoc = productCollection.find().sort(Sorts.descending("customer_id")).limit(1).first();
            if (maxProdDoc != null && maxProdDoc.containsKey("customer_id")) {
                maxIdProduct = maxProdDoc.getInteger("customer_id");
            }

            MongoCollection<Document> ordersCollection = db.getCollection("orders");
            Document maxOrderDoc = ordersCollection.find().sort(Sorts.descending("customer_id")).limit(1).first();
            if (maxOrderDoc != null && maxOrderDoc.containsKey("customer_id")) {
                maxIdOrders = maxOrderDoc.getInteger("customer_id");
            }

            customerId = Math.max(maxIdProduct, maxIdOrders) + 1;
            if (customerId == 0) {
                customerId = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardNC() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> collection = db.getCollection("orders");
        try {
            long nc = collection.countDocuments();
            dashboard_NC.setText(String.valueOf(nc));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardTI() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> collection = db.getCollection("orders");
        double ti = 0;

        java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());

        Bson match = Aggregates.match(Filters.eq("date", sqlDate));
        Bson group = Aggregates.group(null, new BsonField("totalToday", new Document("$sum", "$total")));
        List<Bson> pipeline = Arrays.asList(match, group);

        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                ti = doc.getDouble("totalToday");
            }
            dashboard_TI.setText("$" + String.valueOf(ti));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboardTIncome() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> collection = db.getCollection("orders");
        double ti = 0;

        Bson group = Aggregates.group(null, new BsonField("totalIncome", new Document("$sum", "$total")));
        List<Bson> pipeline = Arrays.asList(group);

        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                ti = doc.getDouble("totalIncome");
            }
            dashboard_Tincome.setText("$" + String.valueOf(ti));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dashboard_NCChart() {
        // Logic MongoDB Aggregation Pipeline theo ngày cho Count
    }

    public void dashboard_ICChart() {
        // Logic MongoDB Aggregation Pipeline theo ngày cho Sum(Total)
    }

    // =========================================================================
    // AVAILABLE PRODUCTS (Categories CRUD)
    // =========================================================================
    public void availableIDAdd() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> collection = db.getCollection("categories");

        String productID = availableID_ID.getText();
        String productName = availableID_Name.getText();
        String type = (String) availableID_Size.getSelectionModel().getSelectedItem();
        String priceText = availableID_Price.getText();
        String status = (String) availableID_Status.getSelectionModel().getSelectedItem();

        Alert alert;

        if (productID.isEmpty() || productName.isEmpty() || type == null || priceText.isEmpty() || status == null) {
            alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi!!!");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập đầy đủ thông tin");
            alert.showAndWait();
            return;
        }

        try {
            Double price = Double.parseDouble(priceText);

            Document checkDoc = collection.find(Filters.eq("product_id", productID)).first();

            if (checkDoc != null) {
                alert = new Alert(AlertType.ERROR);
                alert.setTitle("Lỗi!!!");
                alert.setHeaderText(null);
                alert.setContentText("ID sản phẩm: " + productID + " đã tồn taị!");
                alert.showAndWait();
            } else {
                Document newCategory = new Document("product_id", productID)
                        .append("product_name", productName)
                        .append("type", type)
                        .append("price", price)
                        .append("status", status);

                collection.insertOne(newCategory);

                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Thành Công!!");
                alert.setHeaderText(null);
                alert.setContentText("Thêm thành công!!");
                alert.showAndWait();

                availableIDShowData();
                avalibleFClear();
            }

        } catch (NumberFormatException e) {
            alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi!!!");
            alert.setHeaderText(null);
            alert.setContentText("Giá (Price) phải là số hợp lệ.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void avalibleDelete() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        String productID = availableID_ID.getText();

        Alert alert;
        if (productID.isEmpty()) {
            alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi!!!");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn hoặc nhập ID sản phẩm để xoá.");
            alert.showAndWait();
            return;
        }

        alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xoá?");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc là muốn xoá sản phẩm: " + productID + "?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get().equals(ButtonType.OK)) {
            try {
                MongoCollection<Document> collection = db.getCollection("categories");
                collection.deleteOne(Filters.eq("product_id", productID));

                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Thành Công!!");
                alert.setHeaderText(null);
                alert.setContentText("Xóa thành công!");
                alert.showAndWait();

                availableIDShowData();
                avalibleFClear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void avalibleUpadate() {
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        String productID = availableID_ID.getText();
        String productName = availableID_Name.getText();
        String type = (String) availableID_Size.getSelectionModel().getSelectedItem();
        String priceText = availableID_Price.getText();
        String status = (String) availableID_Status.getSelectionModel().getSelectedItem();

        // ... (Kiểm tra input rỗng) ...
        try {
            Double price = Double.parseDouble(priceText);

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Update?");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc là muốn cập nhật sản phẩm: " + productID + "?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get().equals(ButtonType.OK)) {
                MongoCollection<Document> collection = db.getCollection("categories");

                Bson filter = Filters.eq("product_id", productID);
                Bson update = Updates.combine(
                        Updates.set("product_name", productName),
                        Updates.set("type", type),
                        Updates.set("price", price),
                        Updates.set("status", status)
                );

                collection.updateOne(filter, update);

                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Thành Công!!");
                alert.setHeaderText(null);
                alert.setContentText("Cập nhật thành công!");
                alert.showAndWait();

                availableIDShowData();
                avalibleFClear();

            }
        } catch (NumberFormatException e) {
            // Cần thêm Alert lỗi
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void avalibleFClear() {
        availableID_ID.setText("");
        availableID_Name.setText("");
        availableID_Size.getSelectionModel().clearSelection();
        availableID_Price.setText("");
        availableID_Status.getSelectionModel().clearSelection();
    }

    public ObservableList<categories> avalibleIDListData() {
        ObservableList<categories> listData = FXCollections.observableArrayList();
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return listData;
        }
        MongoCollection<Document> collection = db.getCollection("categories");
        try (MongoCursor<Document> cursor = collection.find().iterator()) { // Đã sửa
            while (cursor.hasNext()) {
                Document doc = cursor.next();

                // XỬ LÝ LỖI: Đảm bảo trường price được lấy ra đúng kiểu Double (Như đã thảo luận)
                Double price;
                Object priceObj = doc.get("price");

                if (priceObj instanceof Integer) {
                    price = ((Integer) priceObj).doubleValue();
                } else if (priceObj instanceof Long) {
                    price = ((Long) priceObj).doubleValue();
                } else if (priceObj instanceof Double) {
                    price = (Double) priceObj;
                } else {
                    price = 0.0;
                }

                categories cat = new categories(
                        doc.getString("product_id"),
                        doc.getString("product_name"),
                        doc.getString("type"),
                        price,
                        doc.getString("status")
                );

                listData.add(cat);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu categories: " + e.getMessage());
            e.printStackTrace();
        }
        return listData;
    }

    public void avalibleSearch() {
        FilteredList<categories> filter = new FilteredList<>(availableIDList, e -> true);
        availableID_search.textProperty().addListener((observabl, newValue, oldValue) -> {

            filter.setPredicate(predicateCategories -> {

                if (newValue.isEmpty() || newValue == null) {
                    return true;
                }
                String searchKey = newValue.toLowerCase();

                if (predicateCategories.getProductID().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicateCategories.getName().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicateCategories.getType().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicateCategories.getPrice().toString().contains(searchKey)) {
                    return true;
                } else if (predicateCategories.getStatus().toLowerCase().contains(searchKey)) {
                    return true;
                } else {
                    return false;
                }
            });
        });

        SortedList<categories> sortList = new SortedList<>(filter);

        sortList.comparatorProperty().bind(availableID_tableview.comparatorProperty());
        availableID_tableview.setItems(sortList);
    }

    public void availableIDShowData() {

        availableIDList = avalibleIDListData();

        availableID_colID.setCellValueFactory(new PropertyValueFactory<>("productID"));
        availableID_colFd.setCellValueFactory(new PropertyValueFactory<>("name"));
        availableID_col_size.setCellValueFactory(new PropertyValueFactory<>("type"));
        availableID_colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        availableID_col_Status.setCellValueFactory(new PropertyValueFactory<>("status"));

        availableID_tableview.setItems(availableIDList);
    }

    public void avalableFDSelect() {

        categories catData = availableID_tableview.getSelectionModel().getSelectedItem();

        if (catData == null) {
            return;
        }

        availableID_ID.setText(catData.getProductID());
        availableID_Name.setText(catData.getName());
        availableID_Price.setText(String.valueOf(catData.getPrice()));

    }

    public void availableIDStatus() {
        ObservableList listdata = FXCollections.observableArrayList(STATUS_LIST);
        availableID_Status.setItems(listdata);

    }

    public void availableID_Size() {
        ObservableList listdata = FXCollections.observableArrayList(SIZE_LIST);
        availableID_Size.setItems(listdata);

    }

    // =========================================================================
    // ORDER FUNCTIONS (Giỏ hàng products và Thanh toán orders)
    // =========================================================================
    public void orderAdd() {
        orderCustomerId();
        orderQuantity();

        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> productCollection = db.getCollection("product");

        try {
            String productId = (String) order_productID.getSelectionModel().getSelectedItem();
            String productName = (String) order_productName.getSelectionModel().getSelectedItem();

            if (productId == null || qty <= 0) {
                // Cần thêm Alert lỗi
                return;
            }

            MongoCollection<Document> categoriesCollection = db.getCollection("categories");
            Document categoryDoc = categoriesCollection.find(Filters.eq("product_id", productId)).first();

            String orderType = "";
            double orderPrice = 0;

            if (categoryDoc != null) {
                orderType = categoryDoc.getString("type");
                orderPrice = categoryDoc.getDouble("price");
            } else {
                // Cần thêm Alert lỗi
                return;
            }

            double totalPriceForThisItem = orderPrice * qty;

            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());

            Document newProduct = new Document("customer_id", customerId)
                    .append("product_id", productId)
                    .append("product_name", productName)
                    .append("type", orderType)
                    .append("price", totalPriceForThisItem)
                    .append("quantity", qty)
                    .append("date", sqlDate);

            productCollection.insertOne(newProduct);

            orderDisplayTotal();
            orderDisplayData();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void orderPay() {
        orderTotal();

        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }
        MongoCollection<Document> ordersCollection = db.getCollection("orders");

        try {

            if (totalP <= 0) {
                // Cần thêm Alert lỗi
                return;
            }

            String amountText = order_amount.getText().trim();
            if (amountText.isEmpty()) {
                // Cần thêm Alert lỗi
                return;
            }

            amount = Double.parseDouble(amountText);

            if (amount < totalP) {
                // Cần thêm Alert lỗi
                return;
            }

            balance = amount - totalP;

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Thanh toán");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc thanh toán?");
            Optional<ButtonType> option = alert.showAndWait();

            if (option.get().equals(ButtonType.OK)) {

                java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());

                Document orderInfo = new Document("customer_id", customerId)
                        .append("total", totalP)
                        .append("date", sqlDate);

                ordersCollection.insertOne(orderInfo);

                MongoCollection<Document> productCollection = db.getCollection("products");
                productCollection.deleteMany(Filters.eq("customer_id", customerId));

                // [Alert Thành công]
                // Reset lại giao diện
                order_total.setText("$0.0");
                order_balance.setText("$0.0");
                order_amount.setText("");
                totalP = 0;
                balance = 0;

                orderDisplayData();
            }

        } catch (NumberFormatException e) {
            // Cần thêm Alert lỗi
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void orderTotal() {
        orderCustomerId();

        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        MongoCollection<Document> collection = db.getCollection("products");
        totalP = 0;

        Bson match = Aggregates.match(Filters.eq("customer_id", customerId));
        Bson group = Aggregates.group(null, new BsonField("totalSum", new Document("$sum", "$price")));
        List<Bson> pipeline = Arrays.asList(match, group);

        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                totalP = doc.getDouble("totalSum");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void orderAmount() {
        orderTotal();

        Alert alert;

        if (order_amount.getText().isEmpty() || order_amount.getText() == null
                || order_amount.getText() == "") {
            alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Please type the amount!");
            alert.showAndWait();
        } else {
            amount = Double.parseDouble(order_amount.getText());

            if (amount < totalP) {
                order_amount.setText("");
            } else {
                balance = (amount - totalP);
                order_balance.setText("$" + String.valueOf(balance));
            }
        }
    }

    public void orderDisplayTotal() {
        orderTotal();
        order_total.setText("$" + String.valueOf(totalP));

    }

    public ObservableList<product> orderListData() {
        orderCustomerId();

        ObservableList<product> listData = FXCollections.observableArrayList();

        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return listData;
        }

        MongoCollection<Document> collection = db.getCollection("products");
        Bson filter = Filters.eq("customer_id", customerId);

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                // SỬA LỖI: Dùng constructor nhận ObjectId
                product prod = new product(
                        doc.getObjectId("_id"),
                        doc.getString("product_id"),
                        doc.getString("product_name"),
                        doc.getString("type"),
                        doc.getDouble("price"),
                        doc.getInteger("quantity")
                );

                listData.add(prod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;

    }

    public void orderRecipt() {
        // ...
    }

    public void orderRemove() {

        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        if (selectedOrderItemId == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn vật phẩm trước!!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có muốn xóa?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get().equals(ButtonType.OK)) {
            try {
                MongoCollection<Document> collection = db.getCollection("products");

                // Xóa Document chính xác bằng ObjectId
                collection.deleteOne(Filters.eq("_id", selectedOrderItemId));

                // [Alert Thành công]
                orderDisplayData();
                orderDisplayTotal();

                order_amount.setText("");
                order_balance.setText("$0.0");
                selectedOrderItemId = null;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void orderSelectData() {

        product prod = order_tableview.getSelectionModel().getSelectedItem();

        if (prod == null) {
            return;
        }

        // LƯU Ý: Đã sửa lỗi get_id()
        selectedOrderItemId = prod.get_id();
    }

    public void orderDisplayData() {
        orderData = orderListData();
        order_col_ID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        order_col_FD.setCellValueFactory(new PropertyValueFactory<>("name"));
        order_col_size.setCellValueFactory(new PropertyValueFactory<>("type"));
        order_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        order_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        order_tableview.setItems(orderData);
    }

    public void orderProductID() { // KHẮC PHỤC LỖI DÒNG 778
        MongoDatabase db = Database.connectDB();
    if (db == null) return;
    MongoCollection<Document> collection = db.getCollection("categories");

    // SỬA LỖI: Dùng chuỗi "Còn hàng" (KHÔNG có khoảng trắng ở cuối)
    Bson filter = Filters.eq("status", "Còn hàng"); 

    ObservableList listData = FXCollections.observableArrayList();

    try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            listData.add(doc.getString("product_id"));
        }
        order_productID.setItems(listData);
        orderProductName(); // Gọi hàm tiếp theo

    } catch (Exception e) {
        e.printStackTrace();
    }
    }

    public void orderProductName() { // KHẮC PHỤC LỖI DÒNG 779
        MongoDatabase db = Database.connectDB();
        if (db == null) {
            return;
        }

        String selectedProductID = (String) order_productID.getSelectionModel().getSelectedItem();

        if (selectedProductID == null) {
            return;
        }

        MongoCollection<Document> collection = db.getCollection("categories");
        Bson filter = Filters.eq("product_id", selectedProductID);

        ObservableList listData = FXCollections.observableArrayList();

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            if (cursor.hasNext()) {
                Document doc = cursor.next();
                listData.add(doc.getString("product_name"));
            }
            order_productName.setItems(listData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void orderSpinner() {
        spinner = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);

        order_quanitity.setValueFactory(spinner);
    }

    public void orderQuantity() {
        qty = order_quanitity.getValue();
    }

    private double x = 0;
    private double y = 0;

    public void switchForm(ActionEvent event) {
        if (event.getSource() == dashboard_btn) {
            dashboard_form.setVisible(true);
            availableID_form.setVisible(false);
            order_form.setVisible(false);

            dashboard_btn.setStyle("-fx-background-color:#25bcbf; -fx-text-fill: #fff; -fx-border-width: 0px;");
            available_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");
            order_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");

            dashboardNC();
            dashboardTI();
            dashboardTIncome();
            dashboard_NCChart();
            dashboard_ICChart();

        } else if (event.getSource() == available_btn) {
            dashboard_form.setVisible(false);
            availableID_form.setVisible(true);
            order_form.setVisible(false);

            available_btn.setStyle("-fx-background-color:#25bcbf; -fx-text-fill: #fff; -fx-border-width: 0px;");
            dashboard_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");
            order_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");

            availableIDShowData();
            avalibleSearch();

        } else if (event.getSource() == order_btn) {
            dashboard_form.setVisible(false);
            availableID_form.setVisible(false);
            order_form.setVisible(true);

            order_btn.setStyle("-fx-background-color:#25bcbf; -fx-text-fill: #fff; -fx-border-width: 0px;");
            available_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");
            dashboard_btn.setStyle("-fx-background-color: transparent; -fx-border-width: 1px; -fx-text-fill: #000;");

            orderProductID();
            orderProductName();
            orderSpinner();
            orderDisplayData();
            orderDisplayTotal();
        }

    }

    public void logout() {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout");
            alert.setHeaderText(null);
            alert.setContentText("Bạn muốn đăng xuất?");
            Optional<ButtonType> option = alert.showAndWait();
            //Trả về màn hình đăng nhập
            if (option.get().equals(ButtonType.OK)) {
                logout.getScene().getWindow().hide();

                Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);

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

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayUsername() {
        String user = data.username;
        user = user.substring(0, 1).toUpperCase() + user.substring(1);
        username.setText(user);

    }

    public void close() {
        System.exit(0);
    }

    public void minimize() {
        Stage stage = (Stage) main_form.getScene().getWindow();
        stage.setIconified(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        dashboardNC();
        dashboardTI();
        dashboardTIncome();

        displayUsername();
        availableIDStatus();
        availableID_Size();
        availableIDShowData();
        avalibleFClear();

        orderProductID();
        orderProductName();
        orderSpinner();

        orderDisplayData();
        orderDisplayTotal();
    }

}
