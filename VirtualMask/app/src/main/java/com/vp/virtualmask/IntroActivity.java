package com.vp.virtualmask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IntroActivity extends AppCompatActivity {
    Button button;
    Integer genderFlag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_intro);

        button = (Button) findViewById(R.id.gender_selected);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewActivity();
            }
        });
        final TextView tv = (TextView)findViewById(R.id.textView);
        CardView card_view_men = (CardView) findViewById(R.id.men_card); // creating a CardView and assigning a value.
        card_view_men.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderFlag=1;
                tv.setText("Welcome Sir");//Gentleman
            }
        });

        CardView card_view_women = (CardView) findViewById(R.id.women_card); // creating a CardView and assigning a value.
        card_view_women.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                genderFlag=2;
                tv.setText("Welcome Ma'am");//lady
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
            Intent intent = new Intent(this, VirtualMask.class);
            startActivity(intent);
            finish();
        }
    }
}