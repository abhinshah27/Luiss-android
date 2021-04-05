/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.utility.helpers;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.realm.Realm;
import tech.icrossing.lsm.models.Post;

/**
 * @author mbaldrighi on 9/27/2017.
 */
public interface RealTimeCommunicationListener extends Serializable {

	void onPostAdded(@Nullable Post post, int position);
	void onPostUpdated(@NonNull String postId, int position);
	void onPostDeleted(int position);
	void onHeartsUpdated(int position);
	void onSharesUpdated(int position);
	void onTagsUpdated(int position);
	void onCommentsUpdated(int position);

	void onNewDataPushed(boolean hasInsert);

	void registerRealTimeReceiver();
	void unregisterRealTimeReceiver();

	Realm getRealm();

}
