
package quanlymongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Database {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; 
    private static final String DB_NAME = "Restaurant"; 

    
    public static MongoDatabase connectDB() {
        if (database == null) {
            try {
                // Khởi tạo MongoClient và MongoDatabase
                mongoClient = MongoClients.create(CONNECTION_STRING);
                database = mongoClient.getDatabase(DB_NAME);
                System.out.println("Kết nối MongoDB thành công!");
            } catch (Exception e) {
                System.err.println("Lỗi kết nối MongoDB: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return database;
    }

    
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("Đã đóng kết nối MongoDB.");
        }
    }
}
