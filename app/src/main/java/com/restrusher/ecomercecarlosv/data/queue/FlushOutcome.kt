package com.restrusher.ecomercecarlosv.data.queue

/**
 * Result of a [QueueProcessor.flush].
 *
 * [FAILED] and [DEFERRED] both mean "nothing was pushed", but they must not be reported the same
 * way: a deferred flush is our own missing session, not a problem with the user's data, so it earns
 * no error notification and no retry counter.
 */
enum class FlushOutcome {
    /** Everything pending was pushed, or there was nothing to push. */
    COMPLETED,

    /** At least one operation was rejected by the server. Worth telling the user about. */
    FAILED,

    /** No usable session yet — nothing was attempted. Silent; retry when the session returns. */
    DEFERRED,
}
