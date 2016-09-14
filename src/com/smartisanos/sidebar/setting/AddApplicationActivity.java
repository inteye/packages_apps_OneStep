package com.smartisanos.sidebar.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.smartisanos.sidebar.R;
import com.smartisanos.sidebar.util.AppItem;
import com.smartisanos.sidebar.util.AppManager;

public class AddApplicationActivity extends BaseActivity {
    private static final String TAG = AddApplicationActivity.class.getName();

    private ListView mListView;
    private AppAdapter mAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddApplicationActivity.onCreate()...");
        setContentView(R.layout.add_app_activity);
        getWindow().setBackgroundDrawable(null);

        mListView = (ListView) findViewById(R.id.list_view);
        mAdapter = new AppAdapter(this);
        mListView.setAdapter(mAdapter);
        setupBackBtnOnTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.refreshData();
    }

    private final class AppAdapter extends BaseAdapter {

        private List<ViewItem> mItems = new ArrayList<ViewItem>();
        LayoutInflater mInflater;

        public AppAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            refreshData();
            AppManager.getInstance(context).addListener(mUpdateListener);
        }

        private AppManager.RecentUpdateListener mUpdateListener = new AppManager.RecentUpdateListener() {
            @Override
            public void onUpdate() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onDataChange();
                    }
                });
            }
        };

        private void onDataChange() {
            for (ViewItem vi : mItems) {
                boolean added = AppManager.getInstance(getApplicationContext()).isAppItemAdded(vi.appItem);
                vi.selected = added;
            }
            notifyDataSetChanged();
        }

        private void refreshData() {
            mItems.clear();
            for(AppItem ai: AppManager.getInstance(getApplicationContext()).getAddedAppItem()) {
                mItems.add(new ViewItem(ai, true));
            }
            for(AppItem ai: AppManager.getInstance(getApplicationContext()).getUnAddedAppItem()) {
                mItems.add(new ViewItem(ai, false));
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return (mItems.size() + 2) / 3;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder mHolder = null;
            if (view == null) {
                mHolder = new ViewHolder();
                view = mInflater.inflate(R.layout.app_picker_item, null);
                mHolder.spaceViewTop = view.findViewById(R.id.space_top);
                mHolder.spaceViewBottom = view.findViewById(R.id.space_bottom);
                mHolder.subViews[0] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_1);
                mHolder.subViews[1] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_2);
                mHolder.subViews[2] = (AppPickerSubView) view.findViewById(R.id.app_picker_sub_view_3);
                mHolder.view = view;
                view.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) view.getTag();
            }
            int index = position * 3;
            for (int i = 0; i < 3; ++i) {
                AppPickerSubView apsv = mHolder.subViews[i];
                final int pos = index + i;
                if (pos < mItems.size()) {
                    final ViewItem ai = mItems.get(pos);
                    apsv.setVisibility(View.VISIBLE);
                    apsv.setImageBitmap(ai.appItem.getAvatar());
                    apsv.setText(ai.appItem.getDisplayName());
                    apsv.setListener(null);
                    apsv.setSelected(ai.selected);
                    apsv.setListener(new AppPickerSubView.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(AppPickerSubView view, boolean isChecked) {
                            mItems.get(pos).selected = isChecked;
                            if (isChecked) {
                                AppManager.getInstance(getApplicationContext()).addAppItem(ai.appItem);
                            } else {
                                AppManager.getInstance(getApplicationContext()).removeAppItem(ai.appItem);
                            }
                        }
                    });
                } else {
                    apsv.setVisibility(View.INVISIBLE);
                }
            }

            mHolder.updateSpace(position, getCount());
            int bgRes = getBackgrondResId(position, getCount());
            if (bgRes > 0) {
                mHolder.view.setBackgroundResource(bgRes);
            }

            return view;
        }

        private int getBackgrondResId(int position, int count) {
            if (count <= 0 || position < 0 || position >= count) {
                return 0;
            }
            int bgRes = 0;
            if (count == 1) {
                bgRes = R.drawable.common_icon_picker_bg_single;
            } else {
                if (position == 0) {
                    bgRes = R.drawable.common_icon_picker_bg_top;
                } else if (position == count - 1) {
                    bgRes = R.drawable.common_icon_picker_bg_bottom;
                } else {
                    bgRes = R.drawable.common_icon_picker_bg_middle;
                }
            }
            return bgRes;
        }

        class ViewHolder {
            View view;
            View spaceViewTop, spaceViewBottom;
            AppPickerSubView[] subViews = new AppPickerSubView[3];

            public void updateSpace(int postion, int all) {
                spaceViewTop.setVisibility(postion == 0 ? View.VISIBLE
                        : View.GONE);
                spaceViewBottom.setVisibility(postion == all - 1 ? View.VISIBLE
                        : View.GONE);
            }
        }

        class ViewItem {
            public AppItem appItem;
            public boolean selected;
            public ViewItem(AppItem ai, boolean selected) {
                this.appItem = ai;
                this.selected = selected;
            }
        }
    }
}