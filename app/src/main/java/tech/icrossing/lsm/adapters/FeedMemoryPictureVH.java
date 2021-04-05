/*
 * Copyright (c) 2017. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import tech.icrossing.lsm.activities_and_fragments.activities_home.HomeActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.PhotoViewActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.profile.ProfileActivity;
import tech.icrossing.lsm.activities_and_fragments.activities_home.profile.ProfileHelper;
import tech.icrossing.lsm.activities_and_fragments.activities_home.timeline.OnTimelineFragmentInteractionListener;
import tech.icrossing.lsm.activities_and_fragments.activities_home.timeline.TimelineFragment;
import tech.icrossing.lsm.models.Post;
import tech.icrossing.lsm.models.enums.PostTypeEnum;
import tech.icrossing.lsm.utility.Constants;
import tech.icrossing.lsm.utility.GlideApp;
import tech.icrossing.lsm.utility.GlideRequest;
import tech.icrossing.lsm.utility.GlideRequests;
import tech.icrossing.lsm.utility.LogUtils;
import tech.icrossing.lsm.utility.caches.PicturesCache;
import tech.icrossing.lsm.utility.helpers.HLMediaType;

/**
 * @author mbaldrighi on 9/28/2017.
 */
class FeedMemoryPictureVH extends FeedMemoryViewHolder {

	public static final String LOG_TAG = FeedMemoryPictureVH.class.getCanonicalName();

	private ImageView mainView;

	FeedMemoryPictureVH(View view, TimelineFragment fragment,
						OnTimelineFragmentInteractionListener mListener) {
		super(view, fragment, mListener);

		mainView = (ImageView) mMainView;
	}


	public void onBindViewHolder(@NonNull Post object) {
		super.onBindViewHolder(object);

		LogUtils.d(LOG_TAG, "onBindViewHolder called for object: " + object.hashCode());

		ViewCompat.setTransitionName(mainView, mItem.getContent());

		// INFO: 2/26/19    adds background color handling here due to following edit
//		mainView.setBackgroundColor(mItem.getBackgroundColor(fragment.getResources()));

		if (super.mItem != null) {
			Object media = PicturesCache.Companion.getInstance(fragment.getContext())
					.getMedia(mItem.getContent(), HLMediaType.PHOTO);

			if (mainView != null) {

				// INFO: 2/26/19    restores MATCH_PARENT for both width and height statically in XML
//				mainView.setLayoutParams(
//						new FrameLayout.LayoutParams(
//								mItem.doesMediaWantFitScale() ? FrameLayout.LayoutParams.WRAP_CONTENT : FrameLayout.LayoutParams.MATCH_PARENT,
//								mItem.doesMediaWantFitScale() ? FrameLayout.LayoutParams.WRAP_CONTENT : FrameLayout.LayoutParams.MATCH_PARENT,
//								Gravity.CENTER
//						)
//				);

				mainView.setScaleType(mItem.doesMediaWantFitScale() ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);

				GlideRequests glide = GlideApp.with(fragment);
				GlideRequest<Drawable> glideRequest;
				if (media instanceof File)
					glideRequest = glide.load(media);
				else if (media instanceof Uri)
					glideRequest = glide.load(new File(((Uri) media).getPath()));
				else
					glideRequest = glide.load(super.mItem.getContent());

				if (mItem.doesMediaWantFitScale())
					glideRequest.fitCenter();
				else
					glideRequest.centerCrop();

				glideRequest.into(mainView);
			}
		}
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (super.onSingleTapConfirmed(e) && mItem != null) {
			mListener.saveFullScreenState();

			if (mItem.getTypeEnum() == PostTypeEnum.FOLLOW_INTEREST) {
				if (mItem.hasNewFollowedInterest())
					ProfileActivity.openProfileCardFragment(
							getActivity(),
							ProfileHelper.ProfileType.INTEREST_NOT_CLAIMED,
							mItem.getFollowedInterestId(),
							HomeActivity.PAGER_ITEM_TIMELINE
					);
			}
			else {
				Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
				intent.putExtra(Constants.EXTRA_PARAM_1, mItem.getContent());
				intent.putExtra(Constants.EXTRA_PARAM_2, ViewCompat.getTransitionName(mainView));
				intent.putExtra(Constants.EXTRA_PARAM_3, mItem.getId());
				intent.putExtra(Constants.EXTRA_PARAM_4, false);

				ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
						getActivity(),
						mainView,
						ViewCompat.getTransitionName(mainView));

				mMainView.getContext().startActivity(intent, options.toBundle());
			}
			return true;
		}
		return false;
	}
}