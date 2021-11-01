package itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.db

import android.app.Application
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import itech.pdfreader.documentreader.alldocumentreader.filereader.officereader.models.DataModel

@Keep
@Database(entities = [DataModel::class], version = 4)
abstract class DocumentsDatabase : RoomDatabase(){
    abstract fun docModelDao(): DocumentDao
    companion object{
        private val lock = Any()
        private const val DB_NAME = "Document.db"
        private var INSTANCE : DocumentsDatabase? = null
        fun getInstance(application: Application): DocumentsDatabase {
            synchronized(lock){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(application, DocumentsDatabase::class.java, DB_NAME)
                        .allowMainThreadQueries()
//                        .addMigrations(object : Migration(2, 4) {
//                            override fun migrate(database: SupportSQLiteDatabase) {
//                                database.execSQL("ALTER TABLE DocModel ADD COLUMN 'encryptedTime' INTEGER NOT NULL DEFAULT 0")
//                                database.execSQL("ALTER TABLE DocModel ADD COLUMN 'encryptedLength' INTEGER NOT NULL DEFAULT 0")
//                            }
//                        })
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}