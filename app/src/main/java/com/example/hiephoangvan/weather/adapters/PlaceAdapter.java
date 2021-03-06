package com.example.hiephoangvan.weather.adapters;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.hiephoangvan.weather.R;
import com.example.hiephoangvan.weather.Utils.UtilPref;
import com.example.hiephoangvan.weather.databases.Datamanager;
import com.example.hiephoangvan.weather.interfaces.ItemOnClick;
import com.example.hiephoangvan.weather.databases.Places;
import java.util.List;
import butterknife.BindView;

public class PlaceAdapter extends BaseAdapter<Places, PlaceAdapter.ViewHolder> {
    ItemOnClick mListener;
    int homePossition = 0;

    public PlaceAdapter(List<Places> data, Context context, ItemOnClick mListener) {
        super(data, context);
        this.mListener = mListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        homePossition = UtilPref.getInstance().getInt("homePossition", 0);
        return new ViewHolder(getViewItem(parent));
    }

    @Override
    public int layoutItem() {
        return R.layout.item_place;
    }

    public class ViewHolder extends BaseViewHolder<Places> {
        @BindView(R.id.image_place)
        ImageView mImagePlace;
        @BindView(R.id.tv_place)
        TextView mPlace;
        @BindView(R.id.btn_more)
        ImageView mBtnMore;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindView(Places data, int position) {
            mPlace.setSelected(true);
            if (position == homePossition) {
                mImagePlace.setImageResource(R.drawable.ic_home);
            } else {
                mImagePlace.setImageResource(R.drawable.ic_location);
            }
            mPlace.setText(data.getAddress());
            mBtnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(context, mBtnMore);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.popup_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.it_setashome:
                                    UtilPref.getInstance().setInt("homePossition", position);
                                    homePossition = position;
                                    notifyDataSetChanged();
                                    mListener.onClick(v, position);
                                    return true;
                                case R.id.it_delete:
                                    if (homePossition == position) {
                                        homePossition = 0;
                                        UtilPref.getInstance().setInt("homePossition", 0);
                                    }
                                    Datamanager.getInstance().deletePlace(data);
                                    notifyDataSetChanged();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    Menu popupMenu = popup.getMenu();
                    if (position == 0)
                        popupMenu.findItem(R.id.it_delete).setVisible(false);
                    else
                        popupMenu.findItem(R.id.it_delete).setVisible(true);
                    popup.show();

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(v, position);
                }
            });
        }

    }
}
