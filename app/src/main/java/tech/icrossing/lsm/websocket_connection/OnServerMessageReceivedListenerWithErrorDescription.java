/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.websocket_connection;

/**
 * @author mbaldrighi on 10/29/2017.
 */
public interface OnServerMessageReceivedListenerWithErrorDescription extends OnServerMessageReceivedListener {

	void handleErrorResponse(int operationId, int errorCode, String description);

}
