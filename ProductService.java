package service;

import dao.ProductDAO;
import model.Product;

public class ProductService {

    ProductDAO dao = new ProductDAO();

    // Add Product
    public void addProduct(Product p) {
        dao.addProduct(p);
    }

    // Display Products
    public void displayProducts() {
        dao.viewProducts();
    }
}