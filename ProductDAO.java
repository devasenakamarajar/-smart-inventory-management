package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import model.Product;

public class ProductDAO {

    Connection con = DBConnection.getConnection();

    // CREATE
    public void addProduct(Product p) {

        try {

            String query = "INSERT INTO product VALUES(?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, p.getId());
            ps.setString(2, p.getName());
            ps.setInt(3, p.getQuantity());
            ps.setDouble(4, p.getPrice());

            ps.executeUpdate();
    
       } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ
    public void viewProducts() {

        try {

            String query = "SELECT * FROM product";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {

                System.out.println(
                        rs.getInt("id") + " " +
                        rs.getString("name") + " " +
                        rs.getInt("quantity") + " " +
                        rs.getDouble("price")
                );

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPDATE
    public void updateProduct(int id, int quantity, double price) {

        try {

            String update = "UPDATE product SET quantity=?, price=? WHERE id=?";

            PreparedStatement ps2 = con.prepareStatement(update);

            ps2.setInt(1, quantity);
            ps2.setDouble(2, price);
            ps2.setInt(3, id);

            ps2.executeUpdate();

           

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void deleteProduct(int id) {

        try {

            String delete = "DELETE FROM product WHERE id=?";

            PreparedStatement ps3 = con.prepareStatement(delete);

            ps3.setInt(1, id);

            ps3.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
      