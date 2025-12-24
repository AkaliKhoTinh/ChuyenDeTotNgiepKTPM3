package quanlymongodb;

import org.bson.types.ObjectId;

public class product {
    
    private ObjectId _id; 
    private Integer id; 
    private String productId;
    private String name;
    private String type;
    private Double price;
    private Integer quantity;
    
    // Constructor cho việc đọc dữ liệu từ MongoDB (Fix lỗi Dòng 640)
    public product(ObjectId _id, String productId, String name, String type, Double price, Integer quantity){
        this._id = _id;
        // Ánh xạ ObjectId sang Integer cho TableView
        this.id = (_id != null) ? _id.hashCode() : 0; 
        this.productId = productId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }
    
    // Constructor cho việc tạo Object mới trong Java
    public product(String productId, String name, String type, Double price, Integer quantity){
        this.productId = productId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }
    
    // Getter cho ObjectId (Fix lỗi Dòng 713: get_id())
    public ObjectId get_id() {
        return _id;
    }
    
    public Integer getId(){ 
        return id;
    }
    public String getProductId(){
        return productId;
    }
    public String getName(){
        return name;
    }
    public String getType(){
        return type;
    }
    public Double getPrice(){
        return price;
    }
    public Integer getQuantity(){
        return quantity;
    }
    
    // Setters
    public void set_id(ObjectId _id) {
        this._id = _id;
        this.id = (_id != null) ? _id.hashCode() : 0;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}