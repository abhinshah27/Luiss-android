/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.activities_and_fragments.activities_home.global_search;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;

import java.util.ArrayList;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import tech.icrossing.lsm.R;
import tech.icrossing.lsm.activities_and_fragments.ViewAllTagsActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_create_post.CreatePostActivityMod;
import tech.icrossing.lsm.activities_and_fragments.activities_home.HomeActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.profile.ProfileActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.profile.ProfileHelper;
import tech.icrossing.lsm.activities_and_fragments.activities_home.timeline.OnTimelineFragmentInteractionListener;
import tech.icrossing.lsm.activities_and_fragments.activities_home.timeline.PostOverlayActionActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.timeline.TimelineFragment;
import tech.icrossing.lsm.base.BasicInteractionListener;
import tech.icrossing.lsm.base.HLActivity;
import tech.icrossing.lsm.base.OnBackPressedListener;
import tech.icrossing.lsm.models.HLPosts;
import tech.icrossing.lsm.models.Post;
import tech.icrossing.lsm.models.Tag;
import tech.icrossing.lsm.models.chat.ChatRoom;
import tech.icrossing.lsm.models.enums.GlobalSearchTypeEnum;
import tech.icrossing.lsm.utility.Constants;
import tech.icrossing.lsm.utility.FragmentsUtils;
import tech.icrossing.lsm.utility.Utils;
import tech.icrossing.lsm.utility.helpers.FullScreenHelper;
import tech.icrossing.lsm.utility.helpers.MediaHelper;
import tech.icrossing.lsm.utility.helpers.NotificationAndRequestHelper;
import tech.icrossing.lsm.utility.helpers.PostBottomSheetHelper;
import tech.icrossing.lsm.websocket_connection.OnMissingConnectionListener;
import tech.icrossing.lsm.websocket_connection.OnServerMessageReceivedListener;
import tech.icrossing.lsm.websocket_connection.ServerMessageReceiver;

/**
 * @author mbaldrighi on 4/9/2018.
 */
