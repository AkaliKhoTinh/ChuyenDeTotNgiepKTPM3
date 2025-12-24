
package quanlymongodb;


public class categories {
    // Thuộc tính
    private String productID;
    private String name; // product_name
    private String type; // Ví dụ: Đồ ăn, Nước uống
    private Double price;
    private String status; // Ví dụ: Còn hàng, Hết hàng

    // =========================================================================
    // CONSTRUCTOR (Bắt buộc cho việc đọc dữ liệu từ MongoDB)
    // =========================================================================

    public categories(String productID, String name, String type, Double price, String status) {
        this.productID = productID;
        this.name = name;
        this.type = type;
        this.price = price;
        this.status = status;
    }
    
    // =========================================================================
    // GETTERS (Bắt buộc cho JavaFX TableView)
    // =========================================================================

    public String getProductID() {
        return productID;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }
    
    // =========================================================================
    // SETTERS (Dùng để cập nhật dữ liệu)
    // =========================================================================
    
    public void setProductID(String productID) {
        this.productID = productID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
