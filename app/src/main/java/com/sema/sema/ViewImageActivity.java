package com.sema.sema;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;

public class ViewImageActivity extends AppCompatActivity {

    private String mPostKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
