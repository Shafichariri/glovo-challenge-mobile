package com.shafic.challenge.common

import com.shafic.challenge.network.NetworkErrors

sealed class BaseEvent {
    class ConnectionFailed(val error: NetworkErrors.NetworkError) : BaseEvent()
}
