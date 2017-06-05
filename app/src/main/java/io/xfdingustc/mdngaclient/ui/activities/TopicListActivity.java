package io.xfdingustc.mdngaclient.ui.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.OnClick;
import gov.anzong.androidnga.R;
import gov.anzong.androidnga.activity.SwipeBackAppCompatActivity;
import sp.phone.adapter.AppendableTopicAdapter;
import sp.phone.adapter.TopicListAdapter;
import sp.phone.bean.BoardHolder;
import sp.phone.bean.ThreadData;
import sp.phone.bean.ThreadPageInfo;
import sp.phone.bean.TopicListInfo;
import sp.phone.fragment.ArticleContainerFragment;
import sp.phone.fragment.TopicListContainer;
import sp.phone.interfaces.EnterJsonArticle;
import sp.phone.interfaces.NextJsonTopicListLoader;
import sp.phone.interfaces.OnChildFragmentRemovedListener;
import sp.phone.interfaces.OnThreadPageLoadFinishedListener;
import sp.phone.interfaces.OnTopListLoadFinishedListener;
import sp.phone.interfaces.PagerOwnner;
import sp.phone.interfaces.PullToRefreshAttacherOnwer;
import sp.phone.task.CheckReplyNotificationTask;
import sp.phone.task.DeleteBookmarkTask;
import sp.phone.task.JsonTopicListLoadTask;
import sp.phone.utils.ActivityUtil;
import sp.phone.utils.HttpUtil;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.ReflectionUtil;
import sp.phone.utils.StringUtil;
import sp.phone.utils.ThemeManager;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

/**
 * 帖子列表
 */
