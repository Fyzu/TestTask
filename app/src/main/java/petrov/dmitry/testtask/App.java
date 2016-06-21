package petrov.dmitry.testtask;

import android.app.Application;
import android.content.Context;

import petrov.dmitry.testtask.Utility.AppDataBase;

public class App extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
        AppDataBase.getInstance();
    }

    public static Context getAppContext() {
        return App.context;
    }
}
