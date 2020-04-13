package com.cyy.seekbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenyy
 * @date 2020/3/31
 */

public class CustomTextSeekBar extends View {
    private static final String TAG = CustomTextSeekBar.class.getSimpleName();
    /**
     * 刻度文本
     */
    private static final String[] TEXT_ARRAY = {"2s", "4s", "6s", "8s", "10s"};

    /**
     * 文本间距
     */
    private float offset;

    /**
     * 文本与进度条间距
     */
    private static final int MARGIN = 10;

    /**
     * 滑动按钮x方向的对应刻度
     */
    private List<Float> buttonDistance = new ArrayList<>();

    /**
     * 文本x方向的对应刻度
     */
    private List<Float> textDistance = new ArrayList<>();

    /**
     * 文本画笔
     */
    private Paint textPaint = new Paint();

    /**
     * 进度条画笔
     */
    private Paint progressPaint = new Paint();

    /**
     * 滑动按钮画笔
     */
    private Paint buttonPaint = new Paint();

    /**
     * 进度条
     */
    private RectF progressRect;

    /**
     * 滑动按钮
     */
    private RectF buttonRect;

    /**
     * 文本
     */
    private Rect mBounds = new Rect();

    /**
     * 进度条背景色
     */
    private int progressColor;

    /**
     * 进度条高度
     */
    private float progressHeight;

    /**
     * 进度条宽度
     */
    private float progressWidth;

    /**
     * 滑动按钮背景色
     */
    private int circleButtonColor;

    /**
     * 滑动按钮高度
     */
    private float buttonHeight;

    /**
     * 滑动按钮宽度
     */
    private float buttonWidth;

    /**
     * 滑动按钮半径
     */
    private float circleButtonRadius;

    /**
     * 文本颜色
     */
    private int textColor;

    /**
     * 文本大小
     */
    private float textSize;

    /**
     * 文本及滑动按钮距离左右的间距
     */
    private float padding;

    private boolean isMove;

    private float lastX;

    /**
     * x方向的偏移
     */
    private float distance;

    /**
     * 一次move的总偏移
     */
    private float totalOffset;

    /**
     * x方向的最小偏移
     */
    private float minDistance;

    /**
     * x方向的最大偏移
     */
    private float maxDistance;

    /**
     * 文本y方向的位置
     */
    private float height;

    /**
     * 是否允许自动定位
     */
    private boolean autoLocation;

    private Drawable progressDrawable;

    private Drawable buttonDrawable;

    private Bitmap progressBitmap;

    private Bitmap buttonBitmap;

    private Matrix matrix = new Matrix();

    /**
     * 自动定位到的刻度索引
     */
    private int index;

    private OnSeekBarChangeListener listener;

    private VelocityTracker velocityTracker;

    public CustomTextSeekBar(Context context) {
        this(context, null);
    }

