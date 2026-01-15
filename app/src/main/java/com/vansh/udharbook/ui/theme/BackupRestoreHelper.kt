package com.vansh.udharbook.ui.theme

import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupRestoreHelper {

    // The name of your Room Database
    private const val DB_NAME = "udharbook_database"

    // 1. BACKUP: Copy App DB -> User Selected File
    fun backupData(context: Context, destUri: Uri) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")
            val walFile = File(dbFile.parent, "$DB_NAME-wal")

            // Close DB connections before copy to be safe (checkpoint)
            // Ideally we trigger a checkpoint, but for simple copy:

            context.contentResolver.openOutputStream(destUri)?.use { output ->
                FileInputStream(dbFile).use { input ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context, "Backup Successful!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. RESTORE: Copy User Selected File -> App DB
    fun restoreData(context: Context, sourceUri: Uri) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)

            // Delete existing temp files to prevent corruption
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            if (shmFile.exists()) shmFile.delete()
            if (walFile.exists()) walFile.delete()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(context, "Restore Success! Restarting...", Toast.LENGTH_LONG).show()

            // Restart App to reload fresh data
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
            System.exit(0)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}