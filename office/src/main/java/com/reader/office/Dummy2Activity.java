package com.reader.office;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.reader.office.constant.MainConstant;
import com.reader.office.officereader.AppActivity;
import com.reader.office.R;

import java.io.File;

public class Dummy2Activity extends Activity {

    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_dummy_activity);

        textView = findViewById(R.id.onClick);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent();
//                intent.setClass(Dummy2Activity.this, AppActivity.class);
//                intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, "storage/emulated/0/Documents/6509-1.docx");
//                startActivityForResult(intent, RESULT_FIRST_USER);



//                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//                chooseFile.setType("*/*");
//                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
//                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword","application/vnd.openxmlformats-officedocument.presentationml.presentation","application/vnd.ms-powerpoint","application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/vnd.ms-excel"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, 200);


//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                String[] mimetypes = {"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword"};
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
//                startActivityForResult(
//                        Intent.createChooser(chooseFile, "Choose a file"),
//                        200
//                );


            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 200) {
                Uri uri = data.getData();
                String src = uri.getPath();

                String fileName = ExtenFuncKt.getFileNameByUri(this, uri);

                ExtenFuncKt.copyUriToExternalFilesDir(this, uri, fileName);


                String path = getExternalCacheDir().toString() + "/" + fileName;

                File file = new File(path);

                Toast.makeText(this, path + "", Toast.LENGTH_SHORT).show();

                Log.d("#ERROR", path);
                Intent intent = new Intent();
                intent.setClass(Dummy2Activity.this, AppActivity.class);
                intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, path);
                intent.putExtra("FileName", fileName);
                startActivityForResult(intent, RESULT_FIRST_USER);

            }


        }


    }
}
