package petrov.dmitry.testtask;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        Button buttonAddClient = (Button) findViewById(R.id.button_add_client);
        buttonAddClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Вызов добавления нового клиента

                // Для тестирования Базы данных и отображения
                Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                // Получаем текущую дату
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                // Добавляем запись в БД
                AppDataBase.getInstance().addClient("Дмитрий", "Попович", "Попов", sdf.format(new Date()), stream.toByteArray());
                // Обновляем список
                getLoaderManager().getLoader(0).forceLoad();
            }
        });

        listView = (ListView) findViewById(R.id.listView);

        // Формируем столбцы сопоставления
        String[] fromDB = new String[]{AppDataBase.COLUMN_IMAGE, AppDataBase.COLUMN_FIRST_NAME};
        int[] toView = new int[]{R.id.photo, R.id.first_name};

        scAdapter = new SimpleCursorAdapter(this, R.layout.client_item, null, fromDB, toView, 0);
        listView.setAdapter(scAdapter);
        SimpleCursorAdapter.ViewBinder viewBinder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex){
                switch (view.getId()) {
                    case R.id.photo:
                        byte[] data = cursor.getBlob(columnIndex);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

                        BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                        Paint paint = new Paint();
                        paint.setShader(shader);

                        Canvas c = new Canvas(circleBitmap);
                        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);

                        ((ImageView) view).setImageBitmap(circleBitmap);
                        return true;
                    default:
                        return false;
                }
            }
        };
        scAdapter.setViewBinder(viewBinder);

        // Создаем лоадер для чтения данных
        getLoaderManager().initLoader(0, null, this);

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        scAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return AppDataBase.getInstance().getClients(constraint.toString());
            }
        });
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

    // Статический класс курсор лоадера для списка оповещений
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
}
