package main;

import java.util.Scanner;

import dao.ProductDAO;
import model.Product;

public class Inventory {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ProductDAO pdao = new ProductDAO();

        while (true) {

            System.out.println("\n1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Update Product");
            System.out.println("4. Delete Product");
            System.out.println("5. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {

            case 1:

                System.out.print("Enter Product ID: ");
                int id = sc.nextInt();

                System.out.print("Enter Product Name: ");
                String name = sc.next();

                System.out.print("Enter Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Enter Price: ");
                double price = sc.nextDouble();

                Product p = new Product(id, name, quantity, price);
               

                pdao.addProduct(p);
                System.out.println("Product added Successfully");
                
                break;

            case 2:

                pdao.viewProducts();
                break;

            case 3:

                System.out.print("Enter Product ID: ");
                int uid = sc.nextInt();

                System.out.print("Enter New Quantity: ");
                int uq = sc.nextInt();

                System.out.print("Enter New Price: ");
                double up = sc.nextDouble();

                pdao.updateProduct(uid, uq, up);

                System.out.println("Product Updated Successfully");

                break;

            case 4:

                System.out.print("Enter Product ID to delete: ");
                int did = sc.nextInt();

                pdao.deleteProduct(did);

                System.out.println("Product Deleted Successfully");

                break;

            case 5:

                System.out.println("Exiting...");
                sc.close();
                return;

            default:

                System.out.println("Invalid choice");
            }
        }
    }
}