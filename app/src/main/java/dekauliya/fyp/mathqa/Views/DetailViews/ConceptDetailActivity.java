package dekauliya.fyp.mathqa.Views.DetailViews;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import dekauliya.fyp.mathqa.Models.Concept;
import dekauliya.fyp.mathqa.R;
import dekauliya.fyp.mathqa.Utils.FabUtils;
import dekauliya.fyp.mathqa.Views.BaseListActivity;
import dekauliya.fyp.mathqa.Views.ListViews.KeypointConceptListFragment_;
import dekauliya.fyp.mathqa.Views.ListViews.KeypointFormulaListFragment_;
import dekauliya.fyp.mathqa.Views.ListViews.QuestionConceptListFragment_;
import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.SelectableAdapter;

/**
 * Activity for viewing a concept, which includes important concept keypoints, formulas and
 * available questions under a concept grouped by subconcept.
 */
@EActivity
public class ConceptDetailActivity extends BaseListActivity implements
        FastScroller.OnScrollStateChangeListener, OnDetailFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Extra("conceptExtra")
    Concept conceptExtra;

    @ViewById(R.id.concept_detail_container)
    CoordinatorLayout container;

    @Bean
    FabUtils fabUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concept_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.concept_detail_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fabUtils.setUpFab();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_concept, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings: return true;
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFastScrollerStateChange(boolean scrolling) {

    }

    @Override
    public void onFragmentChange(SwipeRefreshLayout swipeRefreshLayout, RecyclerView recyclerView,
                                 @SelectableAdapter.Mode int mode) {

    }

    @Override
    public void onFragmentInteraction() {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        static final int PAGE_COUNT = 3;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position){
                case 0:
                    return KeypointConceptListFragment_.builder().conceptArg(conceptExtra).build();
                case 1:
                    return KeypointFormulaListFragment_.builder().conceptArg(conceptExtra).build();
                case 2:
                    return QuestionConceptListFragment_.builder().conceptArg(conceptExtra).build();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Keypoints";
                case 1:
                    return "Formula";
                case 2:
                    return "Questions";
            }
            return null;
        }


    }
}