public class GlobalSearchActivity extends HLActivity implements View.OnClickListener,
		GlobalSearchActivityListener, OnTimelineFragmentInteractionListener, BasicInteractionListener,
		OnServerMessageReceivedListener, OnMissingConnectionListener {

	private FragmentManager fragmentManager;

	private MediaHelper mediaHelper;
	private FullScreenHelper fullScreenListener;
	private FullScreenHelper.FullScreenType fullScreenSavedState;
	private FullScreenHelper.RestoreFullScreenStateListener fsStateListener;

	/**
	 * Serves for TIMELINE pagination purpose.
	 */
	private int lastPageID = 1;

	private View rootView;

	private View bottomBar, notificationDot, notificationDotChat;
	private OnBackPressedListener backListener;

	private PostBottomSheetHelper bottomSheetHelper;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(
				rootView = LayoutInflater.from(this)
						.inflate(R.layout.activity_global_search, null, false)
		);
		setRootContent(R.id.root_content);
		setProgressIndicator(R.id.generic_progress_indicator);

		fragmentManager = getSupportFragmentManager();

		configureBottomBar();

		mediaHelper = new MediaHelper();
		fullScreenListener = new FullScreenHelper(this, false);

		manageIntent();

		configurePostOptionsSheets();
	}

	@Override
	protected void onStart() {
		super.onStart();

		configureResponseReceiver();
	}

	@Override
	protected void onResume() {
		super.onResume();

//		TimelineFragment tlFragment = (TimelineFragment) fragmentManager.findFragmentByTag(TimelineFragment.LOG_TAG);
//		bottomBar.setVisibility((tlFragment != null) ? View.GONE : View.VISIBLE);
//		bottomBar1.setVisibility((tlFragment != null) ? View.VISIBLE : View.GONE);
//
//		InterestsUsersListFragment iuFragment = (InterestsUsersListFragment) fragmentManager.findFragmentByTag(InterestsUsersListFragment.LOG_TAG);
//		bottomBar1.setVisibility((iuFragment != null) ? View.GONE : View.VISIBLE);
//		bottomBar.setVisibility((iuFragment != null) ? View.VISIBLE : View.GONE);

		NotificationAndRequestHelper.handleDotVisibility(notificationDot, mUser.isValid());
//		NotificationAndRequestHelper.handleDotVisibility(notificationDot1, mUser.isValid());

		boolean condition = mUser.isValid() && ChatRoom.Companion.areThereUnreadMessages(null, realm);
		notificationDotChat.setVisibility(condition ? View.VISIBLE : View.GONE);
//		notificationDotChat1.setVisibility(condition ? View.VISIBLE : View.GONE);


		if (bottomSheetHelper != null)
			bottomSheetHelper.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (bottomSheetHelper != null)
			bottomSheetHelper.onPause();
	}

	@Override
	protected void onDestroy() {
		HLPosts.getInstance().resetPropertiesForDiaryOrGlobalSearch();
		super.onDestroy();
	}


	@Override
	public void onClick(View v) {
		if (!Utils.checkAndOpenLogin(this, mUser, HomeActivity.PAGER_ITEM_HOME_WEBVIEW)) {
			switch (v.getId()) {
				case R.id.btn_close:
					finish();
					break;

				case R.id.bottom_timeline:
				case R.id.bottom_profile:
				case R.id.bottom_notifications:
				case R.id.bottom_settings:
				case R.id.main_action_btn:
					handleBottomBarAction(v.getId());
					break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (backListener != null)
			backListener.onBackPressed();
		else if (fragmentManager.getBackStackEntryCount() == 1)
			finish();
		else
			super.onBackPressed();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			// caught when returning from PostOverlayActivity
			case Constants.RESULT_TIMELINE_INTERACTIONS:
				if (fsStateListener != null)
					fsStateListener.restoreFullScreenState(fullScreenSavedState);
				break;
		}
	}

	@Override
	protected void configureResponseReceiver() {
		if (serverMessageReceiver == null)
			serverMessageReceiver = new ServerMessageReceiver();
		serverMessageReceiver.setListener(this);
	}

	@Override
	protected void manageIntent() {
		Intent intent = getIntent();
		if(intent == null)
			return;

		int showFragment = intent.getIntExtra(Constants.FRAGMENT_KEY_CODE,
				Constants.FRAGMENT_INVALID);
		int requestCode = intent.getIntExtra(Constants.REQUEST_CODE_KEY, Constants.NO_RESULT);
		Bundle extras = intent.getExtras();

		switch (showFragment) {
			case Constants.FRAGMENT_GLOBAL_SEARCH:
				if (extras != null) {
					String query = extras.getString(Constants.EXTRA_PARAM_1);
					addGlobalSearchFragment(null, query, requestCode, false);
				}
				break;

			case Constants.FRAGMENT_GLOBAL_SEARCH_TIMELINE:
				if (extras != null) {
					String listName = extras.getString(Constants.EXTRA_PARAM_1);
					String postId = extras.getString(Constants.EXTRA_PARAM_2);
					String userId = extras.getString(Constants.EXTRA_PARAM_3);
					String name = extras.getString(Constants.EXTRA_PARAM_4);
					String avatar = extras.getString(Constants.EXTRA_PARAM_5);
					String query = extras.getString(Constants.EXTRA_PARAM_6);
					addGlobalTimelineFragment(null, listName, postId, userId, name, avatar,
							query, requestCode, false);
				}
				break;

			case Constants.FRAGMENT_GLOBAL_SEARCH_USERS_INTERESTS:
				if (extras != null) {
					String query = extras.getString(Constants.EXTRA_PARAM_1);
					GlobalSearchTypeEnum returnType = (GlobalSearchTypeEnum)
							extras.getSerializable(Constants.EXTRA_PARAM_2);
					String title = extras.getString(Constants.EXTRA_PARAM_3);
					addInterestsUsersListFragment(null, query, returnType, title, requestCode, false);
				}
				break;
		}
	}

	@Override
	public void handleSuccessResponse(int operationId, JSONArray responseObject) {
		super.handleSuccessResponse(operationId, responseObject);

		if (operationId == Constants.SERVER_OP_GET_NOTIFICATION_COUNT) {
			NotificationAndRequestHelper.handleDotVisibility(notificationDot, mUser.isValid());
//			NotificationAndRequestHelper.handleDotVisibility(notificationDot1, mUser.isValid());
		}
	}

	@Override
	public void onMissingConnection(int operationId) {}


	//region == Class custom methods ==

	private void configureBottomBar() {

		bottomBar = findViewById(R.id.bottom_bar);
		if (bottomBar != null) {
			View l1 = bottomBar.findViewById(R.id.bottom_timeline);
			l1.setOnClickListener(this);
			View l2 = bottomBar.findViewById(R.id.bottom_profile);
			l2.setOnClickListener(this);
			View l3 = bottomBar.findViewById(R.id.bottom_notifications);
			l3.setOnClickListener(this);
			View l4 = bottomBar.findViewById(R.id.bottom_global_search);
			l4.setSelected(true);

			ImageView ib4 = bottomBar.findViewById(R.id.icon_global_search);
			TransitionDrawable td4 = (TransitionDrawable) ib4.getDrawable();
			td4.setCrossFadeEnabled(true);
			td4.startTransition(0);

			View main = bottomBar.findViewById(R.id.main_action_btn);
			main.setOnClickListener(this);

			notificationDot = bottomBar.findViewById(R.id.notification_dot);
			notificationDotChat = bottomBar.findViewById(R.id.notification_dot);
		}

//		bottomBar1 = findViewById(R.id.bottom_bar_1);
//		if (bottomBar1 != null) {
//			View l1 = bottomBar1.findViewById(R.id.bottom_timeline);
//			l1.setOnClickListener(this);
//			View l2 = bottomBar1.findViewById(R.id.bottom_profile);
//			l2.setOnClickListener(this);
//			View l3 = bottomBar1.findViewById(R.id.bottom_chats);
//			l3.setOnClickListener(this);
//			View l4 = bottomBar1.findViewById(R.id.bottom_global_search);
//			l4.setSelected(true);
//
//			ImageView ib4 = bottomBar1.findViewById(R.id.icon_global_search);
//			TransitionDrawable td4 = (TransitionDrawable) ib4.getDrawable();
//			td4.setCrossFadeEnabled(true);
//			td4.startTransition(0);
//
//			ImageView main = bottomBar1.findViewById(R.id.main_action_btn);
//			main.setOnClickListener(this);
//
//			notificationDot1 = bottomBar1.findViewById(R.id.notification_dot);
//			notificationDotChat1 = bottomBar1.findViewById(R.id.notification_dot_chat);
//		}
	}

	private void handleBottomBarAction(@IdRes int viewId) {
		Intent intent = new Intent();

		if (viewId == R.id.main_action_btn) {
			intent.setClass(this, CreatePostActivityMod.class);
		}
		else{
			intent.setClass(this, HomeActivity.class);
			int pageId = -1;
			switch (viewId) {
				case R.id.bottom_profile:
					pageId = HomeActivity.PAGER_ITEM_PROFILE;
					break;
				case R.id.bottom_notifications:
					pageId = HomeActivity.PAGER_ITEM_NOTIFICATIONS;
					break;
				case R.id.bottom_timeline:
					pageId = HomeActivity.PAGER_ITEM_TIMELINE;
					break;
			}

			if (pageId != -1)
				intent.putExtra(Constants.EXTRA_PARAM_1, pageId);
		}

		startActivity(intent);
		finish();
	}

	private void configurePostOptionsSheets() {
		if (bottomSheetHelper == null) {
			bottomSheetHelper = new PostBottomSheetHelper(this);
		}
		bottomSheetHelper.configurePostOptionsSheets(rootView);
	}

	//endregion


	//region == GlobalActivity listener interface ==

	/* FRAGMENTs section */

	public static void openGlobalSearchFragment(Context context, String query) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_PARAM_1, query);
		FragmentsUtils.openFragment(context, bundle, Constants.FRAGMENT_GLOBAL_SEARCH, Constants.NO_RESULT,
				GlobalSearchActivity.class);
	}

	private void addGlobalSearchFragment(Fragment target, String query, int requestCode, boolean animate) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(animate)
			fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
					R.anim.slide_in_left, R.anim.slide_out_right);

		GlobalSearchFragment fragment = (GlobalSearchFragment) fragmentManager.findFragmentByTag(GlobalSearchFragment.LOG_TAG);
		if (fragment == null) {
			fragment = GlobalSearchFragment.newInstance(query);
			FragmentsUtils.addFragmentNull(fragmentTransaction, R.id.pages_container, fragment,
					GlobalSearchFragment.LOG_TAG, target, requestCode);
		} else
			FragmentsUtils.addFragmentNotNull(fragmentTransaction, fragment, target, requestCode);
		fragmentTransaction.commit();
	}


	public static void openGlobalSearchUsersInterestsFragment(Context context, String query, GlobalSearchTypeEnum returnType,
	                                                          String title) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_PARAM_1, query);
		bundle.putSerializable(Constants.EXTRA_PARAM_2, returnType);
		bundle.putString(Constants.EXTRA_PARAM_3, title);
		FragmentsUtils.openFragment(context, bundle, Constants.FRAGMENT_GLOBAL_SEARCH_USERS_INTERESTS, Constants.NO_RESULT,
				GlobalSearchActivity.class);
	}

	@Override
	public void showInterestsUsersListFragment(String query, GlobalSearchTypeEnum returnType,
	                                           String title) {
		addInterestsUsersListFragment(null, query, returnType, title, Constants.NO_RESULT, true);
	}

	private void addInterestsUsersListFragment(Fragment target, String query,
	                                           GlobalSearchTypeEnum returnType, String title,
	                                           int requestCode, boolean animate) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(animate)
			fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
					R.anim.slide_in_left, R.anim.slide_out_right);

		InterestsUsersListFragment fragment = (InterestsUsersListFragment) fragmentManager.findFragmentByTag(InterestsUsersListFragment.LOG_TAG);
		if (fragment == null) {
			fragment = InterestsUsersListFragment.newInstance(query, returnType, title);
			FragmentsUtils.addFragmentNull(fragmentTransaction, R.id.pages_container, fragment,
					InterestsUsersListFragment.LOG_TAG, target, requestCode);
		} else
			FragmentsUtils.addFragmentNotNull(fragmentTransaction, fragment, target, requestCode);
		fragmentTransaction.commit();

