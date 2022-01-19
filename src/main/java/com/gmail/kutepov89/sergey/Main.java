package com.gmail.kutepov89.sergey;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static Connection conn;
    static DbProperties props = new DbProperties();

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);
//        initDb();
        try {
            while (true) {
                System.out.println("What do you want to do?");
                System.out.println("1 - add customer");
                System.out.println("2 - add product");
                System.out.println("3 - add order");
                System.out.println("4 - view customers");
                System.out.println("5 - view products");
                System.out.println("6 - view orders");
                System.out.println("-> ");

                String s = sc.nextLine();
                switch (s) {
                    case "1":
                        addCustomer(sc);
                        break;
                    case "2":
                        addProduct(sc);
                        break;
                    case "3":
                        addOrder(sc);
                        break;
                    case "4":
                        viewCustomers();
                        break;
                    case "5":
                        viewProducts();
                        break;
                    case "6":
                        viewOrders();
                        viewOrderProduct();
                        break;
                    default:
                        return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initDb() throws SQLException {
        try (Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword())) {
            try (Statement st = conn.createStatement()) {
                st.execute("DROP TABLE IF EXISTS Products");
                st.execute("CREATE TABLE Products (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY" +
                        ", name VARCHAR(32) NOT NULL" +
                        ", category VARCHAR(32) NOT NULL)");

                st.execute("DROP TABLE IF EXISTS Customers");
                st.execute("CREATE TABLE Customers (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY" +
                        ", first_name VARCHAR(32) NOT NULL" +
                        ", last_name VARCHAR(32) NOT NULL)");

                st.execute("DROP TABLE IF EXISTS Orders");
                st.execute("CREATE TABLE Orders (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY" +
                        ", customer_id INT NOT NULL" +
                        ", date DATE NOT NULL)");

                st.execute("DROP TABLE IF EXISTS OrderProduct");
                st.execute("CREATE TABLE OrderProduct (order_id INT NOT NULL REFERENCES Orders(id)" +
                        ", product_id INT NOT NULL REFERENCES Products(id))");
            } finally {
                conn.close();
            }
        }
    }

    private static void addCustomer(Scanner sc) throws SQLException {
        System.out.println("Enter customer first name");
        String firstName = sc.nextLine();
        System.out.println("Enter customer last name");
        String lastName = sc.nextLine();

        try (Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword())) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Customers (first_name, last_name) VALUES(?, ?)");
            try {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.executeUpdate();
            } finally {
                conn.close();
            }
        }
    }

    private static void addProduct(Scanner sc) throws SQLException {
        System.out.println("Enter product name");
        String productName = sc.nextLine();
        System.out.println("Enter product category");
        String categoryName = sc.nextLine();

        try (Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword())) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO Products (name, category) VALUES(?, ?)");
            try {
                ps.setString(1, productName);
                ps.setString(2, categoryName);
                ps.executeUpdate();
            } finally {
                conn.close();
            }
        }
    }

    private static void addOrder(Scanner sc) throws SQLException {
        Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());

        System.out.println();
        System.out.println("Enter id product");
        String productId = sc.nextLine();
        int prodId = Integer.parseInt(productId);

        System.out.println("Enter id customer");
        String customerId = sc.nextLine();
        int cusId = Integer.parseInt(customerId);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        try(PreparedStatement ps3 = conn.prepareStatement("INSERT INTO Orders (customer_id, date) VALUES(?, ?)")) {
            try {
                ps3.setInt(1, cusId);
                ps3.setString(2, dtf.format(now));
                ps3.executeUpdate();
                System.out.println("Order is processed.");
            } finally {
                conn.close();
            }
        }
        Connection conn2 = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
        try (PreparedStatement ps4 = conn2.prepareStatement("SELECT id FROM Orders WHERE customer_id = ?")) {
            ps4.setInt(1, cusId);
            ResultSet resultSet = ps4.executeQuery();

            resultSet.next();
            int ordId = resultSet.getInt("Id");
            Connection conn3 = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
            try(PreparedStatement ps3 = conn3.prepareStatement("INSERT INTO OrderProduct (order_id, product_id) VALUES(?, ?)")) {
                try {
                    ps3.setInt(1, ordId);
                    ps3.setInt(2, prodId);
                    ps3.executeUpdate();
                    System.out.println("Order is processed.");
                } finally {
                    conn3.close();
                }
            }
        }  finally {
            conn2.close();
        }
    }

    private static void viewCustomers() throws SQLException {
        System.out.println("Products: ");
        Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Customers")) {
            viewResult(conn, ps);
        }
    }

    private static void viewProducts() throws SQLException {
        System.out.println("Products: ");
        Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Products")) {
            viewResult(conn, ps);
        }
    }

    private static void viewOrders() throws SQLException {
        System.out.println("Orders: ");
        Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Orders")) {
            viewResult(conn, ps);
        }
    }

    private static void viewResult(Connection conn, PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();

            for (int i = 1; i <= md.getColumnCount(); i++)
                System.out.print(md.getColumnName(i) + "\t\t\t");
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.print(rs.getString(i) + "\t\t\t");
                }
                System.out.println();
            }
        } finally {
            conn.close();
        }
    }

    private static void viewOrderProduct() throws SQLException {
        System.out.println("OrderProduct: ");
        Connection conn = DriverManager.getConnection(props.getUrl(), props.getUser(), props.getPassword());
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM OrderProduct")) {
            viewResult(conn, ps);
        }
    }

}
