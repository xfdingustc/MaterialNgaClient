package io.xfdingustc.mdngaclient.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import gov.anzong.androidnga.R;
import sp.phone.bean.ThreadPageInfo;
import sp.phone.bean.TopicListInfo;
import sp.phone.interfaces.NextJsonTopicListLoader;
import sp.phone.utils.ActivityUtil;
import sp.phone.utils.PhoneConfiguration;
import sp.phone.utils.StringUtil;
import sp.phone.utils.ThemeManager;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshAttacher;

/**
 * Created by whaley on 2017/6/5.
 */

public class TopicListRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TopicListRvAdapter.class.getSimpleName();

    private final static int _FONT_RED = 1, _FONT_BLUE = 2, _FONT_GREEN = 4,
        _FONT_ORANGE = 8, _FONT_SILVER = 16, _FONT_B = 32, _FONT_I = 64,
        _FONT_U = 128;
    protected Context context;
    protected int count = 0;

    private TopicListInfo topicListInfo = null;
    private int selected = -1;

    final private List<TopicListInfo> infoList;

    private final NextJsonTopicListLoader loader;
    Set<Integer> tidSet;
    private Toast toast = null;
    private boolean isEndOfList = false;
    private boolean isPrompted = false;
    private int table;
    private boolean isLoading = false;

    public TopicListRvAdapter(Context context, NextJsonTopicListLoader loader) {
        this.context = context;
        this.infoList = new ArrayList<>();
        this.loader = loader;

        tidSet = new HashSet<>();
    }

    public void clear() {
        count = 0;
        infoList.clear();
        tidSet.clear();
        isPrompted = false;
        setSelected(-1);
    }

    public void jsonfinishLoad(TopicListInfo result) {
        Logger.t(TAG).d("json finished");
        isLoading = false;

        if (result.get__SEARCHNORESULT()) {
            if (toast != null) {
                toast.setText("结果已搜索完毕");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            } else {
                toast = Toast.makeText(this.context, "结果已搜索完毕", Toast.LENGTH_SHORT);
                toast.show();
            }
            isLoading = false;
        }
        ActivityUtil.getInstance().dismiss();
        if (count != 0) {
            List<ThreadPageInfo> threadList = new ArrayList<ThreadPageInfo>();
            for (int i = 0; i < result.getArticleEntryList().size(); i++) {
                ThreadPageInfo info = result.getArticleEntryList().get(i);
                if (info == null) {
                    continue;
                }
                int tid = info.getTid();
                if (!tidSet.contains(tid)) {
                    threadList.add(info);
                    tidSet.add(tid);
                }
            }
            result.set__T__ROWS(threadList.size());
            result.setArticleEntryList(threadList);
        } else {
            for (int i = 0; i < result.getArticleEntryList().size(); i++) {
                ThreadPageInfo info = result.getArticleEntryList().get(i);
                if (info == null) {
                    continue;
                }
                int tid = info.getTid();
                tidSet.add(tid);
            }

        }
        infoList.add(result);
        count += result.get__T__ROWS();
        if (count >= (result.get__ROWS())) {
            isEndOfList = true;
        } else {
            isEndOfList = false;
        }
        notifyDataSetChanged();
    }

    public void setSelected(int position) {
        this.selected = position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(context).inflate(R.layout.relative_topic_list, parent, false);
        return new TopicListViewHolder(convertView);
    }


    protected ThreadPageInfo getEntry(int position) {
        for (int i = 0; i < infoList.size(); i++) {
            if (position < infoList.get(i).get__T__ROWS()) {
                return infoList.get(i).getArticleEntryList().get(position);
            }
            position -= infoList.get(i).get__T__ROWS();
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ThreadPageInfo entry = getEntry(position);

        if (entry == null) {
            Logger.t(TAG).d("null entry");
            return;
        }

        ThemeManager theme = ThemeManager.getInstance();
        boolean night = false;
        int nightLinkColor = context.getResources().getColor(R.color.night_link_color);
        if (theme.getMode() == ThemeManager.MODE_NIGHT)
            night = true;
        TopicListViewHolder holder = (TopicListViewHolder) viewHolder;

        holder.author.setText(entry.getAuthor());
        if (night)
            holder.author.setTextColor(nightLinkColor);

        String lastPoster = entry.getLastposter_org();
        if (StringUtil.isEmpty(lastPoster))
            lastPoster = entry.getLastposter();
        holder.lastReply.setText(lastPoster);
        holder.num.setText("" + entry.getReplies());
        if (night) {
            holder.lastReply.setTextColor(nightLinkColor);
            holder.num.setTextColor(nightLinkColor);
        }
        handleTitleView(holder.title, entry);
    }

    private void handleTitleView(TextView view, ThreadPageInfo entry) {
        ThemeManager theme = ThemeManager.getInstance();

        float size = PhoneConfiguration.getInstance().getTextSize();
        view.setTextColor(context.getResources().getColor(theme.getForegroundColor()));
        view.setTextSize(size);
        String titile = entry.getContent();
        int type = entry.getType();
        String needadd = "";
        if ((type & 1024) == 1024) {
            needadd += " [锁定]";
        }
        if ((type & 8192) == 8192) {
            needadd += " +";
        }
        int titlelength;
        if (StringUtil.isEmpty(titile)) {
            titile = entry.getSubject();
            titile = StringUtil.unEscapeHtml(titile);
            titlelength = titile.length();
            titile += needadd;

        } else {
            titile = StringUtil.removeBrTag(StringUtil
                .unEscapeHtml(titile));
            titlelength = titile.length();
            titile += needadd;
        }
        ForegroundColorSpan greenSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.title_green));
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.title_blue));
        ForegroundColorSpan redSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.title_red));
        ForegroundColorSpan lockredSpan = new ForegroundColorSpan(Color.RED);
        ForegroundColorSpan orangeSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.title_orange));
        ForegroundColorSpan picorangeSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.title_orange));
        ForegroundColorSpan sliverSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.silver));

        SpannableStringBuilder builder = new SpannableStringBuilder(titile);
        int totallength = titile.length();
        if ((type & 8192) == 8192 && (type & 1024) == 1024 && totallength >= 6) {//均有
            builder.setSpan(picorangeSpan, totallength - 1, totallength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(lockredSpan, totallength - 6, totallength - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ((type & 8192) == 8192 && totallength > 0) {//只有+
            builder.setSpan(picorangeSpan, totallength - 1, totallength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if ((type & 1024) == 1024 && totallength >= 4) {//只有锁定
            builder.setSpan(lockredSpan, totallength - 4, totallength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (!StringUtil.isEmpty(entry.getTopicMisc())) {
            final String misc = entry.getTopicMisc();
            if (misc.indexOf("~") >= 0) {
                if (misc.equals("~1~~") || misc.equals("~~~1")) {
                    builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    String miscarray[] = misc.toLowerCase(Locale.US).split("~");
                    for (int i = 0; i < miscarray.length; i++) {
                        if (miscarray[i].equals("green")) {
                            builder.setSpan(greenSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("blue")) {
                            builder.setSpan(blueSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("red")) {
                            builder.setSpan(redSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("orange")) {
                            builder.setSpan(orangeSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("sliver")) {
                            builder.setSpan(sliverSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (miscarray[i].equals("b") && miscarray[i].equals("i")) {
                            builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("b")) {
                            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (miscarray[i].equals("i")) {
                            builder.setSpan(new StyleSpan(Typeface.ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        if (miscarray[i].equals("u")) {
                            builder.setSpan(new UnderlineSpan(), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }

                    }
                }
            } else {
                byte b[] = Base64.decode(misc, Base64.DEFAULT);
                if (b != null) {
                    if (b.length == 5) {
                        String miscstring = toBinary(b);
                        String miscstringstart = miscstring.substring(0, 8);
                        BigInteger src1 = new BigInteger(miscstringstart, 2);//转换为BigInteger类型
                        int d1 = src1.intValue();
                        if (d1 == 1) {
                            String miscstringend = miscstring.substring(8, miscstring.length());
                            BigInteger src2 = new BigInteger(miscstringend, 2);//转换为BigInteger类型
                            int d2 = src2.intValue();
                            if ((d2 & _FONT_GREEN) == _FONT_GREEN) {
                                builder.setSpan(greenSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_BLUE) == _FONT_BLUE) {
                                builder.setSpan(blueSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_RED) == _FONT_RED) {
                                builder.setSpan(redSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_ORANGE) == _FONT_ORANGE) {
                                builder.setSpan(orangeSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_SILVER) == _FONT_SILVER) {
                                builder.setSpan(sliverSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            if ((d2 & _FONT_B) == _FONT_B && (d2 & _FONT_I) == _FONT_I) {
                                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_I) == _FONT_I) {
                                builder.setSpan(new StyleSpan(Typeface.ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if ((d2 & _FONT_B) == _FONT_B) {
                                builder.setSpan(new StyleSpan(Typeface.BOLD), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            if ((d2 & _FONT_U) == _FONT_U) {
                                builder.setSpan(new UnderlineSpan(), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                        }
                    }
                }
            }
        } else {
            if (!StringUtil.isEmpty(entry.getTitlefont())) {
                final String font = entry.getTitlefont();
                if (font.indexOf("~") >= 0) {
                    if (font.equals("~1~~") || font.equals("~~~1")) {
                        builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        String miscarray[] = font.toLowerCase(Locale.US).split("~");
                        for (int i = 0; i < miscarray.length; i++) {
                            if (miscarray[i].equals("green")) {
                                builder.setSpan(greenSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("blue")) {
                                builder.setSpan(blueSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("red")) {
                                builder.setSpan(redSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("orange")) {
                                builder.setSpan(orangeSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("sliver")) {
                                builder.setSpan(sliverSpan, 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            if (miscarray[i].equals("b") && miscarray[i].equals("i")) {
                                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("b")) {
                                builder.setSpan(new StyleSpan(Typeface.BOLD), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            } else if (miscarray[i].equals("i")) {
                                builder.setSpan(new StyleSpan(Typeface.ITALIC), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                            if (miscarray[i].equals("u")) {
                                builder.setSpan(new UnderlineSpan(), 0, titlelength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }

                        }
                    }
                }
            }
        }
        view.setText(builder);
    }

    private String toBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public int getNextPage() {
        return infoList.size() + 1;
    }

    public boolean getIsEnd() {
        return isEndOfList;
    }

    public class TopicListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.num)
        TextView num;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.author)
        TextView author;

        @BindView(R.id.last_reply)
        TextView lastReply;

        public TopicListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
