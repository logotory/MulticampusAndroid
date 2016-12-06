package com.example.ch2_meterial;


import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OneFragment extends Fragment {

    RecyclerView recyclerView;


    public OneFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Fragmemt 의 layout 초기화
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_one, container, false);

        //항목 구성 가상 데이터 준비..
        List<String> list = new ArrayList<>();
        for(int i=0; i < 20; i++){
            list.add("item=" +i);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MyAdapter(list));
        recyclerView.addItemDecoration(new MyDecorator());

        return recyclerView;
    }

    //findbViewById의 성능이슈 .. find한 Viw를 메모리에 누적시키는 역할
    //adapter에서 이 Holder객체를 메모리에 누적만 시킨다면 한번만 find
    class MyViewHolder extends RecyclerView.ViewHolder {
        //한 항목에 find 대상이 되는 view를 나열..
        public TextView titleView;

        //holder를 사용하는 곳은 ..adapter이다. adapter에서
        //항목 layout 계층 root를 매게변수로 전달..
        public MyViewHolder(View root) {
            super(root);

            titleView = (TextView) root.findViewById(android.R.id.text1); //android에 있는 텍스트 넣었다.
        }
    }

    //항목을 구성하기 위한 adapter..
    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        List<String> list; //항목 데이터 집합객체 .. activity 가 전달할 거다

        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        //각 항목을 위한 layoutchrl초기화..view 홀더 준비
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false); //문자열 하나 나오는 안드로이드 라이브러리 쓴거다.

            return new MyViewHolder(view);
        }

        //항목 하나를 구성하기 위한 함수..
        //여기에 개발자 로직이 들어간다.
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            String txt = list.get(position);
            holder.titleView.setText(txt);
        }
    }

    class MyDecorator extends RecyclerView.ItemDecoration {
        //각 항목이 화면에 모두 찍힌후 그 위에 추가 꾸미기 작업

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);

            //view의 사이즈 획득
            int width = parent.getWidth();
            int height = parent.getHeight();

            //이미지 사이즈 획득
            //가운데 위치시키려고
            Drawable dr = getActivity().getResources().getDrawable(R.drawable.kbo);
            int drWidth = dr.getIntrinsicWidth();
            int drHeight = dr.getIntrinsicHeight();

            //이미지가 그려질 left, top 좌표값 획득..
            int left = width / 2  - drWidth/2;
            int top = height/2 - drHeight/2;

            //이미지를 화면에 그리고..
            c.drawBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.kbo),
                    left, top, null);
        }


        //항목 하나하나를 꾸미기 위해서 호출
        //항목 하나를 구성하기 위한 사각형 정보가 매게 변수로 전달..
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            //항목의 index 값을 획득해서..
            int index = parent.getChildAdapterPosition(view)+1;

            if(index % 3 == 0){
                outRect.set(20, 20, 20, 20);
            }else {
                outRect.set(20, 20, 20, 0);
            }

            view.setBackgroundColor(0xFFECE9E9); //배경색 회색 철
            ViewCompat.setElevation(view, 5.0f); //음영처리. 하위 호환성 때문에 ViewCompat 사용
        }
    }
}
