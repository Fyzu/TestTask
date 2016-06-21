package petrov.dmitry.testtask;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.math.BigInteger;

import petrov.dmitry.testtask.Utility.AppDataBase;

public class MainActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private ListView listView;
    private SimpleCursorAdapter scAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Добавляем обработчик клика на кнопку добавить клиент
        Button buttonAddClient = (Button) findViewById(R.id.button_add_client);
        buttonAddClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), AddClientActivity.class), 1);
            }
        });

        listView = (ListView) findViewById(R.id.listView);

        // Формируем столбцы сопоставления
        String[] fromDB = new String[]{AppDataBase.COLUMN_IMAGE, AppDataBase.COLUMN_FIRST_NAME, AppDataBase.COLUMN_ID};
        int[] toView = new int[]{R.id.photo, R.id.first_name, R.id.client_item};
        // Устанавливаем адаптер для нашего списка
        scAdapter = new SimpleCursorAdapter(this, R.layout.client_item, null, fromDB, toView, 0);
        listView.setAdapter(scAdapter);
        // Создаем свой viewBinder
        SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, final Cursor cursor, final int columnIndex){
                switch (view.getId()) {
                    case R.id.client_item:
                        long id = cursor.getLong(cursor.getColumnIndex(AppDataBase.COLUMN_ID));
                        // Устанавливаем слушатели на клики
                        ItemClick itemClick = new ItemClick(id);
                        view.findViewById(R.id.first_name).setOnClickListener(itemClick);
                        view.findViewById(R.id.photo).setOnClickListener(itemClick);
                        view.findViewById(R.id.button_inc).setOnClickListener(new IncButtonClick(id,
                                cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_FIRST_NAME)),
                                cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_FIRST_NAME)),
                                cursor.getBlob(cursor.getColumnIndex(AppDataBase.COLUMN_IMAGE)))
                        );
                        view.findViewById(R.id.button_dec).setOnClickListener(new DecButtonClick(
                                id, cursor.getString(cursor.getColumnIndex(AppDataBase.COLUMN_FIRST_NAME)))
                        );

                        return true;
                    case R.id.photo:
                        // Получаем фотографию
                        byte[] data = cursor.getBlob(columnIndex);
                        // Отрисовываю круг вокруг фотографии, если фотография есть
                        if(data.length > 0) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                            Paint paint = new Paint();
                            paint.setShader(shader);
                            Canvas c = new Canvas(circleBitmap);
                            c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);

                            ((ImageView) view).setImageBitmap(circleBitmap);
                        } else {
                            Drawable drawable = getResources().getDrawable(R.drawable.photo_empty);
                            ((ImageView) view).setImageBitmap(((BitmapDrawable)drawable).getBitmap());
                        }
                        return true;
                    default:
                        return false;
                }
            }
        };
        scAdapter.setViewBinder(viewBinder);

        // Создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

        // Добавляем слушатели для вьюхи поиска
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        // Устанавливаем фильтр для адаптера
        scAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return AppDataBase.getInstance().getClients(constraint.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Получаем данные из активности добавления клиента
        if (intent != null) {
            AppDataBase.getInstance().addClient(
                    intent.getStringExtra(AppDataBase.COLUMN_FIRST_NAME),
                    intent.getStringExtra(AppDataBase.COLUMN_LAST_NAME),
                    intent.getStringExtra(AppDataBase.COLUMN_MIDDLE_NAME),
                    intent.getStringExtra(AppDataBase.COLUMN_PHONE_NUMBER),
                    intent.getStringExtra(AppDataBase.COLUMN_DATE),
                    intent.getByteArrayExtra(AppDataBase.COLUMN_IMAGE)
            );
        }

        // Обновляем список после изменений
        getLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String text) {
        // Запрашиваем фильтр записей у адаптера
        scAdapter.getFilter().filter(text);
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new ClientsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        scAdapter.swapCursor(null);
    }

    // Статический класс курсор лоадера для списка клиентов
    private static class ClientsLoader extends CursorLoader {

        Context context;

        public ClientsLoader(Context context) {
            super(context);

            this.context = context;
        }

        @Override
        public Cursor loadInBackground() {
            return AppDataBase.getInstance().getClients();
        }
    }

    // Модифицированный callback нажатия, для хранения уникальных данных
    private class IncButtonClick implements View.OnClickListener {

        private long id;
        private String firstName;
        private String lastName;
        private byte[] img;

        public IncButtonClick(long id, String firstName, String lastName, byte[] img) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.img = img;
        }

        @Override
        public void onClick(View view) {
            // Вызываю модальное окно с транзакциями
            new TransactionDialog(id, String.format("%s %s", firstName, lastName), img).show(getFragmentManager(), "TransactionDialog");
        }
    }

    // Модифицированный callback нажатия, для хранения уникальных данных
    private class DecButtonClick implements View.OnClickListener {

        private long id;
        private String name;

        public DecButtonClick(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public void onClick(View view) {
            Cursor cursor = AppDataBase.getInstance().getTransactions(id);

            // Получаем баланс
            BigInteger sum = BigInteger.ZERO;
            if (cursor.moveToFirst())
                do {
                    sum = sum.add(BigInteger.valueOf(cursor.getLong(cursor.getColumnIndex(AppDataBase.COLUMN_COST))));
                } while (cursor.moveToNext());

            // Проверка, возможно ли снятие средств у клиента
            int compare = BigInteger.ZERO.compareTo(sum);
            if(compare == 0 || compare == 1) {
                Toast.makeText(getApplication(), String.format(getResources().getString(R.string.transaction_subtract_cost_error), name), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplication(), String.format(getResources().getString(R.string.transaction_subtract_cost), name), Toast.LENGTH_SHORT).show();
                AppDataBase.getInstance().addTransaction(id, -1000);
            }
        }
    }

    private class ItemClick implements View.OnClickListener {

        private long id;

        public ItemClick(long id) {
            this.id = id;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), ClientActivity.class);
            intent.putExtra(AppDataBase.COLUMN_ID, id);
            startActivityForResult(intent, 2);
        }
    }
}
