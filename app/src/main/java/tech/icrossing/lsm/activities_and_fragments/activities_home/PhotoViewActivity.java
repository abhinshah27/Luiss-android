/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.activities_and_fragments.activities_home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import androidx.annotation.Nullable;
import tech.icrossing.lsm.R;
import tech.icrossing.lsm.base.HLActivity;
import tech.icrossing.lsm.utility.AnalyticsUtils;
import tech.icrossing.lsm.utility.Constants;
import tech.icrossing.lsm.utility.GlideApp;
import tech.icrossing.lsm.utility.LogUtils;
import tech.icrossing.lsm.utility.Utils;
import tech.icrossing.lsm.utility.caches.PicturesCache;
import tech.icrossing.lsm.utility.helpers.HLMediaType;
import tech.icrossing.lsm.utility.helpers.ShareHelper;

/**
 * @author mbaldrighi on 4/25/2018.
 */
public class PhotoViewActivity extends HLActivity implements ShareHelper.ShareableProvider {

	public static final String LOG_TAG = PhotoViewActivity.class.getCanonicalName();

	private String urlToLoad, transitionName;
	private File fileToLoad;
	private String postOrMessageId;
	private boolean fromChat;

	private ShareHelper mShareHelper;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view_constr);
		setRootContent(R.id.root_content);
		setProgressIndicator(R.id.progress);

		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
		decorView.setSystemUiVisibility(uiOptions);
		setImmersiveValue(true);

		supportPostponeEnterTransition();


//		configureToolbar(findViewById(R.id.toolbar), "", false);


		manageIntent();

		findViewById(R.id.close_btn).setOnClickListener(v -> onBackPressed());
		findViewById(R.id.share_btn).setOnClickListener(v -> mShareHelper.initOps(fromChat));

		PhotoView mPhotoView = findViewById(R.id.photo_view);
		if (Utils.areStringsValid(urlToLoad, transitionName)) {
			if (Utils.hasLollipop())
				mPhotoView.setTransitionName(transitionName);

			GlideApp.with(this)
					.asDrawable()
					.fitCenter()
					.load(fileToLoad != null ? fileToLoad : urlToLoad)
					.dontAnimate()
					.listener(new RequestListener<Drawable>() {
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							if (e != null)
								LogUtils.e(LOG_TAG, e.getMessage(), e);
							return false;
						}

						@Override
						public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
							supportStartPostponedEnterTransition();
							return false;
						}
					})
					.into(mPhotoView);
		}

		mShareHelper = new ShareHelper(this, this);

	}

	@Override
	protected void onResume() {
		super.onResume();

		AnalyticsUtils.trackScreen(this, AnalyticsUtils.FEED_IMAGE_VIEWER);

		mShareHelper.onResume();
	}

	@Override
	protected void onStop() {

		mShareHelper.onStop();

		super.onStop();
	}

	//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu_viewer_sharing, menu);
//
//		// Locate MenuItem with ShareActionProvider
//		MenuItem item = menu.findItem(R.id.share);
//
//		// Fetch and store ShareActionProvider
//		mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setType("text/plain");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, transitionName);
//        mShareActionProvider.setShareIntent(shareIntent);
//
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		return super.onOptionsItemSelected(item);
//	}


	@Override
	protected void configureResponseReceiver() {}

	@Override
	protected void manageIntent() {
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(Constants.EXTRA_PARAM_1))
				urlToLoad = intent.getStringExtra(Constants.EXTRA_PARAM_1);
			if (intent.hasExtra(Constants.EXTRA_PARAM_2))
				transitionName = intent.getStringExtra(Constants.EXTRA_PARAM_2);
			if (intent.hasExtra(Constants.EXTRA_PARAM_3))
				postOrMessageId = intent.getStringExtra(Constants.EXTRA_PARAM_3);
			if (intent.hasExtra(Constants.EXTRA_PARAM_4))
				fromChat = intent.getBooleanExtra(Constants.EXTRA_PARAM_4, false);

			Object objToLoad = PicturesCache.Companion.getInstance(this).getMedia(urlToLoad, HLMediaType.PHOTO);
			if (objToLoad instanceof Uri) {
				String path = ((Uri) objToLoad).getPath();
				fileToLoad = Utils.isStringValid(path) ? new File(path) : null;
			} else if (objToLoad instanceof File) fileToLoad = ((File) objToLoad);
		}
	}


	//region == SHARE ==

	@org.jetbrains.annotations.Nullable
	@Override
	public View getProgressView() {
		return null;
	}

	@Override
	public void afterOps() {}

	@NotNull
	@Override
	public String getUserID() {
		return mUser.getId();
	}

	@NotNull
	@Override
	public String getPostOrMessageID() {
		return postOrMessageId;
	}

	//endregion

}