    public CustomTextSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.CustomTextSeekBar);
        progressColor = attr.getColor(R.styleable.CustomTextSeekBar_progressColor, Color.BLUE);
        progressHeight = attr.getDimension(R.styleable.CustomTextSeekBar_progressHeight, 60);
        progressWidth = attr.getDimension(R.styleable.CustomTextSeekBar_progressWidth, 1000);
        progressDrawable = attr.getDrawable(R.styleable.CustomTextSeekBar_progressDrawable);
        circleButtonColor = attr.getColor(R.styleable.CustomTextSeekBar_buttonColor, Color.WHITE);
        buttonHeight = attr.getDimension(R.styleable.CustomTextSeekBar_buttonHeight, 60);
        buttonWidth = attr.getDimension(R.styleable.CustomTextSeekBar_buttonWidth, 60);
        circleButtonRadius = attr.getDimension(R.styleable.CustomTextSeekBar_circleButtonRadius, 30);
        buttonDrawable = attr.getDrawable(R.styleable.CustomTextSeekBar_buttonDrawable);
        textColor = attr.getColor(R.styleable.CustomTextSeekBar_textColor, Color.BLACK);
        textSize = attr.getDimension(R.styleable.CustomTextSeekBar_textSize, 30);
        padding = attr.getDimension(R.styleable.CustomTextSeekBar_padding, 0);
        autoLocation = attr.getBoolean(R.styleable.CustomTextSeekBar_autoLocation, true);
        attr.recycle();
        initView();
    }

    private void initView() {
        //初始化文本画笔
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(2);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);

        //初始化进度条画笔
        progressPaint.setColor(progressColor);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);

        //初始化滑动按钮画笔
        buttonPaint.setColor(circleButtonColor);
        buttonPaint.setAntiAlias(true);
        buttonPaint.setStyle(Paint.Style.FILL);

        progressRect = new RectF(0, 0, progressWidth, progressHeight);
        offset = (progressRect.width() - progressRect.height() - padding * 2) / (TEXT_ARRAY.length - 1);
        Log.i(TAG, "text offset: " + offset);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        textPaint.getTextBounds(TEXT_ARRAY[0], 0, TEXT_ARRAY[0].length(), mBounds);
        height = progressHeight + mBounds.height() + MARGIN;
        setMeasuredDimension((int) progressWidth + 1, (int) height + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (progressBitmap != null) {
            canvas.drawBitmap(progressBitmap, 0, 0, progressPaint);
        } else {
            canvas.drawRoundRect(progressRect, progressHeight / 2, progressHeight / 2, progressPaint);
        }

        for (int i = 0; i < TEXT_ARRAY.length; i++) {
            canvas.drawText(TEXT_ARRAY[i], textDistance.get(i), height, textPaint);
        }

        Log.i(TAG, "distance: " + distance);

        if (buttonBitmap != null) {
            canvas.drawBitmap(buttonBitmap, distance, 0, buttonPaint);
        } else {
            canvas.drawCircle(distance, progressHeight / 2, circleButtonRadius, buttonPaint);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();

        initDistance();
    }

    private void initDistance() {
        for (int i = 0; i < TEXT_ARRAY.length; i++) {
            textPaint.getTextBounds(TEXT_ARRAY[i], 0, TEXT_ARRAY[i].length(), mBounds);
            float x = offset * i + progressRect.height() / 2 - mBounds.width() / 2 + padding;
            textDistance.add(x);
            if (buttonBitmap != null) {
                buttonDistance.add(x + mBounds.width() / 2 - buttonWidth / 2);
            } else {
                buttonDistance.add(x + mBounds.width() / 2);
            }
        }
        minDistance = buttonDistance.get(0);
        maxDistance = buttonDistance.get(TEXT_ARRAY.length - 1);
        Log.i(TAG, "minDistance: " + minDistance + " maxDistance: " + maxDistance);
        distance = minDistance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                totalOffset = 0.0f;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "ACTION_MOVE");
                if (!isMove) {
                    lastX = x;
                }
                isMove = true;
                float offset = x - lastX;
                totalOffset = totalOffset + offset;
                Log.i(TAG, "offset: " + offset);
                distance = distance + offset;
                if (distance <= minDistance) {
                    distance = minDistance;
                } else if (distance >= maxDistance) {
                    distance = maxDistance;
                }

                if (distance <= maxDistance && distance >= minDistance) {
                    //animateButton(currentDistance, distance);
                    invalidate();
                }
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    isMove = false;
                }
                if (autoLocation) {
                    setProgress();
                } else {
                    index = (int) distance;
                    invalidate();
                }
                if (listener != null) {
                    listener.onProgressChanged(index);
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void setProgress() {
        velocityTracker.computeCurrentVelocity(1);
        float velocity = velocityTracker.getXVelocity();
        Log.i(TAG, "velocity: " + velocity);
        for (int i = 0; i < TEXT_ARRAY.length; i++) {
            if (distance < (buttonDistance.get(i) + offset / 2) && distance > (buttonDistance.get(i) - offset / 2)) {
                Log.i(TAG, "totalOffset: " + totalOffset);
                if (Math.abs(velocity) > 0.1 && Math.abs(totalOffset) < offset / 2) {
                    if (velocity > 0) {
                        index = i >= TEXT_ARRAY.length - 1 ? TEXT_ARRAY.length - 1 : i + 1;
                    } else {
                        index = i <= 0 ? 0 : i - 1;
                    }
                } else {
                    index = i;
                }
            }
        }
        distance = buttonDistance.get(index);
        Log.i(TAG, "index: " + index + " distance: " + distance);
        invalidate();
    }

    private void setup() {
        if (getWidth() == 0 && getHeight() == 0) {
            Log.i(TAG, "width == 0 and height == 0 !");
            return;
        }

        progressBitmap = getBitmap(progressDrawable);

        buttonBitmap = getBitmap(buttonDrawable);

        if (progressBitmap == null) {
            Log.i(TAG, "progressBitmap == null");
            invalidate();
            return;
        }

        BitmapShader progressBitmapShader = new BitmapShader(progressBitmap, Shader.TileMode.CLAMP, Shader
                .TileMode.CLAMP);

        float progressScale = Math.max(progressHeight / progressBitmap.getHeight(), progressWidth / progressBitmap.getWidth());
        float dx, dy;
        dx = (progressWidth - progressBitmap.getWidth() * progressScale) * 0.5f;
        dy = (progressHeight - progressBitmap.getHeight() * progressScale) * 0.5f;

        matrix.setScale(progressScale, progressScale);
        matrix.postTranslate(dx, dy);
        progressBitmapShader.setLocalMatrix(matrix);

        progressPaint.setShader(progressBitmapShader);

        if (buttonBitmap == null) {
            Log.i(TAG, "buttonBitmap == null");
            invalidate();
            return;
        }

        BitmapShader buttonBitmapShader = new BitmapShader(buttonBitmap, Shader.TileMode.CLAMP, Shader
                .TileMode.CLAMP);

        float buttonScale = Math.max(buttonHeight / buttonBitmap.getHeight(), buttonWidth / buttonBitmap.getWidth());
        float sx, sy;
        sx = (buttonWidth - buttonBitmap.getWidth() * buttonScale) * 0.5f;
        sy = (buttonHeight - buttonBitmap.getHeight() * buttonScale) * 0.5f;

        matrix.setScale(buttonScale, buttonScale);
        matrix.postTranslate(sx, sy);
        buttonBitmapShader.setLocalMatrix(matrix);

        buttonPaint.setShader(buttonBitmapShader);
    }

    private Bitmap getBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap
                        .createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap
                        .createBitmap((int) progressWidth, (int) progressHeight,
                                Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void animateButton(float src, float dist) {
        ValueAnimator animator = ValueAnimator.ofFloat(src, dist);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                distance = (float) animation.getAnimatedValue();
                Log.i(TAG, "distance: " + distance);
                invalidate();
            }
        });
        animator.start();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(int index);
    }
}
