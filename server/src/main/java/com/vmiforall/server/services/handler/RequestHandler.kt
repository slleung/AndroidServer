package com.vmiforall.server.services.handler

/**
 * An interface for request handlers.
 */
interface RequestHandler<T, R> {

    /**
     * Handles request T and responds with response R.
     */
    suspend fun handle(request: T): R

}
