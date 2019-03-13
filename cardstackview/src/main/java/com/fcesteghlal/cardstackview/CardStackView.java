package com.fcesteghlal.cardstackview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListAdapter;

@SuppressWarnings("unused")
public class CardStackView extends ViewGroup {

    private final static int INITIAL_OFFSET_LEFT = 0;
    private final static int INITIAL_OFFSET_TOP = 0;
    private final static int NO_VIEW = -1;
    private final static boolean ENABLE_OVERDRAW_IMPROVEMENT = false;
    private final static int ANIMATION_DURATION_IN_MS = 200;
    private final static int INITIAL_OFFSET_IN_PX = 0;
    private final static int FIRST_VIEW = 0;
    private final static float INITIAL_ALPHA_FOR_LATEST_ITEM = 0.8f;
    private final static float INITIAL_STEP_ALPHA_ITEMS = 0.20f;
    private final static boolean INITIAL_EXIST_ALPHA = true;
    private final static float MAXIMUM_OFFSET_TOP_BOTTOM_FACTOR = 0.75f;
    private final static float MAXIMUM_OFFSET_LEFT_RIGHT_FACTOR = 0.4f;
    private final static int MAXIMUM_ITEMS_ON_SCREEN = 5;
    private final static int ITEMS_TOP_MARGIN_DP = 8;
    private final static int ITEMS_LEFT_RIGHT_MARGIN_DP = 8;
    private int animationDuration = ANIMATION_DURATION_IN_MS;
    private int itemsMarginTop = dp2px(ITEMS_TOP_MARGIN_DP);
    private int itemsMarginLeftRight = dp2px(ITEMS_LEFT_RIGHT_MARGIN_DP);
    private int maximumViewsOnScreen = MAXIMUM_ITEMS_ON_SCREEN;
    private View[] viewsBuffer;
    private ListAdapter adapter;
    private StackViewTouchListener touchController;
    private int offsetTopBottom = INITIAL_OFFSET_IN_PX;
    private int offsetLeftRight = INITIAL_OFFSET_IN_PX;
    private int viewIndex = NO_VIEW;
    private int initialViewHeight = -1;
    private int maximumOffsetTopBottom;
    private int maximumOffsetLeftRight;
    private boolean existAlpha = true;
    private boolean performingSwipe = false;
    private boolean expandedVertically = false;
    private boolean expandable = false;
    private float stepAlpha = INITIAL_STEP_ALPHA_ITEMS;
    private SwipeEventListener swipeEventListener;
    private View emptyView;
    private float minimumAlpha = INITIAL_ALPHA_FOR_LATEST_ITEM;

    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            attachChildViews();
            if (emptyView != null) {
                if (adapter.getCount() == 0) {
                    CardStackView.this.setVisibility(GONE);
                    emptyView.setVisibility(VISIBLE);
                } else {
                    CardStackView.this.setVisibility(VISIBLE);
                    emptyView.setVisibility(GONE);
                }
            }
        }
    };

    public boolean isExpandable() {
        return expandable;
    }

    public void setMaximumViewsOnScreen(int maxViews) {
        if (maxViews <= 0) {
            throw new IllegalArgumentException("The value of maxViews cannot be less than 1.");
        }
        maximumViewsOnScreen = maxViews;
    }

    public void setAnimationDuration(int duration) {
        animationDuration = duration;
    }

    public void setEmptyView(@NonNull View emptyView) {
        this.emptyView = emptyView;
        this.emptyView.setVisibility(GONE);
    }

    public void setAdapter(@NonNull ListAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(dataSetObserver);
        viewsBuffer = new View[maximumViewsOnScreen];
        attachChildViews();
    }

    public boolean isExpanded() {
        return expandedVertically;
    }

    public void setSwipeEventListener(SwipeEventListener swipeEventListener) {
        this.swipeEventListener = swipeEventListener;
    }

    public void swipeItem(View item) {
        swipeItem(item, swipeEventListener);
    }

    public void swipeItem(View item, SwipeEventListener listener) {
        viewIndex = findViewIndexByPosition(item.getLeft(), item.getTop());
        if (viewIndex != NO_VIEW) {
            performHorizontalSwipe(listener);
        }
    }

    public CardStackView(Context context) {
        super(context);
        initializeView(null);
    }

    public CardStackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(attrs);
    }

    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(attrs);
    }

    private void initializeView(AttributeSet attrs) {
        readAttributesFromXmlLayout(attrs);
        viewsBuffer = new View[maximumViewsOnScreen];
        touchController = new StackViewTouchListener(this);
    }

    private void readAttributesFromXmlLayout(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = getContext()
                    .getTheme().obtainStyledAttributes(attrs, R.styleable.StackView, 0, 0);
            maximumViewsOnScreen = attributes
                    .getInteger(R.styleable.StackView_maxViews, MAXIMUM_ITEMS_ON_SCREEN);
            itemsMarginTop = attributes
                    .getDimensionPixelSize(R.styleable.StackView_viewsMarginTop, dp2px(ITEMS_TOP_MARGIN_DP));
            itemsMarginLeftRight = attributes
                    .getDimensionPixelSize(R.styleable.StackView_viewsMarginLeftRight, dp2px(ITEMS_LEFT_RIGHT_MARGIN_DP));
            animationDuration = attributes
                    .getInt(R.styleable.StackView_viewAnimDuration, ANIMATION_DURATION_IN_MS);
            expandable = attributes
                    .getBoolean(R.styleable.StackView_expandable, expandable);
            minimumAlpha = attributes
                    .getFloat(R.styleable.StackView_firstAlpha, INITIAL_ALPHA_FOR_LATEST_ITEM);

            stepAlpha = attributes
                    .getFloat(R.styleable.StackView_stepAlpha, INITIAL_STEP_ALPHA_ITEMS);

            existAlpha = attributes
                    .getBoolean(R.styleable.StackView_viewAlpha, INITIAL_EXIST_ALPHA);

            attributes.recycle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean touchEventCaptured = touchController.onTouchEvent(event);
        if (!touchEventCaptured) {
            touchEventCaptured = super.onTouchEvent(event);
        }
        return touchEventCaptured;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result;
        int childIndex = getChildIndex(child);
        result = super.drawChild(canvas, child, drawingTime);

        return result;
    }

    private int getChildIndex(@NonNull View child) {
        int result = 0;
        for (int index = 0; index < getChildCount(); index++) {
            final View item = getChildAt(index);
            if ((item.getTop() == child.getTop()) && (item.getLeft() == child.getLeft())) {
                result = index;
                break;
            }
        }
        return result;
    }

    private Rect calculateClippingViewRect(View currentView, int viewIndex) {
        int nextViewIndex = viewIndex + 1;
        final View nextView = getChildAt(nextViewIndex);
        int left = currentView.getLeft();
        int top = currentView.getTop();
        int right = currentView.getRight();
        int bottom = top + (nextView.getTop() - top);
        return new Rect(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = calculateWrapContentHeight();
        if (getChildCount() > 0) {
            if (initialViewHeight == -1) {
                initialViewHeight = viewHeight;
            } else if (viewHeight < initialViewHeight) {
                viewHeight = initialViewHeight;
            }
        } else {
            initialViewHeight = -1;
        }
        setMeasuredDimension(viewWidth, viewHeight);
        configureChildViewsMeasureSpecs(widthMeasureSpec);
    }

    private int calculateWrapContentHeight() {
        int maxChildHeight = 0;
        for (int index = FIRST_VIEW; index < getChildCount(); index++) {
            final View childView = getChildAt(index);
            measureChildView(childView);
            if (childView.getVisibility() != View.GONE) {
                maxChildHeight = Math.max(maxChildHeight, getChildAt(index).getMeasuredHeight());
            }
        }
        int itemsElevationPadding = itemsMarginTop * getViewsCount();
        int measuredHeight = maxChildHeight + getPaddingTop() + getPaddingBottom() + itemsElevationPadding;
        int measuredOffset = offsetTopBottom * getViewsCount();
        return measuredHeight + measuredOffset;
    }

    private void configureChildViewsMeasureSpecs(int widthMeasureSpec) {
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        final int parentWidth = MeasureSpec.getSize(widthMeasureSpec)
                - getPaddingLeft()
                - getPaddingRight();
        int viewWidth;
        int viewHeight;
        int minimumViewHeight = 0;
        int maximumViewWidth = 0;
        for (int index = getViewsCount(); index >= FIRST_VIEW; index--) {
            final View childView = getChildAt(index);
            measureChildView(childView);
            viewWidth = calculateViewWidth(parentWidth, index);
            viewHeight = childView.getMeasuredHeight();
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);
            childView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            if (minimumViewHeight == 0) {
                minimumViewHeight = viewHeight;
            }
            if (maximumViewWidth == 0) {
                maximumViewWidth = viewWidth;
            }
            minimumViewHeight = Math.min(minimumViewHeight, viewHeight);
            maximumViewWidth = Math.max(maximumViewWidth, viewWidth);
        }
        maximumOffsetTopBottom = (int) (minimumViewHeight * MAXIMUM_OFFSET_TOP_BOTTOM_FACTOR);
        maximumOffsetLeftRight = (int) (maximumViewWidth * MAXIMUM_OFFSET_LEFT_RIGHT_FACTOR);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft;
        int childTop;
        int childRight;
        int childBottom;
        for (int index = getViewsCount(); index >= FIRST_VIEW; index--) {
            if (isVisibleView(index)) {
                final View childView = getChildAt(index);
                childLeft = calculateViewLeft(left, right, childView.getMeasuredWidth(), index);
                childRight = childLeft + childView.getMeasuredWidth();
                childTop = calculateViewTop(bottom, childView.getMeasuredHeight(), index);
                childBottom = childTop + childView.getMeasuredHeight();
                childView.layout(childLeft, childTop, childRight, childBottom);
            }
        }
        if (getChildCount() > 1) {
            float viewAlpha = minimumAlpha;
            float step;
            if ((stepAlpha < 1 && stepAlpha > 0) && (viewAlpha > 0 && viewAlpha < 1) &&
                    !(stepAlpha * (getChildCount() - 1) - viewAlpha < 0)
                    ) {
                viewAlpha = minimumAlpha;
                step = stepAlpha;
            } else {
                viewAlpha = INITIAL_ALPHA_FOR_LATEST_ITEM;
                step = .15f;
            }
            if (!isExpanded()) {
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    if (i < getChildCount() - 1) {
                        getChildAt(i).setAlpha(viewAlpha - Math.abs(i - (getChildCount() - 1)) * step);
                    }
                }
            }
        }
    }

    private int calculateViewLeft(int parentLeft, int parentRight, int childWith, int zIndex) {
        int center = parentLeft + ((parentRight - parentLeft) / 2);
        int result = center - (childWith / 2);
        if (viewIndex == zIndex) {
            result += offsetLeftRight;
        }
        return result;
    }

    private int calculateViewTop(int parentBottom, int viewHeight, int zIndex) {
        int viewTop = calculateTheoreticalViewTop(parentBottom, viewHeight, zIndex);
        if (offsetLeftRight > INITIAL_OFFSET_LEFT && zIndex < viewIndex) {
            int nextViewIndex = zIndex + 1;
            final View nextView = getChildAt(nextViewIndex);
            int nextViewTop = calculateTheoreticalViewTop(parentBottom,
                    nextView.getMeasuredHeight(),
                    nextViewIndex);
            float offsetFactor = calculateCurrentLeftRightOffsetFactor();
            viewTop += (nextViewTop - viewTop) * offsetFactor;
        }
        return viewTop;
    }

    private int calculateTheoreticalViewTop(int parentBottom, int viewHeight, int zIndex) {
        int topMinimumOffset = itemsMarginTop;
        int viewTop = parentBottom - getPaddingBottom() - viewHeight - (topMinimumOffset
                * (getViewsCount() - zIndex));
        if (offsetTopBottom > INITIAL_OFFSET_TOP) {
            viewTop -= calculateOffsetTop(zIndex);
        }
        return viewTop;
    }

    private int getViewsCount() {
        return (getChildCount() - 1);
    }

    private boolean isVisibleView(int zIndex) {
        final View view = getChildAt(zIndex);
        return isVisibleView(view);
    }

    private boolean isVisibleView(View view) {
        return view.getAlpha() > 0f && view.getVisibility() != View.GONE;
    }

    private int calculateOffsetTop(int zIndex) {
        int result = 0;
        if (isNotTheForegroundView(zIndex)) {
            result = offsetTopBottom * (getChildCount() - (zIndex + 1));
        }
        return result;
    }

    private boolean isNotTheForegroundView(int zIndex) {
        return zIndex < getViewsCount();
    }

    private float calculateCurrentTopBottomOffsetFactor() {
        float offsetLimit = (maximumOffsetTopBottom * MAXIMUM_OFFSET_TOP_BOTTOM_FACTOR);
        float offsetFactor = ((float) offsetTopBottom / offsetLimit);
        if (offsetFactor > 1) {
            offsetFactor = 1f;
        }
        return offsetFactor;
    }

    private float calculateCurrentLeftRightOffsetFactor() {
        float offsetLimit = (maximumOffsetLeftRight * MAXIMUM_OFFSET_LEFT_RIGHT_FACTOR);
        float offsetFactor = ((float) offsetLeftRight / offsetLimit);
        if (offsetFactor > 1) {
            offsetFactor = 1f;
        }
        return offsetFactor;
    }

    private int calculateViewWidth(float parentWidth, int zIndex) {
        float viewWidth = calculateTheoreticalViewWidth(parentWidth, zIndex);
        if (zIndex < viewIndex) {
            int nextViewIndex = zIndex + 1;
            float nextViewWidth = calculateTheoreticalViewWidth(parentWidth, nextViewIndex);
            float offsetFactor = calculateCurrentLeftRightOffsetFactor();
            viewWidth += (nextViewWidth - viewWidth) * offsetFactor;

        }
        return (int) viewWidth;
    }

    private float calculateTheoreticalViewWidth(float parentWidth, int zIndex) {
        float widthMinimumOffset = itemsMarginLeftRight;
        float maximumWidthOffset = (widthMinimumOffset * MAXIMUM_OFFSET_TOP_BOTTOM_FACTOR);
        float widthMinimumOffsetFactor = getVerticalOffsetFactor();
        float widthOffset = widthMinimumOffset * widthMinimumOffsetFactor;
        if (widthOffset < maximumWidthOffset) {
            widthMinimumOffset -= widthOffset;
        } else {
            widthMinimumOffset -= maximumWidthOffset;
        }
        return (parentWidth - (widthMinimumOffset * (getViewsCount() - zIndex)));
    }

    private float getVerticalOffsetFactor() {
        float result = 0f;
        if (Math.abs(offsetTopBottom) > INITIAL_OFFSET_TOP) {
            result = (float) offsetTopBottom / (float) maximumOffsetTopBottom;
        }
        return result;
    }

    private void measureChildView(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    private void attachChildViews() {
        removeAllViews();
        if (adapter.getCount() == 0) {
            offsetTopBottom = INITIAL_OFFSET_TOP;
            offsetLeftRight = INITIAL_OFFSET_LEFT;
            viewIndex = NO_VIEW;
            expandedVertically = false;
            viewsBuffer = new View[maximumViewsOnScreen];
        }
        for (int position = FIRST_VIEW; position < adapter.getCount(); position++) {
            if (position < maximumViewsOnScreen) {
                viewsBuffer[position] = adapter.getView(position, viewsBuffer[position], this);
                addViewInLayout(viewsBuffer[position], FIRST_VIEW,
                        viewsBuffer[position].getLayoutParams());
            } else {
                break;
            }
        }
        requestLayout();
    }

    void collapseVerticalOffset() {
        if (offsetTopBottom > INITIAL_OFFSET_TOP) {
            ValueAnimator animator = ValueAnimator.ofInt(offsetTopBottom, INITIAL_OFFSET_IN_PX);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(animationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    offsetTopBottom = (int) animation.getAnimatedValue();
                    requestLayout();
                }
            });
            animator.start();
            expandedVertically = false;
        }
    }

    void collapseHorizontalOffset() {
        if (offsetLeftRight > INITIAL_OFFSET_LEFT && viewIndex != NO_VIEW) {
            ValueAnimator animator = ValueAnimator.ofInt(offsetLeftRight, INITIAL_OFFSET_IN_PX);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(animationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    offsetLeftRight = (int) animation.getAnimatedValue();
                    requestLayout();
                }
            });
            animator.start();
        }
    }

    void setOffsetLeftRight(int viewIndex, int offset) {
        if (!performingSwipe && viewIndex != NO_VIEW) {
            if (offset >= 0) {
                this.viewIndex = viewIndex;
                this.offsetLeftRight = offset;
                requestLayout();
            }
        }
    }

    void setOffsetTopBottom(int offset) {
        if (!performingSwipe && !expandedVertically) {
            if (offset >= INITIAL_OFFSET_IN_PX) {
                if (offset > maximumOffsetTopBottom) {
                    offsetTopBottom = maximumOffsetTopBottom;
                } else {
                    offsetTopBottom = offset;
                }
                requestLayout();
            }
        }
    }

    void performHorizontalSwipe() {
        performHorizontalSwipe(swipeEventListener);
    }

    void performHorizontalSwipe(final SwipeEventListener listener) {
        if (!performingSwipe && viewIndex != NO_VIEW && getChildCount() > 0) {
            performingSwipe = true;
            ValueAnimator animator = ValueAnimator.ofInt(offsetLeftRight, getMeasuredWidth());
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(animationDuration);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    final View item = getChildAt(viewIndex);
                    if (listener != null) {
                        listener.onSwipe(CardStackView.this, item);
                    }
                    offsetLeftRight = INITIAL_OFFSET_LEFT;
                    viewIndex = NO_VIEW;
                    performingSwipe = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    offsetLeftRight = (int) animation.getAnimatedValue();
                    requestLayout();
                }
            });
            animator.start();
        }
    }

    public void performVerticalSwipe() {
        if (isExpandable() && !performingSwipe && getChildCount() > 0) {
            performingSwipe = true;
            int initialValue = offsetTopBottom;
            int endValue = maximumOffsetTopBottom;
            if (expandedVertically) {
                initialValue = maximumOffsetTopBottom;
                endValue = 0;
            }
            ValueAnimator animator = ValueAnimator.ofInt(initialValue, endValue);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(animationDuration);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!expandedVertically) {
                        offsetTopBottom = maximumOffsetTopBottom;
                    } else {
                        offsetTopBottom = INITIAL_OFFSET_TOP;
                    }
                    expandedVertically = !expandedVertically;
                    performingSwipe = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    offsetTopBottom = (int) animation.getAnimatedValue();
                    Log.d("Swipe", String.format("%d", offsetTopBottom));
                    requestLayout();
                }
            });
            animator.start();
        }
    }

    int findViewIndexByPosition(int x, int y) {
        int result = NO_VIEW;
        if (!isExpanded()) {
            result = getViewsCount();
        } else {
            View childView;
            int viewLeft;
            int viewRight;
            int viewTop;
            int viewBottom;
            for (int index = getViewsCount(); index >= 0; index--) {
                childView = getChildAt(index);
                if (isVisibleView(childView)) {
                    viewLeft = childView.getLeft();
                    viewRight = childView.getRight();
                    viewTop = childView.getTop();
                    viewBottom = childView.getBottom();
                    if ((x >= viewLeft && x <= viewRight) &&
                            (y >= viewTop && y <= viewBottom)) {
                        result = index;
                        break;
                    }
                }
            }
        }
        return result;
    }

    void performReleaseTouch() {
        if (!performingSwipe) {
            if (offsetLeftRight > INITIAL_OFFSET_LEFT && viewIndex != NO_VIEW) {
                if (offsetLeftRight < maximumOffsetLeftRight) {
                    collapseHorizontalOffset();
                } else {
                    performHorizontalSwipe(swipeEventListener);
                }
            } else if (offsetTopBottom > INITIAL_OFFSET_TOP) {
                collapseVerticalOffset();
            }
        }
    }

    private int dp2px(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getContext().getResources().getDisplayMetrics());
    }

    public interface SwipeEventListener {
        void onSwipe(CardStackView parent, View item);
    }
}
