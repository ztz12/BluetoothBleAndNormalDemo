package win.lioil.bluetooth.widget;import android.annotation.SuppressLint;import android.app.Activity;import android.content.Context;import android.graphics.Bitmap;import android.graphics.Bitmap.Config;import android.graphics.BitmapFactory;import android.graphics.Canvas;import android.graphics.Color;import android.graphics.Paint;import android.graphics.Path;import android.graphics.PorterDuff;import android.graphics.PorterDuffXfermode;import android.os.Environment;import android.util.AttributeSet;import android.view.MotionEvent;import android.view.View;import java.io.File;import java.io.FileOutputStream;import win.lioil.bluetooth.R;import win.lioil.bluetooth.util.AssistStatic;public class BrushView extends View {    float preX;    float preY;    private Path path;    public Paint paint = null;    public Paint mEPaint = null;    /**     * 定义一个内存中的图片，该图片将作为缓冲区     */    private Bitmap cacheBitmap = null;    /**     * 定义cacheBitmap上的Canvas对象     */    private Canvas cacheCanvas = null;    /**     * 橡皮擦图标     */    private Bitmap clear_bitmap;    /**     * 当前状态是否为橡皮擦     */    private boolean flag_earser = false;    /**     * 判断是否在触摸屏幕，当手指抬起不绘制橡皮擦图标     */    private boolean flag_up = true;    /**     * 判断是否发生变化     */    private boolean isViewChange = false;    public BrushView(Context context, AttributeSet attr) {        super(context, attr);        // 初始化橡皮擦图标        clear_bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.brushicon);        cacheCanvas = new Canvas();        cacheBitmap = null;        path = new Path();        flag_earser = false;        // 初始化画笔信息        if (flag_earser) {            setEarser();        } else {            setPaint(Color.RED, 5);        }    }    @SuppressLint("DrawAllocation")    @Override    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {        // TODO Auto-generated method stub        super.onMeasure(widthMeasureSpec, heightMeasureSpec);        // 创建一个与该View相同大小的缓存区        // 无法再构造函数中获取view的宽高        if (cacheBitmap == null || isViewChange) {            //移除标记            if (isViewChange == true) {                isViewChange = false;            }            cacheBitmap = Bitmap.createBitmap(MeasureSpec.getSize(widthMeasureSpec),                    MeasureSpec.getSize(heightMeasureSpec), Config.ARGB_8888);            // 设置cacheCanvas将会绘制到内存中的cacheBitmap上            cacheCanvas.setBitmap(cacheBitmap);        }    }    /**     * 清空画布     */    public void clear() {        // 创建一个和DrawView大小相同的透明画布        cacheBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Config.ARGB_8888);        cacheCanvas = new Canvas();        // 设置cacheCanvas将会绘制到内存中的cacheBitmap上        cacheCanvas.setBitmap(cacheBitmap);        // 刷新页面        invalidate();    }    /**     * webView区域变大，清空画布绘制,设置最新的webView绘制区域     */    public void clearAll(int width, int height) {        cacheBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);        cacheCanvas = new Canvas();        cacheCanvas.setBitmap(cacheBitmap);        invalidate();    }    /**     * 设置画笔     *     * @param color 设置画笔的颜色     * @param width 画笔宽度     */    public void setPaint(int color, int width) {        // 设置画笔的颜色        paint = new Paint(Paint.DITHER_FLAG);        paint.setColor(color);        // 设置画笔风格        paint.setStyle(Paint.Style.STROKE);        paint.setStrokeWidth(width);        // 反锯齿        paint.setAntiAlias(true);        paint.setDither(true);        paint.setStrokeJoin(Paint.Join.ROUND);        paint.setStrokeCap(Paint.Cap.ROUND);        flag_earser = false;    }    /**     * 设置橡皮擦     */    public void setEarser() {        // 设置橡皮擦画笔        Paint cPaint = new Paint();        cPaint.setAlpha(0);        cPaint.setColor(Color.TRANSPARENT);        cPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));        cPaint.setAntiAlias(true);        cPaint.setDither(true);        cPaint.setStyle(Paint.Style.STROKE);        cPaint.setStrokeJoin(Paint.Join.ROUND);        cPaint.setStrokeCap(Paint.Cap.ROUND);        cPaint.setStrokeWidth(60);        paint = cPaint;        clear_bitmap = Bitmap.createScaledBitmap(clear_bitmap, 100, 100, true);        flag_earser = true;    }    /**     * 设置页面发生变化     *     * @param viewChange     */    public void setViewChange(boolean viewChange) {        isViewChange = viewChange;    }    /**     * 根据手指移动进行绘图     */    @SuppressLint("ClickableViewAccessibility")    @Override    public boolean onTouchEvent(MotionEvent event) {        // 获取拖动事件的发生位置        float x = event.getX();        float y = event.getY();        switch (event.getAction()) {            case MotionEvent.ACTION_DOWN: // 手按下                flag_up = false;                path.moveTo(x, y);                preX = x;                preY = y;                break;            case MotionEvent.ACTION_MOVE: // 手移动                path.quadTo(preX, preY, (x + preX) / 2, (y + preY) / 2);                preX = x;                preY = y;                break;            case MotionEvent.ACTION_UP: // 手放开                flag_up = true;                cacheCanvas.drawPath(path, paint);                path.reset();                break;        }        invalidate();        // 返回true表明处理方法已经处理该事件        return true;    }    @SuppressLint("DrawAllocation")    @Override    protected void onDraw(Canvas canvas) {        Paint bmpPaint = new Paint();        // 将cacheBitmap绘制到该View组件上        canvas.drawBitmap(cacheBitmap, 0, 0, bmpPaint);        // 如果是在橡皮擦状态则画橡皮擦图标        if (flag_earser) {            if (!flag_up) {                canvas.drawBitmap(clear_bitmap, preX - clear_bitmap.getWidth() / 2, preY - clear_bitmap.getHeight() / 2,                        null);            }            // 沿着path绘制            // 这里使用cacheCanvas是防止Xfermode绘图出现绘图时黑框            cacheCanvas.drawPath(path, paint);            //canvas.drawCircle(preX, preY, 5, paint);            //画圆        } else {            // 沿着path绘制,没有这行不会动态绘制            canvas.drawPath(path, paint);        }    }    public void saveBitmap(Activity activity) {        String bitmapPath;        //sd卡存在        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {            bitmapPath = Environment.getExternalStorageDirectory() + "/" + "win.lioil.bluetooth" + "/resource/";        } else {            bitmapPath = "/data/data/win.lioil.bluetooth/resource";        }        File file = new File(bitmapPath);        if (!file.exists()) {            file.mkdirs();        }        try {            FileOutputStream fileOutputStream = new FileOutputStream(file.getPath() + "/2.png");            boolean isSuccess = cacheBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);            if (isSuccess) {                AssistStatic.showToast(activity, "保存成功");            } else {                AssistStatic.showToast(activity, "保存失败");            }            fileOutputStream.flush();            fileOutputStream.close();        } catch (Exception e) {            e.printStackTrace();        }    }}