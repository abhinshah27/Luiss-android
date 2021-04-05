/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.adapters;


import android.view.View;

import tech.icrossing.lsm.models.HLUser;

/**
 * @author mbaldrighi on 1/24/2018.
 */
public interface BasicAdapterInteractionsListener {
	void onItemClick(Object object);
	void onItemClick(Object object, View view);
	HLUser getUser();
}