public class TopicListActivity extends SwipeBackAppCompatActivity implements OnTopListLoadFinishedListener, OnItemClickListener,
    OnThreadPageLoadFinishedListener, PagerOwnner, PullToRefreshAttacherOnwer, OnItemLongClickListener, NextJsonTopicListLoader {
    private String TAG = TopicListActivity.class.getSimpleName();
    int fid;
    private AppendableTopicAdapter adapter;
    String strs[] = {"全部", "精华", "推荐"};
    ArrayAdapter<String> categoryAdapter;
    int flags = 7;
    int toDeleteTid = 0;
    TopicListInfo result = null;

    String guidtmp;
    int authorid;
    int searchpost;
    int favor;
    int content;
    private TopicListInfo mTopicListInfo;
    String key;
    //	String table;
    String fidgroup;
    String author;
//    boolean fromreplyactivity = false;

    private CheckReplyNotificationTask asynTask;
    private PullToRefreshAttacher mPullToRefreshAttacher;
    private OnItemClickListener onItemClickNewActivity = null;
    int category = 0;

    @BindView(R.id.topic_list)
    ListView listView;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @OnClick(R.id.fab)
    public void onFabClicked() {
        refresh();
    }

    public static void launch(Activity activity, int fid) {
        Intent intent = new Intent(activity, TopicListActivity.class);
        intent.putExtra("tab", "1");
        intent.putExtra("fid", fid);
        activity.startActivity(intent);
    }

    private int getUrlParameter(String url, String paraName) {
        if (StringUtil.isEmpty(url)) {
            return 0;
        }
        final String pattern = paraName + "=";
        int start = url.indexOf(pattern);
        if (start == -1)
            return 0;
        start += pattern.length();
        int end = url.indexOf("&", start);
        if (end == -1)
            end = url.length();
        String value = url.substring(start, end);
        int ret = 0;
        try {
            ret = Integer.parseInt(value);
        } catch (Exception e) {
            Log.e(TAG, "invalid url:" + url);
        }

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topiclist);

        String url = getIntent().getDataString();
        if (url != null) {
            authorid = getUrlParameter(url, "authorid");
            searchpost = getUrlParameter(url, "searchpost");
            favor = getUrlParameter(url, "favor");
            key = StringUtil.getStringBetween(url, 0, "key=", "&").result;
            author = StringUtil.getStringBetween(url, 0, "author=", "&").result;
//			table = StringUtil.getStringBetween(url, 0, "table=", "&").result;
            fidgroup = StringUtil.getStringBetween(url, 0, "fidgroup=", "&").result;
            content = getUrlParameter(url, "content");
        } else {
            if (null != getIntent().getExtras()) {
                authorid = getIntent().getExtras().getInt("authorid", 0);
                content = getIntent().getExtras().getInt("content", 0);
                searchpost = getIntent().getExtras().getInt("searchpost", 0);
                favor = getIntent().getExtras().getInt("favor", 0);
                key = getIntent().getExtras().getString("key");
                author = getIntent().getExtras().getString("author");
                if (!StringUtil.isEmpty(author))
                    if (author.indexOf("&searchpost=1") > 0) {
                        author = author.replace("&searchpost=1", "");
                        searchpost = 1;
                    }
//				table = getIntent().getExtras().getString("table");
                fidgroup = getIntent().getExtras().getString("fidgroup");
            }
        }


        initViews();

        int fid = getIntent().getIntExtra("fid", 0);
        if (fid != 0) {
            String boardName = BoardHolder.boardNameMap.get(fid);
            if (null != boardName) {
                strs[0] = boardName;
            }
        }
    }

    private void initViews() {
        adapter = new AppendableTopicAdapter(this, null, this);
        listView.setAdapter(adapter);
        try {
            OnItemClickListener listener = (OnItemClickListener) this;
            listView.setOnItemClickListener(listener);
        } catch (ClassCastException e) {
            Log.e(TAG, "father activity should implenent OnItemClickListener");
        }

        refresh();


        fid = 0;
        authorid = 0;


    }

    private void refresh() {
        JsonTopicListLoadTask task = new JsonTopicListLoadTask(this, this);
        task.execute(getUrl(1, true, true));
    }

    public String getUrl(int page, boolean isend, boolean restart) {
        String jsonUri = HttpUtil.Server + "/thread.php?";
        if (0 != authorid)
            jsonUri += "authorid=" + authorid + "&";
        if (searchpost != 0)
            jsonUri += "searchpost=" + searchpost + "&";
        if (favor != 0)
            jsonUri += "favor=" + favor + "&";
        if (content != 0)
            jsonUri += "content=" + content + "&";

        if (!StringUtil.isEmpty(author)) {
            try {
                if (author.endsWith("&searchpost=1")) {
                    jsonUri += "author="
                        + URLEncoder.encode(
                        author.substring(0, author.length() - 13),
                        "GBK") + "&searchpost=1&";
                } else {
                    jsonUri += "author="
                        + URLEncoder.encode(author, "GBK") + "&";
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            if (0 != fid)
                jsonUri += "fid=" + fid + "&";
            if (!StringUtil.isEmpty(key)) {
                jsonUri += "key=" + StringUtil.encodeUrl(key, "UTF-8") + "&";
            }
            if (!StringUtil.isEmpty(fidgroup)) {
                jsonUri += "fidgroup=" + fidgroup + "&";
            }
        }
        jsonUri += "page=" + page + "&lite=js&noprefix";
        switch (category) {
            case 2:
                jsonUri += "&recommend=1&order_by=postdatedesc&admin=1";
                break;
            case 1:
                jsonUri += "&recommend=1&order_by=postdatedesc&user=1";
                break;
            case 0:
            default:
        }

        return jsonUri;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {


        if (asynTask != null) {
            asynTask.cancel(true);
            asynTask = null;
        }
        long now = System.currentTimeMillis();
        PhoneConfiguration config = PhoneConfiguration.getInstance();
        if (now - config.lastMessageCheck > 30 * 1000 && config.notification) {// 30秒才爽啊艹
            Log.d(TAG, "start to check Reply Notification");
            asynTask = new CheckReplyNotificationTask(this);
            asynTask.execute(config.getCookie());
        }
        super.onResume();
    }

    @Override
    public void jsonfinishLoad(TopicListInfo result) {
        if (result == null)
            return;

        mTopicListInfo = result;
        if (result.get__SEARCHNORESULT()) {
            Toast.makeText(this, "结果已搜索完毕", Toast.LENGTH_SHORT).show();
            return;
        }
        int lines = 35;
        if (authorid != 0)
            lines = 20;
        int pageCount = result.get__ROWS() / lines;
        if (pageCount * lines < result.get__ROWS())
            pageCount++;

        if (searchpost != 0)// can not get exact row counts
        {
            int page = result.get__ROWS();
            pageCount = page;
            if (result.get__T__ROWS() == lines)
                pageCount++;
        }

        adapter.clear();
        adapter.jsonfinishLoad(result);
        listView.setAdapter(adapter);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null == onItemClickNewActivity) {
            onItemClickNewActivity = new EnterJsonArticle(this, false);
        }
        onItemClickNewActivity.onItemClick(parent, view, position, id);
    }

    @Override
    public void finishLoad(ThreadData data) {
        /*
         * int exactCount = 1 + data.getThreadInfo().getReplies()/20;
		 * if(father.getmTabsAdapter().getCount() != exactCount &&this.authorid
		 * == 0){ father.getmTabsAdapter().setCount(exactCount); }
		 * father.setTitle
		 * (StringUtil.unEscapeHtml(data.getThreadInfo().getSubject()));
		 */

        Fragment articleContainer = getSupportFragmentManager()
            .findFragmentById(R.id.item_detail_container);

        OnThreadPageLoadFinishedListener listener = null;
        try {
            listener = (OnThreadPageLoadFinishedListener) articleContainer;
            if (listener != null) {
                listener.finishLoad(data);
                getSupportActionBar().setTitle(
                    StringUtil.unEscapeHtml(data.getThreadInfo()
                        .getSubject()));
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "detailContainer should implements OnThreadPageLoadFinishedListener");
        }
    }

    @Override
    public int getCurrentPage() {
        PagerOwnner child = null;
        try {
            Fragment articleContainer = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
            child = (PagerOwnner) articleContainer;
            if (null == child)
                return 0;
            return child.getCurrentPage();
        } catch (ClassCastException e) {
            Log.e(TAG, "fragment in R.id.item_detail_container does not implements interface " + PagerOwnner.class.getName());
            return 0;
        }
    }

    @Override
    public void setCurrentItem(int index) {
        PagerOwnner child = null;
        try {
            Fragment articleContainer = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
            child = (PagerOwnner) articleContainer;
            child.setCurrentItem(index);
        } catch (ClassCastException e) {
            Log.e(TAG, "fragment in R.id.item_detail_container does not implements interface " + PagerOwnner.class.getName());
            return;
        }
    }


    @Override
    public PullToRefreshAttacher getAttacher() {
        return mPullToRefreshAttacher;
    }

    public ThreadPageInfo getEntry(int position) {
        if (result != null)
            return result.getArticleEntryList().get(position);
        return null;
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, int position, long id) {
        Object a = parent.getAdapter();
        AppendableTopicAdapter adapter = null;
        if (a instanceof AppendableTopicAdapter) {
            adapter = (AppendableTopicAdapter) a;
        } else if (a instanceof HeaderViewListAdapter) {
            HeaderViewListAdapter ha = (HeaderViewListAdapter) a;
            adapter = (AppendableTopicAdapter) ha.getWrappedAdapter();
            position -= ha.getHeadersCount();
        }
        final int positiona = position;
        final String deladd = adapter.gettidarray(positiona);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DeleteBookmarkTask task = new DeleteBookmarkTask(
                            TopicListActivity.this, parent, positiona);
                        task.execute(deladd);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Do nothing
                        break;
                }
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.delete_favo_confirm_text))
            .setPositiveButton(R.string.confirm, dialogClickListener)
            .setNegativeButton(R.string.cancle, dialogClickListener);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(new AlertDialog.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                dialog.dismiss();
                if (PhoneConfiguration.getInstance().fullscreen) {
                    ActivityUtil.getInstance().setFullScreen(view);
                }
            }

        });
        return true;
    }


    @Override
    public void loadNextPage(OnTopListLoadFinishedListener callback) {
        JsonTopicListLoadTask task = new JsonTopicListLoadTask(this, callback);
        task.execute(getUrl(adapter.getNextPage(), adapter.getIsEnd(), false));
    }
}
