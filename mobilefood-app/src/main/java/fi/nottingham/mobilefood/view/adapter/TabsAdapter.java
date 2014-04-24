package fi.nottingham.mobilefood.view.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ViewGroup;
import fi.nottingham.mobilefood.view.IMainView;
import fi.nottingham.mobilefood.view.ViewIsReadyListener;

public class TabsAdapter extends FragmentStatePagerAdapter implements
		ActionBar.TabListener, ViewPager.OnPageChangeListener {
	private static final String TAG = "TabsAdapter";
	private final Context mContext;
	private final ActionBar mActionBar;
	private final ViewPager mViewPager;
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

	public final class TabInfo {
		private final Class<?> clss;
		public final Bundle args;

		TabInfo(Class<?> _class, Bundle _args) {
			clss = _class;
			args = _args;
		}
	}

	public TabsAdapter(ActionBarActivity activity, ViewPager pager) {
		super(activity.getSupportFragmentManager());
		mContext = activity;
		mActionBar = activity.getSupportActionBar();
		mViewPager = pager;
		mViewPager.setAdapter(this);
		mViewPager.setOnPageChangeListener(this);
	}

	public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
		TabInfo info = new TabInfo(clss, args);
		tab.setTag(info);
		tab.setTabListener(this);
		mTabs.add(info);
		mActionBar.addTab(tab);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public Fragment getItem(int position) {
		TabInfo info = mTabs.get(position);
		Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(),
				info.args);
		Log.d(TAG, String.format("Getting item for position: %s, Fragment: %s",
				position, fragment));
		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.destroyItem(container, position, object);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Object tag = tab.getTag();
		for (int i = 0; i < mTabs.size(); i++) {
			if (mTabs.get(i) == tag) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
}
