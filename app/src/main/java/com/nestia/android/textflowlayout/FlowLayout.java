package com.nestia.android.textflowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxinying on 17/1/10
 * 如果onmeasure没有测量完全，onlayout布局会得不到所有的子view
 * 思路:
 * 1.在onmeasure中测量所有子控件会占用的宽高，使用setMeasuredDimension（）方法设置
 * 2.在onlayout中用List<List<>>,记录每一行的view集合，用list<>记录每一行的高度，遍历view集合，布局每一个view的位置
 */

public class FlowLayout extends ViewGroup {
    private FlowLayoutListener flowLayoutListener;
    private int mMaxLineNum;

    public FlowLayout(Context context) {
        super(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 记录每一行的view 集合
     */
    private List<List<View>> mAllViews = new ArrayList<>();
    /**
     * 记录每一行的高度
     */
    private List<Integer> mLineHeight = new ArrayList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //在绘制布局时，onLayout方法会调用多次
        mAllViews.clear();
        mLineHeight.clear();
        //当前ViewGroup的宽度,
        int width = getWidth();
        int lineWidth = 0;
        int lineHeight = 0;
        List<View> lineViews = new ArrayList<View>();
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            // 需要换行的条件
            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                //记录lineHeight
                mLineHeight.add(lineHeight);
                //记录当前行的views
                mAllViews.add(lineViews);
                //重置我们的行宽和行高
                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                //重置View集合
                lineViews = new ArrayList<>();
            }
            //如果不换行,要重新计算 lineWidth/lineHeight
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }
        //为了最后一行在这里加
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);
        //设置子View的位置
        int left = getPaddingLeft();
        int top = getPaddingTop();
        //行数
        int lineNum = mAllViews.size();
        if (lineNum > mMaxLineNum && mMaxLineNum > 0) {
            lineNum = mMaxLineNum;
            if (changed) {
                if (flowLayoutListener != null) {
                    flowLayoutListener.lineChangeListener();
                }
            }
        }
        for (int i = 0; i < lineNum; i++) {
            //当前行的所有的View
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();
                //为子view进行布局
                child.layout(lc, tc, rc, bc);
                //重置起始值(同排的)
                left += child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            }
            //下一行 重置起点
            left = getPaddingLeft();
            top += lineHeight;
        }
    }

    /**
     * 与当前ViewGroup对应的LayoutParams
     *
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //在 EXACTLY 模式下
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);

        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //在 AT_MOST 模式下
        int width = 0;//记录此时viewgroup宽度
        int height = 0;//记录此时viewgroup的高度

        int showWith = 0;
        int showHeight = 0;

        //每一行的宽度和高度
        int lineWidth = 0;
        int lineHeight = 0;
        //得到内部元素的个数
        int cCount = getChildCount();
        //记录行数
        int lineNum = 0;
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            //测量子view
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            //得到ViewGroup的LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //子View占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            //子view占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {//需要新的一行
                //为了可以设置最大显示行，超过最大显示行，就不接着测量宽高。
                ++lineNum;
                //对比得到最大的宽度
                width = Math.max(width, lineWidth);
                //重置lineWidth
                lineWidth = childWidth;
                //记录行高
                height += lineHeight;
                lineHeight = childHeight;
                //如果设置了默认显示行数，记录默认显示行数的宽高
                if (lineNum == mMaxLineNum && mMaxLineNum > 0) {
                    showHeight = height;
                    showWith = width;
                }
            } else {//不需要换行，在同一行添加子view
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            //为什么最后一个控件会不记录？？？最后一个子view 因为不会换行，是在换行的同时加上 已占满的行
            if (i == cCount - 1) {
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension(
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : (showWith == 0 ? width : showWith) + getPaddingLeft() + getPaddingRight()
                , modeHeight == MeasureSpec.EXACTLY ? sizeHeight : (showHeight == 0 ? height : showHeight) + getPaddingTop() + getPaddingBottom());
    }

    public int getLineCount() {
        return mAllViews.size();
    }

    public void setMaxLine(int mMaxLineNum) {
        this.mMaxLineNum = mMaxLineNum;
    }

    public void setFlowLayoutListener(FlowLayoutListener flowLayoutListener) {
        this.flowLayoutListener = flowLayoutListener;
    }

    interface FlowLayoutListener {
        void lineChangeListener();
    }
}
