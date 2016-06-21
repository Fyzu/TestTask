package petrov.dmitry.testtask;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import petrov.dmitry.testtask.Utility.AppDataBase;

public class AddClientActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;

    SimpleDateFormat sdf;
    DatePickerDialog datePickerDialog;

    AutoCompleteTextView firstName;
    AutoCompleteTextView lastName;
    AutoCompleteTextView middleName;
    AutoCompleteTextView phone;
    EditText date;
    ImageView viewImage;

    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sdf = new SimpleDateFormat("yyyy.MM.dd");
        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                date.setText(sdf.format(calendar.getTime()));
            }

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        firstName = (AutoCompleteTextView) findViewById(R.id.first_name);
        lastName = (AutoCompleteTextView) findViewById(R.id.last_name);
        middleName = (AutoCompleteTextView) findViewById(R.id.middle_name);
        phone = (AutoCompleteTextView) findViewById(R.id.phone);
        date = (EditText) findViewById(R.id.date);
        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) datePickerDialog.show();
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        viewImage = (ImageView) findViewById(R.id.photo);
        viewImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        // Кнопка входа
        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                boolean errors = false;
                if(firstName.getText().toString().isEmpty()) {
                    errors = true;
                    firstName.setError(getResources().getString(R.string.error_required_field));
                }
                if(lastName.getText().toString().isEmpty()) {
                    errors = true;
                    lastName.setError(getResources().getString(R.string.error_required_field));
                }
                if(phone.getText().toString().isEmpty()) {
                    errors = true;
                    phone.setError(getResources().getString(R.string.error_required_field));
                }
                if(date.getText().toString().isEmpty()) {
                    errors = true;
                    date.setError(getResources().getString(R.string.error_required_field));
                }
                if(errors) {
                    firstName.requestFocus();
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra(AppDataBase.COLUMN_FIRST_NAME, firstName.getText().toString());
                intent.putExtra(AppDataBase.COLUMN_LAST_NAME, lastName.getText().toString());
                intent.putExtra(AppDataBase.COLUMN_MIDDLE_NAME, middleName.getText().toString());
                intent.putExtra(AppDataBase.COLUMN_PHONE_NUMBER, phone.getText().toString());
                intent.putExtra(AppDataBase.COLUMN_DATE, date.getText().toString());
                if(photo != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    intent.putExtra(AppDataBase.COLUMN_IMAGE, stream.toByteArray());
                } else {
                    intent.putExtra(AppDataBase.COLUMN_IMAGE, new byte[]{});
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            // Отрисовываю круг вокруг фотографии
            Bitmap circleBitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader (photo,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(photo.getWidth()/2, photo.getHeight()/2, photo.getWidth()/2, paint);
            viewImage.setImageBitmap(circleBitmap);
        }
    }
}

