package petrov.dmitry.testtask;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import petrov.dmitry.testtask.Utility.AppDataBase;

public class TransactionDialog extends DialogFragment {

    private long id;
    private String fullName;
    private byte[] img;

    private View v;


    public TransactionDialog setSettings(long id, String fullName, byte[] img) {
        this.id = id;
        this.fullName = fullName;
        this.img = img;
        return this;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instance) {
        getDialog().setTitle(getResources().getString(R.string.transaction_add_cost));
        v = inflater.inflate(R.layout.dialog_transaction, null);
        ((TextView) v.findViewById(R.id.full_name)).setText(fullName);

        if(img.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas c = new Canvas(circleBitmap);
            c.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
            ((ImageView) v.findViewById(R.id.photo)).setImageBitmap(circleBitmap);
        }

        ((Button) v.findViewById(R.id.button_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText cost = (EditText)v.findViewById(R.id.cost);
                if(cost.getText().toString().isEmpty()) {
                    cost.setError(getResources().getString(R.string.error_required_field));
                    cost.requestFocus();
                    return;
                }

                AppDataBase.getInstance().addTransaction(id, Long.valueOf(cost.getText().toString()));
                dismiss();
            }
        });

        // Сохраняем фрагмент, что бы он не удалился вместе с активностью
        setRetainInstance(true);

        return v;
    }

    @Override
    public void onDestroyView() {
        // Избавляемся от краша при повороте
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