//		bottomBar.setVisibility(View.VISIBLE);
//		bottomBar1.setVisibility(View.GONE);
	}


	public static void openGlobalSearchTimelineFragment(Context context, String listName, String postId, String userId,
	                                                    String name, String avatarUrl, String query) {
		Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_PARAM_1, listName);
		bundle.putString(Constants.EXTRA_PARAM_2, postId);
		bundle.putString(Constants.EXTRA_PARAM_3, userId);
		bundle.putString(Constants.EXTRA_PARAM_4, name);
		bundle.putString(Constants.EXTRA_PARAM_5, avatarUrl);
		bundle.putString(Constants.EXTRA_PARAM_6, query);
		FragmentsUtils.openFragment(context, bundle, Constants.FRAGMENT_GLOBAL_SEARCH_TIMELINE, Constants.NO_RESULT,
				GlobalSearchActivity.class);
	}

	@Override
	public void showGlobalTimelineFragment(String listName, String postId, String userId,
	                                       String name, String avatarUrl, String query) {
		addGlobalTimelineFragment(null, listName, postId, userId, name, avatarUrl, query,
				Constants.NO_RESULT, true);

//		bottomBar1.setVisibility(View.VISIBLE);
//		bottomBar.setVisibility(View.GONE);
	}

	private void addGlobalTimelineFragment(Fragment target, String listName, String postId,
	                                       String userId, String name, String avatarUrl, String query,
	                                       int requestCode, boolean animate) {
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		if(animate)
			fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
					R.anim.slide_in_left, R.anim.slide_out_right);

		TimelineFragment fragment = (TimelineFragment) fragmentManager.findFragmentByTag(TimelineFragment.LOG_TAG);
