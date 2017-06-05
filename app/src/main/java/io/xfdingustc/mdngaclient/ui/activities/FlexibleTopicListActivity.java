package io.xfdingustc.mdngaclient.ui.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
public class FlexibleTopicListActivity extends SwipeBackAppCompatActivity implements OnTopListLoadFinishedListener, OnItemClickListener,
    OnThreadPageLoadFinishedListener, PagerOwnner, OnChildFragmentRemovedListener, PullToRefreshAttacherOnwer, OnItemLongClickListener,
    ArticleContainerFragment.OnArticleContainerFragmentListener, TopicListContainer.OnTopicListContainerListener, NextJsonTopicListLoader {
    private String TAG = FlexibleTopicListActivity.class.getSimpleName();
    int fid;
    private AppendableTopicAdapter adapter;
    boolean dualScreen = true;
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
    boolean fromreplyactivity = false;

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
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
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
        if (authorid > 0 || searchpost > 0 || favor > 0
            || !StringUtil.isEmpty(key) || !StringUtil.isEmpty(author)
            || !StringUtil.isEmpty(fidgroup)) {//!StringUtil.isEmpty(table) ||
            fromreplyactivity = true;
        }


        onFragmentCreated();

        int fid = getIntent().getIntExtra("fid", 0);
        if (fid != 0) {
            String boardName = BoardHolder.boardNameMap.get(fid);
            if (null != boardName) {
                strs[0] = boardName;
            }
        }
        int favor = getIntent().getIntExtra("favor", 0);
        String key = getIntent().getStringExtra("key");
        String fidgroup = getIntent().getStringExtra("fidgroup");
        int authorid = getIntent().getIntExtra("authorid", 0);

        if (favor == 0 && authorid == 0 && StringUtil.isEmpty(key)
            && StringUtil.isEmpty(author)) {
            setNavigation();
        } else {
            flags = ThemeManager.ACTION_BAR_FLAG;
        }
        if (favor != 0) {
            getSupportActionBar().setTitle(R.string.bookmark_title);
        }
        if (!StringUtil.isEmpty(key)) {
            flags = ThemeManager.ACTION_BAR_FLAG;
            if (content == 1) {
                if (!StringUtil.isEmpty(fidgroup)) {
                    final String title = "搜索全站(包含正文):" + key;
                    getSupportActionBar().setTitle(title);
                } else {
                    final String title = "搜索(包含正文):" + key;
                    getSupportActionBar().setTitle(title);
                }
            } else {
                if (!StringUtil.isEmpty(fidgroup)) {
                    final String title = "搜索全站:" + key;
                    getSupportActionBar().setTitle(title);
                } else {
                    final String title = "搜索:" + key;
                    getSupportActionBar().setTitle(title);
                }
            }
        } else {
            if (!StringUtil.isEmpty(author)) {
                flags = ThemeManager.ACTION_BAR_FLAG;
                if (searchpost > 0) {
                    final String title = "搜索" + author + "的回复";
                    getSupportActionBar().setTitle(title);
                } else {
                    final String title = "搜索" + author + "的主题";
                    getSupportActionBar().setTitle(title);
                }
            }
        }

    }

    private void onFragmentCreated() {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment f1 = getSupportFragmentManager().findFragmentById(
            R.id.item_list);
        Fragment f2 = getSupportFragmentManager().findFragmentById(
            R.id.item_detail_container);
        f1.onPrepareOptionsMenu(menu);
        if (f2 != null && dualScreen)
            f2.onPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @TargetApi(11)
    private void setNavigation() {

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strs);
        OnNavigationListener callback = new OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition,
                                                    long itemId) {
                TopicListContainer f1 = (TopicListContainer) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list);
                if (f1 != null) {
                    f1.onCategoryChanged(itemPosition);
                }
                return true;
            }

        };
//        actionBar.setListNavigationCallbacks(categoryAdapter, callback);

    }

    @TargetApi(14)
    void setNfcCallBack() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        CreateNdefMessageCallback callback = new CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                FragmentManager fm = getSupportFragmentManager();
                TopicListContainer f1 = (TopicListContainer) fm
                    .findFragmentById(R.id.item_list);
                final String url = f1.getNfcUrl();
                NdefMessage msg = new NdefMessage(
                    new NdefRecord[]{NdefRecord.createUri(url)});
                return msg;
            }

        };
        if (adapter != null) {
            adapter.setNdefPushMessageCallback(callback, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ReflectionUtil.actionBar_setDisplayOption(this, flags);
        return false;// super.onCreateOptionsMenu(menu);
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

            Toast.makeText(this, "结果已搜索完毕",
                Toast.LENGTH_SHORT).show();
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
        if (!dualScreen) {// 非平板
            if (null == onItemClickNewActivity) {
                onItemClickNewActivity = new EnterJsonArticle(this, fromreplyactivity);
            }
            onItemClickNewActivity.onItemClick(parent, view, position, id);
        } else {
            String guid = (String) parent.getItemAtPosition(position);
            if (StringUtil.isEmpty(guid))
                return;

            guid = guid.trim();
            guidtmp = guid;

            int pid = StringUtil.getUrlParameter(guid, "pid");
            int tid = StringUtil.getUrlParameter(guid, "tid");
            int authorid = StringUtil.getUrlParameter(guid, "authorid");
            ArticleContainerFragment f = ArticleContainerFragment.create(tid,
                pid, authorid);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.item_detail_container, f);
            Fragment f1 = fm.findFragmentById(R.id.item_list);
            f1.setHasOptionsMenu(false);
            f.setHasOptionsMenu(true);
            ft.commit();

            ListView listview = (ListView) parent;
            Object a = parent.getAdapter();
            TopicListAdapter adapter = null;
            if (a instanceof TopicListAdapter) {
                adapter = (TopicListAdapter) a;
            } else if (a instanceof HeaderViewListAdapter) {
                HeaderViewListAdapter ha = (HeaderViewListAdapter) a;
                adapter = (TopicListAdapter) ha.getWrappedAdapter();
                position -= ha.getHeadersCount();
            }
            adapter.setSelected(position);
            listview.setItemChecked(position, true);
        }
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
    public void OnChildFragmentRemoved(int id) {
        if (id == R.id.item_detail_container) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment f1 = fm.findFragmentById(R.id.item_list);
            f1.setHasOptionsMenu(true);
            getSupportActionBar().setTitle("主题列表");
            guidtmp = "";
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
                            FlexibleTopicListActivity.this, parent, positiona);
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
    public void onModeChanged() {
        Fragment f1 = getSupportFragmentManager().findFragmentById(R.id.item_list);
        if (f1 != null) {
            ((TopicListContainer) f1).changedMode();
        }
    }

    @Override
    public void onAnotherModeChanged() {
        Fragment f2 = getSupportFragmentManager().findFragmentById(R.id.item_detail_container);
        if (f2 != null) {
            ((ArticleContainerFragment) f2).changemode();
        } else {
            FrameLayout v = (FrameLayout) findViewById(R.id.item_detail_container);
            if (v != null) {
                if (ThemeManager.getInstance().getMode() == ThemeManager.MODE_NIGHT) {
                    v.setBackgroundResource(R.color.night_bg_color);
                } else {
                    v.setBackgroundResource(R.color.shit1);
                }
            }
        }
    }

    @Override
    public void loadNextPage(OnTopListLoadFinishedListener callback) {
        JsonTopicListLoadTask task = new JsonTopicListLoadTask(this, callback);
//        refresh_saying();

        task.execute(getUrl(adapter.getNextPage(), adapter.getIsEnd(), false));
    }
}
