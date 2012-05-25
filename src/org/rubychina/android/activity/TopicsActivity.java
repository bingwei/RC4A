/*Copyright (C) 2012 Longerian (http://www.longerian.me)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package org.rubychina.android.activity;

import java.util.ArrayList;
import java.util.List;

import org.rubychina.android.R;
import org.rubychina.android.RCApplication;
import org.rubychina.android.RCService;
import org.rubychina.android.RCService.LocalBinder;
import org.rubychina.android.api.request.TopicsRequest;
import org.rubychina.android.api.response.TopicsResponse;
import org.rubychina.android.fragment.TopicListFragment;
import org.rubychina.android.type.Node;
import org.rubychina.android.type.Topic;
import org.rubychina.android.type.User;
import org.rubychina.android.util.JsonUtil;
import org.rubychina.android.widget.TopicAdapter;

import yek.api.ApiCallback;
import yek.api.ApiException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;


public class TopicsActivity extends SherlockFragmentActivity {

	private static final String TAG = "TopicsActivity";
	
	private RCService mService;
	private boolean isBound = false; 
	
	private TopicListFragment topicList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		Intent intent = new Intent(this, RCService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            isBound = true;
            initialize();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (isBound) {
			unbindService(mConnection);
			isBound = false;
		}
	}
    
	private void initialize() {
		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			topicList = TopicListFragment.newInstance(Node.MOCK_ACTIVE_NODE);
	        getSupportFragmentManager().beginTransaction().add(android.R.id.content, topicList).commit();
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.id.action_bar_nodes, 0, R.string.actionbar_nodes)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, R.id.action_bar_compose, 1, R.string.actionbar_compose)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, R.id.action_bar_refresh, 2, R.string.actionbar_refresh)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, R.id.action_bar_setting, 2, R.string.actionbar_setting)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent();
		switch(item.getItemId()) {
		case R.id.action_bar_nodes:
			i.setClass(getApplicationContext(), NodesActivity.class);
			startActivityForResult(i, NodesActivity.PICK_NODE);
			break;
        case R.id.action_bar_refresh:
        	topicList.startTopicsRequest(topicList.getNode());
        	break;
        case R.id.action_bar_compose:
        	onCompose();
        	break;
        case R.id.action_bar_setting:
			i.setClass(getApplicationContext(), RCPreferenceActivity.class);
			startActivity(i);
			break;
		default: 
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NodesActivity.PICK_NODE) {
			if(resultCode == RESULT_OK) {
				Node n = data.getParcelableExtra(NodesActivity.PICKED_NODE);
				topicList.setNode(n);
				topicList.startTopicsRequest(n);
			}
		}
	}

	private void onCompose() {
		Intent i = new Intent();
		if(((RCApplication) getApplication()).isLogin()) {
    		i.setClass(getApplicationContext(), TopicEditingActivity.class);
    	} else {
    		i.setClass(getApplicationContext(), RCPreferenceActivity.class);
    		Toast.makeText(getApplicationContext(), R.string.hint_no_token, Toast.LENGTH_SHORT).show();
    	}
    	startActivity(i);
	}
	
	public void requestUserAvatar(User u, ImageView v, int size) {
		if(isBound) {
			mService.requestUserAvatar(u, v, size);
		}
	}
	
	public List<Topic> fetchTopics() {
		return mService.fetchTopics();
	}
	
	public void clearTopics() {
		mService.clearTopics();
	}
	
	public void insertTopics(List<Topic> topics) {
		mService.insertTopics(topics);
	}
	
}
