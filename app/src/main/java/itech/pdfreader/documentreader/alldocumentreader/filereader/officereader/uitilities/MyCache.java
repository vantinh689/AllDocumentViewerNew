package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.uitilities;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

public class MyCache {

    private static MyCache instance;
    private LruCache<Object, Object> lru;

    private MyCache() {

        lru = new LruCache<Object, Object>(1024);

    }

    public static MyCache getInstance() {

        if (instance == null) {
            instance = new MyCache();
        }
        return instance;

    }

    public LruCache<Object, Object> getLru() {
        return lru;
    }

    public void saveBitmapToCahche(String key, Bitmap bitmap){
        try {
            MyCache.getInstance().getLru().put(key, bitmap);
        }catch (Exception ignored){}
    }

    public Bitmap retrieveBitmapFromCache(String key){
        try {
            Bitmap bitmap = (Bitmap) MyCache.getInstance().getLru().get(key);
            return bitmap;
        }catch (Exception e){}
        return null;
    }

}