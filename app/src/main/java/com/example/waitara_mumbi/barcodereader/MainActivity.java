package com.example.waitara_mumbi.barcodereader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ActivityChooserView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

//implements the Interface ZXingScannerView.ResultHandler to be able to act as QR Code Scanner as well as Bar Code scanner.
public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

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
    public void onRequestPermissionsResult(int requestCode,String permission[] ,int grantResults[]){

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


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        alert1.show();
    }
}
