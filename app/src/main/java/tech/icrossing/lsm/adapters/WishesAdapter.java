/*
 * Copyright (c) 2018. Highlanders LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package tech.icrossing.lsm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tech.icrossing.lsm.R;
import tech.icrossing.lsm.models.HLWish;
import tech.icrossing.lsm.utility.Utils;
import tech.icrossing.lsm.utility.helpers.MediaHelper;

/**
 * @author mbaldrighi on 3/14/2018.
 */
public class WishesAdapter extends RecyclerView.Adapter<WishesAdapter.WishVH> {

	private List<HLWish> items;
	private WishesSwipeAdapterInterface listener;

	public WishesAdapter(List<HLWish> items, WishesSwipeAdapterInterface listener) {
		this.items = items;
		this.listener = listener;
	}

	@NonNull
	@Override
	public WishesAdapter.WishVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new WishVH(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_wish, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull WishesAdapter.WishVH holder, int position) {
		HLWish obj = items.get(position);
		holder.setWish(obj);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).hashCode();
	}


	public class WishVH extends RecyclerView.ViewHolder implements View.OnClickListener {

		private final ImageView placeholder;
		private final ImageView coverPicture;
		private final TextView name;

		private HLWish currentObject;

		WishVH(View itemView) {
			super(itemView);

			placeholder = itemView.findViewById(R.id.placeholder_icon);
			coverPicture = itemView.findViewById(R.id.wish_preview);
			name = itemView.findViewById(R.id.wish_name);
			itemView.setOnClickListener(this);
			itemView.findViewById(R.id.btn_delete).setOnClickListener(this);
		}

		void setWish(HLWish obj) {
			currentObject = obj;
			if (Utils.isStringValid(obj.getCoverURL())) {
				MediaHelper.loadPictureWithGlide(coverPicture.getContext(), obj.getCoverURL(), coverPicture);
				placeholder.getLayoutParams().width = Utils.dpToPx(20f, placeholder.getResources());
				placeholder.getLayoutParams().height = Utils.dpToPx(20f, placeholder.getResources());
				placeholder.setImageResource(R.drawable.ic_placeholder_image);
			}
			else {
				placeholder.getLayoutParams().width = Utils.dpToPx(30f, placeholder.getResources());
				placeholder.getLayoutParams().height = Utils.dpToPx(30f, placeholder.getResources());
				placeholder.setImageResource(R.drawable.ic_placeholder_wish);
			}

			name.setText(obj.getName());
		}

		public HLWish getCurrentObject() {
			return currentObject;
		}

		@Override
		public void onClick(View view) {

			if (view.getId() == R.id.btn_delete)
				listener.onRemove(this);
			else
				listener.onItemClick(currentObject);
		}
	}

	public interface WishesSwipeAdapterInterface {
		void onItemClick(HLWish object);
		void onRemove(WishVH viewHolder);
	}
}
