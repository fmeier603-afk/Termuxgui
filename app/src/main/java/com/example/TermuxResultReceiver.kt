package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class TermuxExecutionResult(
    val commandId: String?,
    val stdout: String?,
    val stderr: String?,
    val exitCode: Int,
    val errorCode: Int,
    val errorMessage: String?
)

class TermuxResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val stdout = intent.getStringExtra("stdout")
        val stderr = intent.getStringExtra("stderr")
        val exitCode = intent.getIntExtra("exitCode", 0)
        val errorCode = intent.getIntExtra("errCode", 0)
        val errorMessage = intent.getStringExtra("errmsg")
        val commandId = intent.getStringExtra("EXTRA_COMMAND_ID")

        val result = TermuxExecutionResult(
            commandId = commandId,
            stdout = stdout,
            stderr = stderr,
            exitCode = exitCode,
            errorCode = errorCode,
            errorMessage = errorMessage
        )

        _resultsFlow.tryEmit(result)
    }

    companion object {
        const val ACTION_RESULT = "com.example.termux.RESULT_ACTION"
        const val EXTRA_COMMAND_ID = "EXTRA_COMMAND_ID"

        private val _resultsFlow = MutableSharedFlow<TermuxExecutionResult>(extraBufferCapacity = 32)
        val resultsFlow: SharedFlow<TermuxExecutionResult> = _resultsFlow.asSharedFlow()
    }
}
