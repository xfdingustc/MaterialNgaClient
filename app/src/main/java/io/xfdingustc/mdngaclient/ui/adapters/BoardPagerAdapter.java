package io.xfdingustc.mdngaclient.ui.adapters;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import io.xfdingustc.mdngaclient.ui.fragments.BoardPagerFragment;
import sp.phone.interfaces.PageCategoryOwnner;

public class BoardPagerAdapter extends FragmentStatePagerAdapter {

    final private int widthPercentage;
    private PageCategoryOwnner pageCategoryOwnner;

    public BoardPagerAdapter(FragmentManager fm, PageCategoryOwnner pageCategoryOwnner, int width) {
        super(fm);
        this.pageCategoryOwnner = pageCategoryOwnner;
        this.widthPercentage = width;

    }

    @Override
    public Fragment getItem(int index) {
        return BoardPagerFragment.newInstance(index);
    }

    @Override
    public int getCount() {

        return pageCategoryOwnner.getCategoryCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        return pageCategoryOwnner.getCategoryName(position);
    }

    @Override
    public float getPageWidth(int position) {
        return widthPercentage / 100.0f;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        if (obj != null) {
            try {
                destroyItem(container, position, obj);
            } catch (Exception e) {

            }
            return super.instantiateItem(container, position);
        } else {
            return obj;
        }
    }
}
