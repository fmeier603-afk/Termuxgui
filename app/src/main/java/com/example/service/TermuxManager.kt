package com.example.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.TermuxResultReceiver

object TermuxManager {

    const val TERMUX_PACKAGE_NAME = "com.termux"
    private const val TERMUX_SERVICE_NAME = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"

    // Termux Intent Extras
    private const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
    private const val EXTRA_RUN_COMMAND_RESULT_INTENT = "com.termux.RUN_COMMAND_RESULT_INTENT"

    const val DEFAULT_BASH_PATH = "/data/data/com.termux/files/usr/bin/bash"
    const val DEFAULT_WORK_DIR = "/data/data/com.termux/files/home"

    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    TERMUX_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(TERMUX_PACKAGE_NAME, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun dispatchCommand(
        context: Context,
        commandId: String,
        scriptOrExecutable: String = DEFAULT_BASH_PATH,
        arguments: Array<String>,
        workingDir: String = DEFAULT_WORK_DIR,
        runInBackground: Boolean = true
    ): Result<Unit> {
        return try {
            val intent = Intent().apply {
                setClassName(TERMUX_PACKAGE_NAME, TERMUX_SERVICE_NAME)
                action = ACTION_RUN_COMMAND
                putExtra(EXTRA_RUN_COMMAND_PATH, scriptOrExecutable)
                putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arguments)
                putExtra(EXTRA_RUN_COMMAND_WORKDIR, workingDir)
                putExtra(EXTRA_RUN_COMMAND_BACKGROUND, runInBackground)
                putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, "0")

                // Callback PendingIntent for receiving stdout & stderr
                val resultIntent = Intent(context, TermuxResultReceiver::class.java).apply {
                    action = TermuxResultReceiver.ACTION_RESULT
                    putExtra(TermuxResultReceiver.EXTRA_COMMAND_ID, commandId)
                }
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    commandId.hashCode(),
                    resultIntent,
                    flags
                )
                putExtra(EXTRA_RUN_COMMAND_RESULT_INTENT, pendingIntent)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