//		if (fragment == null) {
		fragment = TimelineFragment.newInstance(TimelineFragment.FragmentUsageType.GLOBAL_SEARCH,
				userId, name, avatarUrl, postId, listName, query);
		FragmentsUtils.addFragmentNull(fragmentTransaction, R.id.pages_container, fragment,
				TimelineFragment.LOG_TAG, target, requestCode);
//		} else
//			FragmentsUtils.addFragmentNotNull(fragmentTransaction, fragment, target, requestCode);
		fragmentTransaction.commit();
	}


	//endregion


	//region == Timeline interface ==

	@Override
	public void actionsForLandscape(@NonNull String postId, View view) {

		// TODO: 3/23/2018     needs implementation

	}

	@Override
	public void setLastAdapterPosition(int position) {}

	@Override
	public Integer getLastAdapterPosition() {
		return null;
	}

	@Override
	public FullScreenHelper getFullScreenListener() {
		return fullScreenListener;
	}

	@Override
	public Toolbar getToolbar() {
		return null;
	}

	@Override
	public View getBottomBar() {
		return bottomBar;
	}

	@Override
	public MediaHelper getMediaHelper() {
		if (mediaHelper == null)
			mediaHelper = new MediaHelper();

		return mediaHelper;
	}

	@Override
	public void goToInteractionsActivity(@NonNull String postId) {
		Intent intent = new Intent(this, PostOverlayActionActivity.class);
		intent.putExtra(Constants.EXTRA_PARAM_1, postId);
		startActivityForResult(intent, Constants.RESULT_TIMELINE_INTERACTIONS);
	}

	@Override
	public void saveFullScreenState() {
		fullScreenSavedState = fullScreenListener.getFullScreenType();
	}

	@Override
	public int getPageIdToCall() {
		return lastPageID + 1;
	}

	@Override
	public int getLastPageID() {
		return lastPageID;
	}

	@Override
	public void setLastPageID(int lastPageID) {
		this.lastPageID = lastPageID;
	}

	@Override
	public HLActivity getActivity() {
		return this;
	}

	@Override
	public void setFsStateListener(FullScreenHelper.RestoreFullScreenStateListener fsStateListener) {
		this.fsStateListener = fsStateListener;
	}

	@Override public void openPostSheet(@NonNull String postId, boolean isOwnPost) {
		if (bottomSheetHelper != null)
			bottomSheetHelper.openPostSheet(postId, isOwnPost);
	}

	@Override
	public void closePostSheet() {
		if (bottomSheetHelper != null)
			bottomSheetHelper.closePostSheet();
	}

	@Override
	public boolean isPostSheetOpen() {
		return isPostSheetOpen(null);
	}

	public boolean isPostSheetOpen(SlidingUpPanelLayout layout) {
		return bottomSheetHelper != null && bottomSheetHelper.isPostSheetOpen(layout);
	}

	@Override
	public void goToProfile(@NonNull String userId, boolean isInterest) {
		if (mUser.getId().equals(userId)) {
			Intent intent = new Intent(this, HomeActivity.class);
			intent.putExtra(Constants.EXTRA_PARAM_1, HomeActivity.PAGER_ITEM_PROFILE);
			startActivity(intent);
			finish();
		}
		else {
			ProfileHelper.ProfileType type = isInterest ? ProfileHelper.ProfileType.INTEREST_NOT_CLAIMED : ProfileHelper.ProfileType.NOT_FRIEND;
			ProfileActivity.openProfileCardFragment(this, type, userId, HomeActivity.PAGER_ITEM_HOME_WEBVIEW);
		}
	}

	@Override
	public void viewAllTags(Post post) {
		if (post != null) {
			if (post.hasTags()) {
				ArrayList<Tag> temp = new ArrayList<>();
				temp.addAll(post.getTags());

				Intent intent = new Intent(this, ViewAllTagsActivity.class);
				intent.putParcelableArrayListExtra(Constants.EXTRA_PARAM_1, temp);
				intent.putExtra(Constants.EXTRA_PARAM_2, true);
				startActivity(intent);
			}
		}
	}

	//endregion


	//region == Getters and setters ==

	public void setBackListener(OnBackPressedListener backListener) {
		this.backListener = backListener;
	}

	public PostBottomSheetHelper getBottomSheetHelper() {
		return bottomSheetHelper;
	}

	//endregion

}
