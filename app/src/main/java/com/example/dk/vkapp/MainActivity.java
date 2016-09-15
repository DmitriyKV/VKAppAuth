package com.example.dk.vkapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    public String[] scope = new String[]{VKScope.PHOTOS};
    String fileName = "fileVKAuth";
    String fileName_img = "VKauthimg";
    TextView txtUserFIO;
    ImageView imageView;
    String fio = "";
    String photoURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fileName = Environment.getExternalStorageDirectory() + "/" + fileName;
        fileName_img = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName_img;

        txtUserFIO = (TextView)findViewById(R.id.txtUserFIO);
        imageView = (ImageView) findViewById(R.id.imgUser);

        readFile();
        if (fio == "") {
            VKSdk.login(this, scope);
        } else {
            setImg();
        }
        setFIO();
    }

    void writeFile() {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE)));
            bw.write(fio);
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(fileName)));
            String str = "";
            if ((str = br.readLine()) != null) {
                fio = str;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setFIO() {
        txtUserFIO.setText(fio);
    }

    void setImg() {
        Bitmap bitmap = BitmapFactory.decodeFile(fileName_img);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,"photo_50"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);

                        try {
                            JSONObject userObj = response.json.getJSONArray("response").getJSONObject(0);
                            fio = userObj.getString("last_name") + " " + userObj.getString("first_name");
                            photoURL = userObj.getString("photo_50");
                        }
                        catch (JSONException e) {
                            Log.e("errorJSON", "Error parsing JSON!");
                        }
                        writeFile();

                        ImageManager IM=new ImageManager();
                        IM.fetchImage(photoURL, fileName_img, imageView);
                    }
                });
            }
            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
