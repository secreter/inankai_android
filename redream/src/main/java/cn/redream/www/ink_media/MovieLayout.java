package cn.redream.www.ink_media;

/**
 * Created by acer on 2016/3/25.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MovieLayout extends LinearLayout {
    public MovieLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MovieLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the tab_focused size of
        // our view. We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the tab_focused to change as this happens.
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        //高度和宽度一样

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        heightMeasureSpec= MeasureSpec.makeMeasureSpec((int) (childWidthSize*2.0), MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}