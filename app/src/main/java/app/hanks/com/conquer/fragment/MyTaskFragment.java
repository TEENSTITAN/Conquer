/*
 * Created by Hanks
 * Copyright (c) 2015 Nashangban. All rights reserved
 *
 */
package app.hanks.com.conquer.fragment;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.List;

import app.hanks.com.conquer.R;
import app.hanks.com.conquer.adapter.MyZixiAdapter;
import app.hanks.com.conquer.bean.Task;
import app.hanks.com.conquer.config.Constants;
import app.hanks.com.conquer.util.CollectionUtils;
import app.hanks.com.conquer.util.TaskUtil;

/**
 * Created by Hanks on 2015/5/22.
 */
public class MyTaskFragment extends BaseFragment {

/*
    private RecyclerView        mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout  refreshLayout;
    private List<Tasks>          list = new ArrayList<>();
    private MyZixiAdapter mAdapter;
*/
    private RecyclerView mRecyclerView;
    private List<Task> list = new ArrayList<>();

    private RecyclerView.LayoutManager          mLayoutManager;
    private RecyclerView.Adapter                mAdapter;
    private RecyclerView.Adapter                mWrappedAdapter;
    private RecyclerViewSwipeManager            mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private RecyclerViewDragDropManager         mRecyclerViewDragDropManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_task, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recylerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager 拖拽排序的manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3_xxhdpi));

        // swipe manager 滑动item的manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();


        getMyTask();
    }


    /**
     * 设置RecyclerView
     */
    private void initRefreshLayout() {
//        refreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.refreshLayout);
//        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.theme_0));
//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getMyZixi();
//            }
//        });
    }


    /**
     * 获取我的任务list
     */
    private void getMyTask() {
        // 访问数据库在子线程中进行
        // 1.首先获取本地数据库
        List<Task> list = TaskUtil.getAfterZixi(context);
        if (CollectionUtils.isNotNull(list)) {
            setListData(list);
        }
        L.i("我的本地长度" + list.size());
        // 2.获取网络，可能是换手机了，或者是没有添加过，或者是当前时间以后没有
        if (list.size() <= 0) {
            TaskUtil.getNetAfterZixi(context, currentUser, Constants.MAIN_MYZIXI_LIMIT, new TaskUtil.GetZixiCallBack() {
                @Override
                public void onSuccess(List<Task> list) {
                    if (list != null) {
                        setListData(list);
                    }
                }
                @Override
                public void onError(int errorCode, String msg) {
                }
            });
        }
    }


    /**
     * 设置list数据
     */
    private void setListData(List<Task> newList) {
        list.clear();
        list.addAll(newList);
        L.d("任务个数:"+list.size());
//        mAdapter.notifyDataSetChanged();
//        refreshLayout.setRefreshing(false);


        //adapter
        mAdapter =  new MyZixiAdapter(context,list);
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping
        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);


        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
    }
    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
}