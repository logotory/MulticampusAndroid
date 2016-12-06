package com.example.ch2_meterial;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


// tablayout 하위에 viewpager..
// viewpager 유저 조정시 tab button 조정은 tablayout이 자동처리
// tab button 유저 조정시 viewpager 조정은 개발자가
public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, View.OnClickListener{

    ViewPager viewPager;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    boolean isDrawerOpend;

    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fab;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //actionbar 에 들어갈 내용을 위리의 View인 toolbar에 적용
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        //toggle에 들어가는 문자열은 화면 출력과 상관없다,
        //accessibility 때문에..
        //서브 클래스 만드는것은 이벤트 처리 하겠다는 의미..
        //필수는 아니다.
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                isDrawerOpend = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                isDrawerOpend = false;
            }
        };


        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //왼쪽에 아이콘 이미지 나오게 하는데 필요

        //NavigationView의 메뉴 클릭 이벤트
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_drawer_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("tag", "navigation view clicked");
                return false;
            }
        });


        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        //tablayout과 viewpager연결
        //tab button을 직업 문자열, icon등으로 구성해도 되는데..
        //viewpager와 연결되어 있다면.. viewpager화면 갯수만큼..viewpager
        //화면 titile을 그대로 얻어 button으로 구성해 준다.
        tabLayout.setupWithViewPager(viewPager);
        //tabLayout.addOnTabSelectedListener(this);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coodinator);

        collapsingToolbarLayout.setTitle("AppBar Title");
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(coordinatorLayout, "I am SnackBar...", Snackbar.LENGTH_SHORT)
                .setAction("MyAction", new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Log.d("kkang", "Snackbar click...");
                    }
                }).show();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        toggle.syncState();
    }

    //menu event 함수 .. toggle의 이벤트가 menu이벤트를 타서
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //drawer가 open된 상태이다.. 유저가 back button 누르면?
    @Override
    public void onBackPressed() {
        if (isDrawerOpend)
            drawerLayout.closeDrawers();
        else
            super.onBackPressed();
    }

    //menu 구현을 위해 자동으로 호출되는 함수
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //menu를 xml로 작성.. 초기화 시켜야 한다..
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //ViewPager를 위한 Adapter
    //PagerAdapter, FragmentStatePagerAdapter 상속으로
    class MyPagerAdapter extends FragmentStatePagerAdapter {
        //항목 집합.. ViewPager의 항목은 화면한장.. Fragment로 표현
        List<Fragment> fragments = new ArrayList<>();

        //ViewPager title 문자열.. PagerTitleStrip, PagerTabStrip등에서
        //이 문자열로 title구성
        //우리의 경우는 tablayout에서..
        String titles[] = new String[]{"TAB1", "TAB2", "TAB3"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm); //상위 클래스에 FramentManter 전달.
            //우리는 각 항목을 위한 Fragmentn만 결정해 주면 상위클래스에서
            //FragmentTransaction를 이용해 Fragment 제어

            fragments.add(new OneFragment());
            fragments.add(new TwoFragment());
            fragments.add(new ThreeFragment());
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        //page의 title을 획득할 목적으로 자동 호출
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        //유저에 의해 tab button 눌린순간 viewpager 화면 조정
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
