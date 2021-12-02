package com.myshoppal.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.myshoppal.R
import com.myshoppal.firestore.FirestoreClass
import com.myshoppal.models.CartItem
import com.myshoppal.models.Product
import com.myshoppal.ui.adapters.CartItemsListAdapter
import kotlinx.android.synthetic.main.activity_cart_list.*

/**
 * Cart list activity of the application.
 */
class CartListActivity : BaseActivity() {

    // A global variable for the product list.
    private lateinit var mProductsList: ArrayList<Product>

    // A global variable for the cart list items.
    private lateinit var mCartListItems: ArrayList<CartItem>

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_cart_list)

        setupActionBar()
    }

    override fun onResume() {
        super.onResume()
        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
    }


    /**
     * A function to get the success result of product list.
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        hideProgressDialog()
        // Initialize mProductList var
        mProductsList = productsList

        // Only after we successfully get our products list, can we get the Cart Items List
        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CartListActivity)
    }


    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CartListActivity)
    }


    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     */
    fun successCartItemsList(cartList: ArrayList<CartItem>) {

        // Hide progress dialog.
        hideProgressDialog()

        // For Testing Purposes
//        for(i in cartList) {
//            Log.i("Cart Item Title", i.title)
//        }


        // Go through all the product list, and through all the cart item inside the Cart List
        for (product in mProductsList) {
            for (cartItem in cartList) {
                // Check if the product id is same as the cart item id
                if (product.product_id == cartItem.product_id) {

                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        // Initialize mCartListItems var
        mCartListItems = cartList

        // If Cart holds Item/s
        if (mCartListItems.size > 0) {

            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0

            // Calculates the total price of each item in the cart
            for (item in mCartListItems) {

                val availableQuantity = item.stock_quantity.toInt()

                // Checks stock quantity before calculating
                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()

                    subTotal += (price * quantity)
                }
            }

            tv_sub_total.text = "₱$subTotal"
            // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
            tv_shipping_charge.text = "₱50.0"

            if (subTotal > 0) {
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + 50
                tv_total_amount.text = "₱$total"
            } else {
                ll_checkout.visibility = View.GONE
            }

        } else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    /**
     * A function to notify the user about the item removed from the cart list.
     */
    fun itemRemovedSuccess() {

        hideProgressDialog()

        Toast.makeText(
            this@CartListActivity,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getCartItemsList()
    }

    /**
     * A function to notify the user about the item quantity updated in the cart list.
     */
    fun itemUpdateSuccess() {

        hideProgressDialog()
        getCartItemsList()
    }

}