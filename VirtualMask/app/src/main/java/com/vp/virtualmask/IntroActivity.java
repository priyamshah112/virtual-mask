package com.vp.virtualmask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class IntroActivity extends AppCompatActivity {
    Button button;
    Integer genderFlag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_intro);

        button = (Button) findViewById(R.id.gender_selected);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });

        CardView card_view_men = (CardView) findViewById(R.id.men_card); // creating a CardView and assigning a value.
        card_view_men.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderFlag=1;
            }
        });

        CardView card_view_women = (CardView) findViewById(R.id.women_card); // creating a CardView and assigning a value.
        card_view_women.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderFlag=2;
            }
        });
    }
    public void openNewActivity(){
        if(genderFlag==0){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "PLease Select any one of the gender photo",
                    Toast.LENGTH_SHORT);

            toast.show();
        }
        else {
            Intent intent = new Intent(this, FullscreenActivity.class);
            startActivity(intent);
            finish();
        }
    }
}