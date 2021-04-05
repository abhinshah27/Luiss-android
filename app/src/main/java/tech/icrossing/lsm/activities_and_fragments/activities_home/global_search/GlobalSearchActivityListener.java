/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.activities_and_fragments.activities_home.global_search;

import tech.icrossing.lsm.models.enums.GlobalSearchTypeEnum;

/**
 * @author mbaldrighi on 4/10/2018.
 */
public interface GlobalSearchActivityListener {

	void showInterestsUsersListFragment(String query, GlobalSearchTypeEnum returnType, String title);
	void showGlobalTimelineFragment(String listName, String postId, String userId, String name,
	                                String avatarUrl, String query);
}
