package com.swp391.coding_platform.entity.cart;

import com.swp391.coding_platform.entity.user.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartEntityTest {

    @Test
    void testConstructorWithUser() {
        UserEntity user = new UserEntity();
        CartEntity cart = new CartEntity(user);
        assertEquals(user, cart.getUser());
    }

    @Test
    void addItem() {
        CartEntity cart = new CartEntity();
        CartItemEntity item = new CartItemEntity();

        cart.addItem(item);

        assertTrue(cart.getItems().contains(item));
        assertEquals(cart, item.getCart());
    }

    @Test
    void removeItem() {
        CartEntity cart = new CartEntity();
        CartItemEntity item = new CartItemEntity();

        cart.addItem(item); // Add first
        assertTrue(cart.getItems().contains(item));

        cart.removeItem(item);

        assertFalse(cart.getItems().contains(item));
        assertNull(item.getCart());
    }
}
