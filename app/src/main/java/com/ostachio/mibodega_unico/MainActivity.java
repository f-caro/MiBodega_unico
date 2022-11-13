package com.ostachio.mibodega_unico;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button storageBtn, shelvesBtn, cashierBtn, productsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     //   storageBtn = (Button) findViewById(R.id.button_storage);
     //   shelvesBtn = (Button) findViewById(R.id.button_shelves);
     //   cashierBtn = (Button) findViewById(R.id.button_cashier);
     //   productsBtn = (Button) findViewById(R.id.button_products);

      //  storageBtn.setOnClickListener(this);
      //  shelvesBtn.setOnClickListener(this);
      //  cashierBtn.setOnClickListener(this);
      //  productsBtn.setOnClickListener(this);


    }


    public void onClickStorage(View v) {
        if (v.getId() == R.id.button_storage) {
            Intent intent_storage_shelves = new Intent(this, Add_Product_to_Storage.class);
                intent_storage_shelves.putExtra("tablename", "STORAGE");
            startActivity(intent_storage_shelves);
        }
    }

    public void onClickShelves(View v) {
        if (v.getId() == R.id.button_shelves) {
            Intent intent_storage_shelves = new Intent(this, Add_Product_to_Storage.class);
                intent_storage_shelves.putExtra("tablename", "SHELVES");
            startActivity(intent_storage_shelves);
        }
    }

    public void onClickCashier(View v) {
        if (v.getId() == R.id.button_cashier) {
            Intent intent_cashier = new Intent(this, Add_Product_to_Cashier.class);
            intent_cashier.putExtra("tablename", "CASHIER");
            startActivity(intent_cashier);
        }
    }

    public void onClickProducts(View v){
        if (v.getId() == R.id.button_products){
            Intent intent_products = new Intent(this, products.class);

            startActivity(intent_products);
        }

    }

}
