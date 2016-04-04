package com.example.chiakimayuzumi.qtlistviewtest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.example.chiakimayuzumi.qtlistviewtest.bean.CommunityMessage;
import com.example.chiakimayuzumi.qtlistviewtest.utils.CommonAdapter;
import com.example.chiakimayuzumi.qtlistviewtest.utils.RefreshListView;
import com.example.chiakimayuzumi.qtlistviewtest.utils.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiakimayuzumi on 16/4/2.
 */
public class CommunityRefreshActivity extends AppCompatActivity {

    private List<CommunityMessage> mDatas;
    private RefreshListView mListView;
    private CommonAdapter commonAdapter;
    private Context mcontext;

    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            commonAdapter.notifyDataSetChanged();//如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容

            boolean isRefresh = (Boolean) msg.obj;

            if (isRefresh){
                // 通知ListView应该完成刷新了
                mListView.completeRefresh();
            } else {
                // 通知ListView应该完成加载更多了
                mListView.completeLoadMore();
            }
        };
    };




    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initDatas();
        mcontext = getApplicationContext();



        mListView = new RefreshListView(mcontext);

        mListView.setOnRefreshListener(new RefreshListView.OnRefreshListener(){

            @Override
            public void onRefresh(RefreshListView listView) {
                // 刷新 - 从服务器加载数据
                sendRequest(true);
            }

            @Override
            public void onLoad(RefreshListView listView) {
                // 加载更多 - 从服务器加载数据
                sendRequest(false);
            }
        });

        commonAdapter = new CommonAdapter<CommunityMessage>(mcontext,mDatas,R.layout.item_single_listview) {
            @Override
            public void convert(final ViewHolder holder, final CommunityMessage communityMessage) {
                holder.setText(R.id.id_title, communityMessage.getTitle())
                        .setText(R.id.id_desc, communityMessage.getDesc())
                        .setText(R.id.id_time, communityMessage.getTime())
                        .setText(R.id.id_phone, communityMessage.getPhone());

                holder.setOnClickListener(R.id.id_title, new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                       Toast.makeText(CommunityRefreshActivity.this, communityMessage.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        mListView.setAdapter(commonAdapter);
        setContentView(mListView);


    }

    private void sendRequest(final boolean isRefresh){
        new Thread(new Runnable() {

            @Override
            public void run() {
                SystemClock.sleep(2000);
                CommunityMessage communityMessage = new CommunityMessage("这是从服务器刷新的帖子",
                        "blablablabla~~~~~", "2016-04-04", "10086");
                mDatas.add(isRefresh?0:mDatas.size(), communityMessage);//根据Refresh状态判断应该把刷新数据放在List的什么位置

                Message msg = mHandler.obtainMessage();
                msg.obj = isRefresh;
                mHandler.sendMessage(msg);
            }
        }).start();
    }


    private void initDatas()
    {
        mDatas = new ArrayList<CommunityMessage>();

        CommunityMessage communityMessage = new CommunityMessage("青苔社区帖子 1",
                "blablablabla~~~~~", "2014-12-12", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 2", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 3", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 4", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 5", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 6", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 7", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 8", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);
        communityMessage = new CommunityMessage("青苔社区帖子 9", "blablablabla~~~~~",
                "2016-04-04", "10086");
        mDatas.add(communityMessage);

    }



}
