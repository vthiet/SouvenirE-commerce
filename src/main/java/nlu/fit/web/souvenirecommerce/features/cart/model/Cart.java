package nlu.fit.web.souvenirecommerce.features.cart.model;

import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Cart implements Serializable {

    private Map<Long, CartItem> data = new HashMap<>();
    private User user;

    public Cart() {
     data = new HashMap<>();
    }


    public void addItem(Product product , int quantity) {
        addItem(product, quantity, product.getOriginalPrice());
    }

    public void addItem(Product product, int quantity, double price) {

        if (quantity <= 0) {quantity = 1;}
        if (data.get(product.getId()) != null) {
            data.get(product.getId()).setPrice(price);
            data.get(product.getId()).upQuantity(quantity);
        }
        else
            data.put(product.getId(), new CartItem(product, price, quantity));

        }

    public boolean  updateItem(Long productId , int quantity) {
        if (data.get(productId) == null) return false;
        if (quantity <= 0) {quantity = 1;}
        data.get(productId).setQuantity(quantity);
        return true;
    }

    public CartItem removeItem(Long productId) {
        if (data.get(productId) == null) return null;
        return data.remove(productId);

    }

    public List<CartItem> removeAllItems() {
        ArrayList<CartItem> cartItems = new ArrayList<>(data.values());
        data.clear();
        return cartItems;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(data.values());
    }

    public CartItem getItem(Long productId) {
        return data.get(productId);
    }



    public int totalQuantity() {
        AtomicInteger total = new AtomicInteger();
        // Duyệt qua danh sách các món hàng (data.values())
        data.values().forEach(item -> {
            total.addAndGet(item.getQuantity());
        });
        return total.get();
    }


    public double total() {
        AtomicReference<Double> total = new AtomicReference<>((double) 0);
        // Duyệt qua từng CartItem để tính tiền
        data.values().forEach(item -> {
            total.updateAndGet(v -> v + (item.getQuantity() * item.getPrice()));
        });
        return total.get();
    }

    public void updateCustomerInfor (User user) {
        this.user = user;

    }
    }
