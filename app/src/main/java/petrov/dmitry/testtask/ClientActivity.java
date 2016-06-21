package petrov.dmitry.testtask;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import petrov.dmitry.testtask.Utility.AppDataBase;

public class ClientActivity extends AppCompatActivity {


    private ListView listView;
    private SimpleCursorAdapter scAdapter;
    private long id;

    private SimpleDateFormat sdf;
    private DatePickerDialog datePickerDialog;

    private AutoCompleteTextView firstName;
    private AutoCompleteTextView lastName;
    private AutoCompleteTextView middleName;
    private AutoCompleteTextView phone;
    private EditText date;
    private Button saveButton;

    private boolean editable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        id = getIntent().getLongExtra(AppDataBase.COLUMN_ID, -1);
        Cursor cursor = AppDataBase.getInstance().getClient(id);
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
        firstName.setText(cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_FIRST_NAME)));
        lastName = (AutoCompleteTextView) findViewById(R.id.last_name);
        lastName.setText(cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_LAST_NAME)));
        middleName = (AutoCompleteTextView) findViewById(R.id.middle_name);
        middleName.setText(cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_MIDDLE_NAME)));
        phone = (AutoCompleteTextView) findViewById(R.id.phone);
        phone.setText(cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_PHONE_NUMBER)));
        date = (EditText) findViewById(R.id.date);
        date.setText(cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_DATE)));

        // Кнопка входа
        saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(editable) {
                    boolean errors = false;
                    if (firstName.getText().toString().isEmpty()) {
                        errors = true;
                        firstName.setError(getResources().getString(R.string.error_required_field));
                    }
                    if (lastName.getText().toString().isEmpty()) {
                        errors = true;
                        lastName.setError(getResources().getString(R.string.error_required_field));
                    }
                    if (phone.getText().toString().isEmpty()) {
                        errors = true;
                        phone.setError(getResources().getString(R.string.error_required_field));
                    }
                    if (date.getText().toString().isEmpty()) {
                        errors = true;
                        date.setError(getResources().getString(R.string.error_required_field));
                    }
                    if (errors) {
                        firstName.requestFocus();
                        return;
                    }

                    // TODO: Реализовать обновление записи
                }

                setEditable(!editable);

                scAdapter.swapCursor(AppDataBase.getInstance().getTransactions(id));
            }
        });

        listView = (ListView) findViewById(R.id.listView);

        // Формируем столбцы сопоставления
        String[] fromDB = new String[]{AppDataBase.COLUMN_DATE, AppDataBase.COLUMN_COST, AppDataBase.COLUMN_ID};
        int[] toView = new int[]{R.id.date, R.id.balance, R.id.transaction_item};

        scAdapter = new SimpleCursorAdapter(this, R.layout.transaction_item, null, fromDB, toView, 0);
        listView.setAdapter(scAdapter);
        SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, final Cursor cursor, final int columnIndex){
                switch (view.getId()) {
                    case R.id.transaction_item:
                        view.findViewById(R.id.button_del).setVisibility(editable? View.VISIBLE : View.INVISIBLE);
                        return true;
                    case R.id.balance:
                        long cost = cursor.getLong(columnIndex);
                        if(cost > 0) {
                            ((TextView) view).setText(String.format("+%s", String.valueOf(cost)));
                        } else {
                            ((TextView) view).setText(String.valueOf(cost));
                        }
                        return true;
                    default:
                        return false;
                }
            }
        };
        scAdapter.setViewBinder(viewBinder);

        // TODO: Переделать на курсор лоадер
        scAdapter.swapCursor(AppDataBase.getInstance().getTransactions(id));
        setListViewHeightBasedOnChildren(listView);
    }

    // Увеличиываем размер listView в зависимости от кол-ва элементов, дабы не было конфликтов с Scroll'ом
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, Toolbar.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void setEditable(boolean editable) {
        this.editable = editable;
        firstName.setEnabled(editable);
        lastName.setEnabled(editable);
        middleName.setEnabled(editable);
        phone.setEnabled(editable);
        date.setEnabled(editable);

        if(editable) {
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
            saveButton.setText(getResources().getString(R.string.button_save));
        } else {
            date.setOnFocusChangeListener(null);
            date.setOnClickListener(null);
            saveButton.setText(getResources().getString(R.string.button_edit));
        }
    }
}
