package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Recording
import com.example.data.model.Script
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Script::class, Recording::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teleprompt_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialScripts(database.scriptDao())
                    }
                }
            }

            suspend fun populateInitialScripts(scriptDao: ScriptDao) {
                val sampleScripts = listOf(
                    Script(
                        title = "🔥 30-Second Viral Hook (Reels/Shorts)",
                        content = """Stop scrolling if you want to double your video watch time! 
Here is the #1 mistake most creators make in their first 3 seconds:
They start with 'Hey guys, welcome back'.
Instead, drop your audience right into the conflict or question.
Show the end result, tease a counter-intuitive secret, and keep your gaze directly into the lens.
Try this structure on your next upload and let me know your retention in the comments!""",
                        scrollSpeed = 38f,
                        overlayOpacity = 0.65f,
                        fontSizeSp = 22,
                        textAlign = "CENTER"
                    ),
                    Script(
                        title = "💡 60-Sec Tech Explainer",
                        content = """Ever wondered why teleprompters make you look so confident on camera?
When you memorize a long script, your eyes constantly glance away trying to recall phrases.
With this real-time overlay directly over the front lens, your eye-line stays locked on the viewer.
You can adjust the scrolling speed to match your natural breathing cadence.
And dial the transparency down so you can still check your lighting and framing.
It feels like magic and saves you 10 re-takes!""",
                        scrollSpeed = 32f,
                        overlayOpacity = 0.55f,
                        fontSizeSp = 24,
                        textAlign = "CENTER"
                    ),
                    Script(
                        title = "🚀 High-Energy Product Pitch",
                        content = """Imagine producing professional studio-grade short videos without expensive teleprompter rigs.
Meet TelePrompt: your pocket production crew.
Auto-scrolling scripts right on your front camera.
Real-time speed regulation with one tap.
Crystal-clear HD recording with zero stutter.
Start filming like a broadcast pro today!""",
                        scrollSpeed = 42f,
                        overlayOpacity = 0.70f,
                        fontSizeSp = 22,
                        textAlign = "CENTER"
                    )
                )
                scriptDao.insertScripts(sampleScripts)
            }
        }
    }
}
