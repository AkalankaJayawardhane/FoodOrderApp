package com.example.foodorderapp.activities;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapters.CartAdapter;
import com.example.foodorderapp.database.DBHelper;
import com.example.foodorderapp.models.CartItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Checkout Activity - Order confirmation screen
 * Displays order summary and delivery address input
 */
public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView rvOrderSummary;
    private TextView tvTotalPrice;
    private TextInputEditText etDeliveryAddress;
    private Button btnPlaceOrder;

    private DBHelper dbHelper;
    private List<CartItem> cartItems;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize database
        dbHelper = new DBHelper(this);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences("FoodAppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Initialize views
        rvOrderSummary = findViewById(R.id.rvOrderSummary);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Load cart items
        loadOrderSummary();

        // Place order button
        btnPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeOrder();
            }
        });
    }

    /**
     * Load order summary
     */
    private void loadOrderSummary() {
        if (userId != -1) {
            cartItems = dbHelper.getCartItems(userId);

            // Set up RecyclerView (read-only, no controls needed)
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvOrderSummary.setLayoutManager(layoutManager);

            CartAdapter adapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartItemListener() {
                @Override
                public void onQuantityChanged(CartItem cartItem, int newQuantity) {
                    // Not allowing changes at checkout
                }

                @Override
                public void onItemRemoved(CartItem cartItem) {
                    // Not allowing removal at checkout
                }
            });
            rvOrderSummary.setAdapter(adapter);

            // Calculate and display total
            int total = dbHelper.getCartTotal(userId);
            tvTotalPrice.setText(total + " RS");
        }
    }

    /**
     * Place order
     */
    private void placeOrder() {
        String address = etDeliveryAddress.getText().toString().trim();

        // Validate address
        if (TextUtils.isEmpty(address)) {
            etDeliveryAddress.setError(getString(R.string.address_required));
            etDeliveryAddress.requestFocus();
            return;
        }

        // Clear cart after order
        dbHelper.clearCart(userId);

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to Order Success Activity
        Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
        startActivity(intent);
        finish();
    }
}
