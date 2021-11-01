package com.reader.office.mychanges.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.Gson
import com.reader.office.mychanges.slidernativead.AppsLinks
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SharedPref(appContext: Context?) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    private var DEFAULT_APP_IMAGEDATA_DIRECTORY: String? = null

    /**
     * Returns the String path of the last saved image
     * @return string path of the last saved image
     */
    var savedImagePath = ""
        private set

    /**
     * Decodes the Bitmap from 'path' and returns it
     * @param path image path
     * @return the Bitmap from 'path'
     */
    fun getImage(path: String?): Bitmap? {
        var bitmapFromPath: Bitmap? = null
        try {
            bitmapFromPath = BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
        }
        return bitmapFromPath
    }

    /**
     * Saves 'theBitmap' into folder 'theFolder' with the name 'theImageName'
     * @param theFolder the folder path dir you want to save it to e.g "DropBox/WorkImages"
     * @param theImageName the name you want to assign to the image file e.g "MeAtLunch.png"
     * @param theBitmap the image you want to save as a Bitmap
     * @return returns the full path(file system address) of the saved image
     */
    fun putImage(theFolder: String?, theImageName: String?, theBitmap: Bitmap?): String? {
        if (theFolder == null || theImageName == null || theBitmap == null) return null
        DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder
        val mFullPath = setupFullPath(theImageName)
        if (mFullPath != "") {
            savedImagePath = mFullPath
            saveBitmap(mFullPath, theBitmap)
        }
        return mFullPath
    }

    /**
     * Saves 'theBitmap' into 'fullPath'
     * @param fullPath full path of the image file e.g. "Images/MeAtLunch.png"
     * @param theBitmap the image you want to save as a Bitmap
     * @return true if image was saved, false otherwise
     */
    fun putImageWithFullPath(fullPath: String?, theBitmap: Bitmap?): Boolean {
        return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap)
    }

    /**
     * Creates the path for the image with name 'imageName' in DEFAULT_APP.. directory
     * @param imageName name of the image
     * @return the full path of the image. If it failed to create directory, return empty string
     */
    private fun setupFullPath(imageName: String): String {
        val mFolder =
            File(Environment.getExternalStorageDirectory(), DEFAULT_APP_IMAGEDATA_DIRECTORY)
        if (isExternalStorageReadable && isExternalStorageWritable && !mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("ERROR", "Failed to setup folder")
                return ""
            }
        }
        return mFolder.path + '/' + imageName
    }

    /**
     * Saves the Bitmap as a PNG file at path 'fullPath'
     * @param fullPath path of the image file
     * @param bitmap the image as a Bitmap
     * @return true if it successfully saved, false otherwise
     */
    private fun saveBitmap(fullPath: String?, bitmap: Bitmap?): Boolean {
        if (fullPath == null || bitmap == null) return false
        var fileCreated = false
        var bitmapCompressed = false
        var streamClosed = false
        val imageFile = File(fullPath)
        if (imageFile.exists()) if (!imageFile.delete()) return false
        try {
            fileCreated = imageFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(imageFile)
            bitmapCompressed = bitmap.compress(CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmapCompressed = false
        } finally {
            if (out != null) {
                try {
                    out.flush()
                    out.close()
                    streamClosed = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    streamClosed = false
                }
            }
        }
        return fileCreated && bitmapCompressed && streamClosed
    }
    // Getters
    /**
     * Get int value from SharedPreferences at 'key'. If key not found, return 0
     * @param key SharedPreferences key
     * @return int value at 'key' or 0 if key not found
     */
    fun getInt(key: String?): Int {
        return preferences.getInt(key, 0)
    }

    /**
     * Get parsed ArrayList of Integers from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of Integers
     */
    fun getListInt(key: String?): ArrayList<Int> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = ArrayList(Arrays.asList(*myList))
        val newList = ArrayList<Int>()
        for (item in arrayToList) newList.add(item.toInt())
        return newList
    }

    /**
     * Get long value from SharedPreferences at 'key'. If key not found, return 0
     * @param key SharedPreferences key
     * @return long value at 'key' or 0 if key not found
     */
    fun getLong(key: String?): Long {
        return preferences.getLong(key, 0)
    }

    /**
     * Get float value from SharedPreferences at 'key'. If key not found, return 0
     * @param key SharedPreferences key
     * @return float value at 'key' or 0 if key not found
     */
    fun getFloat(key: String?): Float {
        return preferences.getFloat(key, 0f)
    }

    /**
     * Get double value from SharedPreferences at 'key'. If exception thrown, return 0
     * @param key SharedPreferences key
     * @return double value at 'key' or 0 if exception is thrown
     */
    fun getDouble(key: String?): Double {
        val number = getString(key)
        return try {
            number!!.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }

    /**
     * Get parsed ArrayList of Double from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of Double
     */
    fun getListDouble(key: String?): ArrayList<Double> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = ArrayList(Arrays.asList(*myList))
        val newList = ArrayList<Double>()
        for (item in arrayToList) newList.add(item.toDouble())
        return newList
    }

    /**
     * Get parsed ArrayList of Integers from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of Longs
     */
    fun getListLong(key: String?): ArrayList<Long> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = ArrayList(Arrays.asList(*myList))
        val newList = ArrayList<Long>()
        for (item in arrayToList) newList.add(item.toLong())
        return newList
    }

    /**
     * Get String value from SharedPreferences at 'key'. If key not found, return ""
     * @param key SharedPreferences key
     * @return String value at 'key' or "" (empty String) if key not found
     */
    fun getString(key: String?): String? {
        return preferences.getString(key, "")
    }

    /**
     * Get parsed ArrayList of String from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of String
     */
    fun getListString(key: String?): ArrayList<String> {
        return ArrayList(Arrays.asList(*TextUtils.split(preferences.getString(key, ""), "‚‗‚")))
    }

    /**
     * Get boolean value from SharedPreferences at 'key'. If key not found, return false
     * @param key SharedPreferences key
     * @return boolean value at 'key' or false if key not found
     */
    fun getBoolean(key: String?): Boolean {
        return preferences.getBoolean(key, false)
    }

    /**
     * Get parsed ArrayList of Boolean from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of Boolean
     */
    fun getListBoolean(key: String?): ArrayList<Boolean> {
        val myList = getListString(key)
        val newList = ArrayList<Boolean>()
        for (item in myList) {
            newList.add(item == "true")
        }
        return newList
    }

    fun getListObject(key: String?, mClass: Class<*>?): ArrayList<Any> {
        val gson = Gson()
        val objStrings = getListString(key)
        val objects = ArrayList<Any>()
        for (jObjString in objStrings) {
            val value = gson.fromJson(jObjString, mClass)
            objects.add(value)
        }
        return objects
    }
    fun getRemoteList(key: String): ArrayList<AppsLinks> {
        val gson = Gson()
        val objStrings = getListString(key)
        val objects = ArrayList<AppsLinks>()
        for (jObjString in objStrings) {
            val value = gson.fromJson(jObjString, AppsLinks::class.java)
            objects.add(value)
        }
        return objects
    }

    fun <T> getObject(key: String?, classOfT: Class<T>?): T {
        val json = getString(key)
        val value: T = Gson().fromJson(json, classOfT) ?: throw NullPointerException()
        return value as T
    }

    // Put methods
    /**
     * Put int value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value int value to be added
     */
    fun putInt(key: String?, value: Int) {
        checkForNullKey(key)
        preferences.edit().putInt(key, value).apply()
    }

    /**
     * Put ArrayList of Integer into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param intList ArrayList of Integer to be added
     */
    fun putListInt(key: String?, intList: ArrayList<Int>) {
        checkForNullKey(key)
        val myIntList = intList.toTypedArray()
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myIntList)).apply()
    }

    /**
     * Put long value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value long value to be added
     */
    fun putLong(key: String?, value: Long) {
        checkForNullKey(key)
        preferences.edit().putLong(key, value).apply()
    }

    /**
     * Put ArrayList of Long into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param longList ArrayList of Long to be added
     */
    fun putListLong(key: String?, longList: ArrayList<Long>) {
        checkForNullKey(key)
        val myLongList = longList.toTypedArray()
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myLongList)).apply()
    }

    /**
     * Put float value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value float value to be added
     */
    fun putFloat(key: String?, value: Float) {
        checkForNullKey(key)
        preferences.edit().putFloat(key, value).apply()
    }

    /**
     * Put double value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value double value to be added
     */
    fun putDouble(key: String?, value: Double) {
        checkForNullKey(key)
        putString(key, value.toString())
    }

    /**
     * Put ArrayList of Double into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param doubleList ArrayList of Double to be added
     */
    fun putListDouble(key: String?, doubleList: ArrayList<Double>) {
        checkForNullKey(key)
        val myDoubleList = doubleList.toTypedArray()
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myDoubleList)).apply()
    }

    /**
     * Put String value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value String value to be added
     */
    fun putString(key: String?, value: String?) {
        checkForNullKey(key)
        checkForNullValue(value)
        preferences.edit().putString(key, value).apply()
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    fun putListString(key: String?, stringList: ArrayList<String>) {
        checkForNullKey(key)
        val myStringList = stringList.toTypedArray()
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply()
    }

    /**
     * Put boolean value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value boolean value to be added
     */
    fun putBoolean(key: String?, value: Boolean) {
        checkForNullKey(key)
        preferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Put ArrayList of Boolean into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param boolList ArrayList of Boolean to be added
     */
    fun putListBoolean(key: String?, boolList: ArrayList<Boolean>) {
        checkForNullKey(key)
        val newList = ArrayList<String>()
        for (item in boolList) {
            if (item) {
                newList.add("true")
            } else {
                newList.add("false")
            }
        }
        putListString(key, newList)
    }

    /**
     * Put ObJect any type into SharedPrefrences with 'key' and save
     * @param key SharedPreferences key
     * @param obj is the Object you want to put
     */

    fun putObject(key: String?, obj: Any?) {
        checkForNullKey(key)
        val gson = Gson()
        putString(key, gson.toJson(obj))
    }

    fun putListObject(key: String?, objArray: ArrayList<Any>) {
        checkForNullKey(key)
        val gson = Gson()
        val objStrings = ArrayList<String>()
        for (obj in objArray) {
            objStrings.add(gson.toJson(obj))
        }
        putListString(key, objStrings)
    }

    fun putListNotifications(key: String?, objArray: ArrayList<AppsLinks>) {
        checkForNullKey(key)
        val gson = Gson()
        val objStrings = ArrayList<String>()
        for (obj in objArray) {
            objStrings.add(gson.toJson(obj))
        }
        putListString(key, objStrings)
    }

    /**
     * Remove SharedPreferences item with 'key'
     * @param key SharedPreferences key
     */
    fun remove(key: String?) {
        preferences.edit().remove(key).apply()
    }

    /**
     * Delete image file at 'path'
     * @param path path of image file
     * @return true if it successfully deleted, false otherwise
     */
    fun deleteImage(path: String?): Boolean {
        return File(path).delete()
    }

    /**
     * Clear SharedPreferences (remove everything)
     */
    fun clear() {
        preferences.edit().clear().apply()
    }

    /**
     * Retrieve all values from SharedPreferences. Do not modify collection return by method
     * @return a Map representing a list of key/value pairs from SharedPreferences
     */
    val all: Map<String, *>
        get() = preferences.all

    /**
     * Register SharedPreferences change listener
     * @param listener listener object of OnSharedPreferenceChangeListener
     */
    fun registerOnSharedPreferenceChangeListener(
        listener: OnSharedPreferenceChangeListener?
    ) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregister SharedPreferences change listener
     * @param listener listener object of OnSharedPreferenceChangeListener to be unregistered
     */
    fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     * @param key the pref key to check
     */
    private fun checkForNullKey(key: String?) {
        if (key == null) {
            throw NullPointerException()
        }
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     * @param value the pref value to check
     */
    private fun checkForNullValue(value: String?) {
        if (value == null) {
            throw NullPointerException()
        }
    }

    companion object {
        /**
         * Check if external storage is writable or not
         * @return true if writable, false otherwise
         */
        val isExternalStorageWritable: Boolean
            get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
        /**
         * Check if external storage is readable or not
         * @return true if readable, false otherwise
         */
        val isExternalStorageReadable: Boolean
            get() {
                val state = Environment.getExternalStorageState()
                return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
            }
    }

}