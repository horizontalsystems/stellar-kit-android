package io.horizontalsystems.stellarkit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.stellar.sdk.Server
import org.stellar.sdk.requests.EventListener
import org.stellar.sdk.requests.SSEStream
import org.stellar.sdk.responses.operations.OperationResponse
import java.util.Optional

class UpdateManager(private val server: Server, private val accountId: String) {

    private val _updateFlow = MutableSharedFlow<Unit>()
    val updateFlow = _updateFlow.asSharedFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var operationsRequest: SSEStream<OperationResponse>? = null

    fun start() {
        operationsRequest = server.operations()
            .forAccount(accountId)
            .includeFailed(true)
            .cursor("now")
            .stream(object : EventListener<OperationResponse> {
                override fun onEvent(operationResponse: OperationResponse) {
                    coroutineScope.launch {
                        _updateFlow.emit(Unit)
                    }
                }

                override fun onFailure(error: Optional<Throwable>, responseCode: Optional<Int>) {
                    stop()
                    start()
                }
            })
    }

    fun stop() {
        operationsRequest?.close()
    }
}
