package com.jackq.funfurniture.API;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.jackq.funfurniture.model.FurnitureModel;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.vuforia.WordList;


import java.io.File;

/**
 * Created by jackq on 12/30/16.
 */

public class APIModelLoader implements FutureCallback<File>{
    public interface LoaderCallback{
        void handlerError(Exception e);
        void finish();
    }

    private static final String TAG = "API_MODEL_LOADER";
    private static final String DIRECTORY = "ModelCache";
    private FurnitureModel model;
    private Context context;
    private LoaderCallback callback;

    public APIModelLoader(Context ctx, FurnitureModel model, LoaderCallback callback){
        this.context = ctx;
        this.model = model;
        this.callback = callback;
    }

    private int finishCount = 0;

    @Override
    public void onCompleted(Exception e, File result) {
        if(e != null){
            // Cancel all other items
            Ion.getDefault(context).cancelAll(this);
            callback.handlerError(e);
        }
        Log.e(TAG, "onCompleted: " + result.getAbsolutePath());
        synchronized (this){
            finishCount++;
            if(finishCount == 5)
                callback.finish();
        }
    }

    public void load(){
        File resourceDir = getModelDir();
        Future<File> mtl = getFile(model.getMtl(), new File(resourceDir, model.getFileNameMtl()));
        Future<File> obj= getFile(model.getObj(), new File(resourceDir, model.getFileNameObj()));
        Future<File> shadow = getFile(model.getShadow(), new File(resourceDir, model.getFileNameShadow()));
        Future<File> texture = getFile(model.getTexture(), new File(resourceDir, model.getFileNameTexture()));
        Future<File> thumb = getFile(model.getThumb(), new File(resourceDir, model.getFileNameThumb()));
    }

    private Future<File> getFile(String url, File file){
        return Ion.with(context)
                .load(url)
                .group(this)
                // .progress(this)
                .write(file)
                .setCallback(this);
    }

    private File getModelDir(){
        File dir = new File(context.getFilesDir(), DIRECTORY);
        if(!dir.exists()){
            if(!dir.mkdir()){
                Log.e(TAG, "getModelDir: Failed to create new directory");
                // If model directory fails, use temp dir as alternation
                return context.getCacheDir();
            }
        }
        return dir;
    }



}
