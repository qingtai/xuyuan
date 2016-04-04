package com.example.chiakimayuzumi.qtlistviewtest.utils;

/**
 * Created by chiakimayuzumi on 16/4/2.
 */
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chiakimayuzumi.qtlistviewtest.R;

public class RefreshListView extends ListView implements OnScrollListener{

    /* 监听接口 */
    private OnRefreshListener onRefreshListener;
    public interface OnRefreshListener {
        void onRefresh(RefreshListView listView);
        void onLoad(RefreshListView listView);
    }

    /* 头部View高度 */
    private View mHeaderView;
    private int mHeaderHeight;

    /* 尾部View高度 */
    private View mFooterView;
    private int mFooterHeight;

    /* HeaderView中的控件 */
    private ImageView mIvArrow; // 箭头
    private ProgressBar mPbRotate; //进度条
    private TextView mTvStatus; //̬状态
    private TextView mTvTime; //时间

    /* 向上动画、向下动画 */
    private RotateAnimation upRotateAnimation;
    private RotateAnimation downRotateAnimation;

    /* 当前状态̬ */
    private RefreshState currState = RefreshState.PULL;

    /* 状态枚举 */
    public enum RefreshState {
        LOADING, //正在加载
        PULL, // 下拉刷新
        RELEASE, // 正在刷新
    }

    /** 构造方法：context 上下文，attrs 属性集，defStyle 样式 */
    public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }
    /** 构造方法：context 上下文，attrs 属性集 */
    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    /** 构造方法：context 上下文 */
    public RefreshListView(Context context) {
        super(context);
        initView();
    }

    private void initView(){
        // 初始化头部
        initHeaderView();
        System.out.println("addHeaderView");

        // 初始化动画
        initAnim();

        // 设置滚动事件
        setOnScrollListener(this);

        // 初始化尾部
        initFooterView();
    }

    /* 初始化头部 */
    private void initHeaderView() {
        mHeaderView = View.inflate(getContext(), R.layout.layout_headerview,null);
        mHeaderView.measure(0, 0);//手动测量，因为是用的inflate
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);

        // 初始化headerView 的成员
        mIvArrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
        mPbRotate = (ProgressBar) mHeaderView.findViewById(R.id.pb_rotate);
        mTvStatus = (TextView) mHeaderView.findViewById(R.id.tv_status);
        mTvTime = (TextView) mHeaderView.findViewById(R.id.tv_time);

        addHeaderView(mHeaderView);
    }

    /* 初始化尾部 */
    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.layout_footerview,
                null);
        mFooterView.measure(0, 0);
        mFooterHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0, -mFooterHeight, 0, 0);

        addFooterView(mFooterView);
    }

    /* 初始化动画 */
    private void initAnim() {
        upRotateAnimation = new RotateAnimation(0f, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        upRotateAnimation.setDuration(300);
        //动画结束，保持状态
        upRotateAnimation.setFillAfter(true);

        downRotateAnimation = new RotateAnimation(-180f, -360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        downRotateAnimation.setDuration(300);
        //动画结束，保持状态
        downRotateAnimation.setFillAfter(true);
    }

    int startY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();//getY 返回触摸点相对view的距离
                break;
            case MotionEvent.ACTION_MOVE:

                int dy = (int) (ev.getY() - startY);
                int newPaddingTop = -mHeaderHeight + dy / 2; // 减速效果

                // 正在刷新时，不允许改变继续往下拉
                if (currState == RefreshState.LOADING){
                    break;
                }

                mHeaderView.setPadding(0, newPaddingTop, 0, 0);

                // 状态的切换
                if (newPaddingTop >= 0 && currState == RefreshState.PULL) {
                    // 进入松开刷新
                    currState = RefreshState.RELEASE;

                    // 根据状态更新UI
                    refreshHeaderView();
                } else if (newPaddingTop < 0 && currState == RefreshState.RELEASE) {
                    // 进入下拉刷新
                    currState = RefreshState.PULL;

                    // 根据状态更新UI
                    refreshHeaderView();
                }

                // 判断事件是否要交给 ListView 处理
                if (dy > 0 && getFirstVisiblePosition() == 0) {
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
                int currPaddingTop = mHeaderView.getPaddingTop();

                if ( currPaddingTop <= 0 && currState == RefreshState.PULL){
                    mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
                } else if (currPaddingTop > 0 && currState == RefreshState.RELEASE){
                    currState = RefreshState.LOADING;
                    refreshHeaderView();

                    // 通知外部，现在正在刷新了
                    if (onRefreshListener != null){
                        onRefreshListener.onRefresh(this);
                    }
                }

                break;
        }

        // 必须依然将事件还给 ListView,处理默认的滚动
        return super.onTouchEvent(ev);
    }

    /* 根据状态，更新HeaderView的UI */
    private void refreshHeaderView() {
        switch (currState) {
            case PULL:
                mIvArrow.startAnimation(downRotateAnimation);
                mTvStatus.setText("下拉刷新");
                break;
            case RELEASE:
                mIvArrow.startAnimation(upRotateAnimation);
                mTvStatus.setText("松开刷新");
                break;
            case LOADING:
                // 必须要清除动画，否则无法设置隐藏
                mIvArrow.clearAnimation();
                mHeaderView.setPadding(0, 0, 0, 0);
                mIvArrow.setVisibility(View.INVISIBLE);
                mPbRotate.setVisibility(View.VISIBLE);
                mTvStatus.setText("正在刷新...");
                break;
        }
    }

    /* 完成下拉刷新 */
    public void completeRefresh(){
        mTvStatus.setText("下拉刷新");
        mHeaderView.setPadding(0, -mHeaderHeight, 0, 0);
        mPbRotate.setVisibility(View.INVISIBLE);
        mIvArrow.setVisibility(View.VISIBLE);

        currState = RefreshState.PULL;
        mTvTime.setText("最后刷新" +getCurrTime());
    }

    /* 完成加载更多 */
    public void completeLoadMore(){
        mFooterView.setPadding(0, -mFooterHeight, 0, 0);
        setSelection(Integer.MAX_VALUE);
    }

    /* 获取当前时间 */
    private String getCurrTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /* 设置刷新回调（含：下拉刷新、加载更多） */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 当手指松开、并且最后一个可见view为List最后一条数据，才显示footer
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && getLastVisiblePosition() == getCount() - 1){
            mFooterView.setPadding(0, 0, 0, 0);
            setSelection(Integer.MAX_VALUE);

            // 通知外部加载更多
            if (onRefreshListener != null){
                onRefreshListener.onLoad(this);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }
}
