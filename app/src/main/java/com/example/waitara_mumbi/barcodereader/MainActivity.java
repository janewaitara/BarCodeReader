package com.example.waitara_mumbi.barcodereader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.Result;

import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.Manifest.permission.CAMERA;

//implements the Interface ZXingScannerView.ResultHandler to be able to act as QR Code Scanner as well as Bar Code scanner.
public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    //Shared Preferences
    SharedPreferences sharedPrefs;
    //naming your shared preference files using a name that's uniquely identifiable to your app
    public static final String MyPREFERENCES = "com.example.waitara_mumbi.barcodereader.PREFERENCE_FILE_KEY";
    
    //Barcode
    private static final int REQUEST_CAMERA = 1; //The constant REQUEST_CAMERA is used while getting the permissions from the user to use the camera.
    private ZXingScannerView scannerView; //ZXingScannerView provides the view to scan the QR code and Bar code




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

       /* We need to detect whether the app has the required permissions or not otherwise we need to request those from the user befor accessing the camera.*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//checking the api version since from android 6,we have to get the user permissions
            if (checkPermission()) {
                Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
    }

    /*For the first time, when the user installs the app, the app will request permission to use the Camera,
    on subsequent app runs we don’t need to provide any permission.*/

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    private boolean checkPermission (){
        return (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA)== PackageManager.PERMISSION_GRANTED);

    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission ,@NonNull int[] grantResults){

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                displayAlertMessage("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }


    }

    private void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK",listener)
                .setNegativeButton("CANCEL", null)
                .create()
                .show();
    }


/*we check if the ScannerView is null or not, in case it is null we create a new one
and then start the Camera to capture the QR Code using the startCamera() method*/
    @Override
    public void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkPermission()){
                if(scannerView==null){
                    scannerView =new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }else
            {
                requestPermission();
            }
        }
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();//release the Camera
    }

    /*This method contains the Logic to handle
    the Result of the scan from the android QR Scanner or the Bar Code scanner*/
    @Override
    public void handleResult(Result rawResult) {
        final String result = rawResult.getText();
        getUrlAccessToken(result);


        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
                startActivity(browserIntent);
            }
        });
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();*/
    }

    public void getUrlAccessToken(final String ticket_no) {
        sharedPrefs = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("clientId", "2");
        editor.putString("clientSecret", "nGUu9OSbwU8FhEJtWmT2CNMyZVeaKdD6jUHasKDO");
        editor.putString("grantType", "password");
        editor.putString("username", "charles@deveint.com");
        editor.putString("password", "deveint#");
        editor.apply();

        //create retrofit instance
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://178.79.155.54/")
                .addConverterFactory(GsonConverterFactory.create());//a converter(gson) to convert json(data format) to java objects

        Retrofit retrofit = builder.build();



        //getting client and call object for the request
        TicketClient client = retrofit.create(TicketClient.class);//instance of our TicketClient using retrofit.create and passing the TicketClient class[interface]
        //calling an actual method on our client
        Call<Authentication> AuthenticationCall = client.getAccesstoken(
                sharedPrefs.getString("clientId", null),
                sharedPrefs.getString("clientSecret", null),
                sharedPrefs.getString("grantType", null),
                sharedPrefs.getString("username", null),
                sharedPrefs.getString("password", null));

        //execute network request
        AuthenticationCall.enqueue(new Callback<Authentication>() {
            @Override
            public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                verifyTicket(response.body().getAccessToken(),ticket_no);

            }

            @Override
            public void onFailure(Call<Authentication> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Big Error"+t, Toast.LENGTH_LONG).show();

            }
        });
    }

    public void verifyTicket(final String accessToken, final String ticket_no){

        //OkHttpClient instance
         OkHttpClient.Builder okhttpBuilder = new  OkHttpClient.Builder();

         okhttpBuilder.addInterceptor(new Interceptor() {
             @Override
             public okhttp3.Response intercept(Chain chain) throws IOException {
                 Request request = chain.request();
                 Request.Builder newRequest = request.newBuilder().header("Authorization", "Bearer "+ accessToken);
                 ///Log.d(accessToken, "this access token");
                 return chain.proceed(newRequest.build());
             }
         });


        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofitTicket = new Retrofit.Builder()
                .client(okhttpBuilder.build())
                .baseUrl("http://178.79.155.54/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TicketClient client_t = retrofitTicket.create(TicketClient.class);
        Call<CheckTicket> CheckTicketCall = client_t.checkTicket(ticket_no);

        CheckTicketCall.enqueue(new Callback<CheckTicket>() {
            @Override
            public void onResponse(Call<CheckTicket> call, Response<CheckTicket> response) {
                Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_LONG).show();
                Log.d(response.toString() , "This string");
                if (response.isSuccessful()) {
                    if (response.body().getStatusCode().equals("200")){
                        showAlert("This ticket exists", ticket_no, accessToken, ticket_no);
                    }
                    if (response.body().getStatusCode().equals("404")){
                        showAlert("That ticket does not exist", ticket_no, accessToken, ticket_no);
                    }
                }/*else {
                    markTicket(accessToken, ticket_no);

                }*/
            }

            @Override
            public void onFailure(Call<CheckTicket> call, Throwable t) {
                Toast.makeText(MainActivity.this, "ERROR VERIFYING TICKET", Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void markTicket(final String accessToken, final String ticket_no){

        OkHttpClient.Builder okhttpBuilder = new OkHttpClient.Builder();

        okhttpBuilder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                Request request = chain.request();

                Request.Builder newRequest = request.newBuilder().header("Authorization", "Bearer " + accessToken);

                return chain.proceed(newRequest.build());
            }
        });


        Gson gson= new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofitMark = new  Retrofit.Builder()
                .client(okhttpBuilder.build())
                .baseUrl("http://178.79.155.54/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TicketClient client_m = retrofitMark.create(TicketClient.class);
        Call<MarkTicket> markTicketCall = client_m.markTicket(ticket_no);

        markTicketCall.enqueue(new Callback<MarkTicket>() {
            @Override
            public void onResponse(Call<MarkTicket> call, Response<MarkTicket> response) {
                //showAlert(null, "MARK TICKET");
                Toast.makeText(MainActivity.this, "Mark", Toast.LENGTH_SHORT).show();
                if (response.isSuccessful()) {
                    if (response.body().getStatusCode().equals("200")){
                        showAlertDialog("This ticket has been Marked successfully", ticket_no);
                    }
                    if (response.body().getStatusCode().equals("403")){
                        showAlertDialog("That ticket has already been used", ticket_no );
                    }
                }
                Toast.makeText(MainActivity.this, "Mark", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<MarkTicket> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error ", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAlert(String msg, String title,final String accessToken,final String ticket_no) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("MARK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                markTicket(accessToken, ticket_no);
                //scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void showAlertDialog(String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                scannerView.resumeCameraPreview(MainActivity.this);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
