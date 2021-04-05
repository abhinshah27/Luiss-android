/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.activities_and_fragments.activities_home.profile;

import androidx.annotation.Nullable;
import tech.icrossing.lsm.models.GenericUserFamilyRels;
import tech.icrossing.lsm.models.enums.SearchTypeEnum;

/**
 * @author mbaldrighi on 12/22/2017.
 */
public interface ProfileActivityListener {

	void showProfileCardFragment(@Nullable ProfileHelper.ProfileType type, String objectId);
	void showUserDetailFragment(String userId);

	void showDiaryFragment(String userId, String name, String avatarUrl);
	void showDiaryTimelineFragment(String listName, String postId, String userId, String name, String avatarUrl);

	void showInnerCircleFragment(String userId, String name, String avatarUrl);
	void showCircleViewMoreFragment(String circleName, String userId, String name, String avatarUrl);

	void showInterestsFragment(String userId, String name, String avatarUrl);
	void showFollowInterestFragment(String userId, String name, String avatarUrl);
	void showBrowseInterestByCategoryFragment(String categoryId, String categoryName);

	void showSearchFragment(String query, SearchTypeEnum type, String userId, String name, String avatarUrl);

	void showInterestDetailFragment(String interestId);
	void showFollowersFragment(String interestId, String name, String avatarUrl);

	void showSimilarForInterestFragment(String interestId, String name, String avatarUrl);
	void showSimilarEmptyDiaryFragment(String interestId, String name, String avatarUrl);

	void showClaimInterestFragment(String interestId, String name, String avatarUrl);

	void showFamiyRelationsStep1Fragment();
	void showFamilyRelationsStep2Fragment(GenericUserFamilyRels selectedUser);
}
