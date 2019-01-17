package com.back4app.stripepaymentgateway;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Map;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String PUBLISHABLE_KEY = "pk_test_D7bK2bKidLFrSPN4fweIe8ar";
    private Card card;
    private ProgressDialog progress;
    private Button purchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a demo test credit Card
        // You can pass the payment form data to create a Real Credit card
        // But you need to implement youself.

        card = new Card(
                "4242424242424242", //card number
                12, //expMonth
                2030,//expYear
                "123"//cvc
        );
        progress = new ProgressDialog(this);
        purchase = (Button) findViewById(R.id.purchase);
        purchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy();
            }
        });
    }

    private void buy(){
        boolean validation = card.validateCard();
        if(validation){
            startProgress("Validating Credit Card");
            new Stripe(this).createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        @Override
                        public void onError(Exception error) {
                            Toast.makeText(MainActivity.this,
                                    "Stripe -" + error.toString(),
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(Token token) {
                            finishProgress();
                            charge(token);
                        }
                    });
        } else if (!card.validateNumber()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The card number that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else if (!card.validateExpiryDate()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The expiration date that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else if (!card.validateCVC()) {
            Toast.makeText(MainActivity.this,
                    "Stripe - The CVC code that you entered is invalid",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this,
                    "Stripe - The card details that you entered are invalid",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void charge(Token cardToken){
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("ItemName", "test");
        params.put("cardToken", cardToken.getId());
        params.put("name","Dominic Wong");
        params.put("email","dominwong4@gmail.com");
        params.put("address","HIHI");
        params.put("zip","99999");
        params.put("city_state","CA");
        startProgress("Purchasing Item");
        ParseCloud.callFunctionInBackground("purchaseItem", params, new FunctionCallback<Object>() {
            public void done(Object response, ParseException e) {
                finishProgress();
                if (e == null) {
                    Log.d("Cloud Response", "There were no exceptions! " + response.toString());
                    Toast.makeText(getApplicationContext(),
                            "Item Purchased Successfully ",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d("Cloud Response", "Exception: " + e);
                    Toast.makeText(getApplicationContext(),
                            e.getMessage().toString(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startProgress(String title){
        progress.setTitle(title);
        progress.setMessage("Please Wait");
        progress.show();
    }
    private void finishProgress(){
        progress.dismiss();
    }
}
