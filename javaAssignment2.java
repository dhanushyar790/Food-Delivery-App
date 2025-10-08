package javaassignmet2;{
//MenuItem.java
public class MenuItem {
    private final int id;
    private final String name;
    private final double price;
    private final boolean available;

    public MenuItem(int id, String name, double price, boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.available = available;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    @Override
    public String toString() {
        return String.format("%s (%.2f)", name, price);
    }}

//Restaurant.java
import java.util.*;

public class Restaurant {
    private final String name;
    private final List<MenuItem> menu;

    public Restaurant(String name) {
        this.name = name;
        this.menu = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<MenuItem> getMenu() { return menu; }

    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }

    @Override
    public String toString() {
        return "Restaurant: " + name;
    }
//Cart.java
import java.util.*;

public class Cart {
    private final int id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Map<Integer, CartItem> items;

    public Cart(int id, Customer customer, Restaurant restaurant) {
        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = new LinkedHashMap<>();
    }

    public Customer getCustomer() { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public Collection<CartItem> getItems() { return items.values(); }

    public boolean addItem(MenuItem menuItem, int qty) {
        if (!menuItem.isAvailable()) return false;
        CartItem existing = items.get(menuItem.getId());
        if (existing == null) {
            items.put(menuItem.getId(), new CartItem(menuItem, qty));
        } else {
            existing.setQuantity(existing.getQuantity() + qty);
        }
        return true;
    }

    public double itemsTotal() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.getTotalPrice();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cart:\n");
        for (CartItem item : items.values()) {
            sb.append(" ").append(item).append("\n");
        }
        sb.append("Items Total: ").append(itemsTotal()).append("\n");
        return sb.toString();
    }
//CartItem.java
public class CartItem {
    private final MenuItem item;
    private int quantity;

    public CartItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int q) { this.quantity = q; }

    public double getTotalPrice() {
        return item.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s x%d = %.2f", item.getName(), quantity, getTotalPrice());
    }
//Customer.java
public class Customer {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final String address;

    public Customer(int id, String firstName, String lastName, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public int getId() { return id; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return String.format("Customer [%d] %s - %s", id, getFullName(), address);
    }
//Order.java
import java.util.*;

public class Order {
    private final int id;
    private final Cart cart;
    private PaymentStatus paymentStatus;
    private final Delivery delivery;
    private final Date placedAt;

    private final double deliveryFee;
    private final double taxPercent;

    public Order(int id, Cart cart, double deliveryFee, double taxPercent) {
        this.id = id;
        this.cart = cart;
        this.deliveryFee = deliveryFee;
        this.taxPercent = taxPercent;
        this.paymentStatus = PaymentStatus.PENDING;
        this.delivery = new Delivery(this);
        this.placedAt = new Date();
    }

    public int getId() { return id; }
    public Cart getCart() { return cart; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus ps) { this.paymentStatus = ps; }
    public Delivery getDelivery() { return delivery; }

    public double itemsTotal() { return cart.itemsTotal(); }
    public double taxAmount() { return itemsTotal() * taxPercent / 100.0; }
    public double total() { return itemsTotal() + taxAmount() + deliveryFee; }

    @Override
    public String toString() {
        return receipt();
    }

    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- ORDER RECEIPT ---\n");
        sb.append("Order ID: ").append(id).append("\n");
        sb.append("Placed at: ").append(placedAt).append("\n");
        sb.append(cart.toString());
        sb.append(String.format("Delivery Fee: %.2f\n", deliveryFee));
        sb.append(String.format("Tax (%.2f%%): %.2f\n", taxPercent, taxAmount()));
        sb.append(String.format("TOTAL: %.2f\n", total()));
        sb.append("Payment status: ").append(paymentStatus).append("\n");
        sb.append("Delivery timeline:\n");
        for (DeliveryStatus ds : delivery.getHistory()) {
            sb.append(" - ").append(ds).append("\n");
        }
        sb.append("---------------------\n");
        return sb.toString();
    }
//Delivery.java
import java.util.*;

enum DeliveryStatus {
    ORDERED, CONFIRMED, PREPARING, PICKED_UP, ON_THE_WAY, DELIVERED
}

enum PaymentStatus {
    SUCCESS, PENDING, FAILED
}

public class Delivery {
    private final Order order;
    private final List<DeliveryStatus> history;
    private DeliveryStatus current;

    public Delivery(Order order) {
        this.order = order;
        this.history = new ArrayList<>();
        this.current = DeliveryStatus.ORDERED;
        history.add(current);
    }

    public DeliveryStatus getCurrent() { return current; }
    public List<DeliveryStatus> getHistory() { return Collections.unmodifiableList(history); }

    public boolean advance() {
        if (order.getPaymentStatus() != PaymentStatus.SUCCESS) return false;
        DeliveryStatus next = switch (current) {
            case ORDERED -> DeliveryStatus.CONFIRMED;
            case CONFIRMED -> DeliveryStatus.PREPARING;
            case PREPARING -> DeliveryStatus.PICKED_UP;
            case PICKED_UP -> DeliveryStatus.ON_THE_WAY;
            case ON_THE_WAY -> DeliveryStatus.DELIVERED;
            case DELIVERED -> null;
        };
        if (next != null) {
            current = next;
            history.add(current);
            return true;
        }
        return false;
    }
//FoodDeliveryApp.java
import java.util.*;

public class FoodDeliveryApp {
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Restaurant> restaurants = new ArrayList<>();
    private static final List<Order> orders = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- Food Delivery App ---");
            System.out.println("1. Add Restaurant");
            System.out.println("2. Add Menu Item to Restaurant");
            System.out.println("3. Create Customer & Place Order");
            System.out.println("4. Show Orders");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addRestaurant();
                case 2 -> addMenuItem();
                case 3 -> placeOrder();
                case 4 -> showOrders();
                case 5 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addRestaurant() {
        System.out.print("Enter restaurant name: ");
        String name = sc.nextLine();
        restaurants.add(new Restaurant(name));
        System.out.println("Restaurant added.");
    }

    private static void addMenuItem() {
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants available.");
            return;
        }
        System.out.println("Select restaurant:");
        for (int i = 0; i < restaurants.size(); i++) {
            System.out.println((i + 1) + ". " + restaurants.get(i).getName());
        }
        int choice = sc.nextInt();
        sc.nextLine();
        Restaurant rest = restaurants.get(choice - 1);

        System.out.print("Enter food name: ");
        String food = sc.nextLine();
        System.out.print("Enter price: ");
        double price = sc.nextDouble();
        sc.nextLine();

        rest.addMenuItem(new MenuItem(rest.getMenu().size() + 1, food, price, true));
        System.out.println("Item added.");
    }

    private static void placeOrder() {
        if (restaurants.isEmpty()) {
            System.out.println("No restaurants available.");
            return;
        }

        System.out.print("Enter first name: ");
        String fname = sc.nextLine();
        System.out.print("Enter last name: ");
        String lname = sc.nextLine();
        System.out.print("Enter address: ");
        String addr = sc.nextLine();
        Customer cust = new Customer(orders.size() + 1, fname, lname, addr);

        System.out.println("Select restaurant:");
        for (int i = 0; i < restaurants.size(); i++) {
            System.out.println((i + 1) + ". " + restaurants.get(i).getName());
        }

        int rchoice = sc.nextInt();
        sc.nextLine();
        if (rchoice < 1 || rchoice > restaurants.size()) {
            System.out.println("Invalid restaurant choice.");
            return;
        }
        Restaurant rest = restaurants.get(rchoice - 1);

        Cart cart = new Cart(orders.size() + 1, cust, rest);
        while (true) {
            System.out.println("Menu:");
            for (int i = 0; i < rest.getMenu().size(); i++) {
                MenuItem mi = rest.getMenu().get(i);
                System.out.println((i + 1) + ". " + mi.getName() + " - " + mi.getPrice());
            }
            System.out.println("0. Finish");

            int mchoice = sc.nextInt();
            sc.nextLine();
            if (mchoice == 0) break; // finish order

            // Validate menu choice
            if (mchoice < 1 || mchoice > rest.getMenu().size()) {
                System.out.println("Invalid menu choice. Try again.");
                continue;
            }

            System.out.print("Enter quantity: ");
            int qty = sc.nextInt();
            sc.nextLine();
            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                continue;
            }

            cart.addItem(rest.getMenu().get(mchoice - 1), qty);
            System.out.println("Item added to cart.");
        }

        if (cart.getItems().isEmpty()) {
            System.out.println("Cart is empty. Order not placed.");
            return;
        }

        Order order = new Order(orders.size() + 1, cart, 50.0, 5.0); // delivery fee 50, tax 5%
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orders.add(order);
        System.out.println("Order placed:\n" + order.receipt());
    }


    private static void showOrders() {
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.println(o.receipt());
        }
    }

