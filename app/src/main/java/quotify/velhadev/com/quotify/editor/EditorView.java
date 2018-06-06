package quotify.velhadev.com.quotify.editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by abhishek on 14/03/18.
 */

public class EditorView extends View {

    public static final int DEFAULT_PADDING_FACTOR = 100;
    public static final int EXPORTED_BITMAP_RESOLUTION = 1080;
    public static final int EXPORTED_STATIC_LAYOUT_WIDTH = 980;
    public static final int EXPORTED_PADDING_FACTOR = (EXPORTED_BITMAP_RESOLUTION - EXPORTED_STATIC_LAYOUT_WIDTH) / 2;

    private int previewResolution;
    private int previewStaticLayoutWidth;
    private int previewPaddingFactor;
    private float textScaleFactor;

    private Context context;

    private float textSize = 45;
    private Typeface typeface;
    private boolean isGradient = false;
    private int textColor = Color.BLACK;
    private int solidBackgroundColor = Color.WHITE;
    private int gradientStartColor = Color.WHITE;
    private int gradientEndColor = Color.WHITE;
    private Paint backgroundPaint;
    private Rect gradientBounds;

    private String quoteText;
    private TextPaint textPaint;
    private StaticLayout textLayout;
    private StaticLayout authorLayout;
    private float textTranslateX = 0f;
    private float textTranslateY = 0f;
    private float authorTranslateX = 0f;
    private float authorTranslateY = 0f;


    public EditorView(Context context) {
        super(context);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        init(null, 0);
    }

    public EditorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        init(attrs, 0);
    }

    public EditorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setDrawingCacheEnabled(true);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        backgroundPaint = new Paint();
        textPaint = new TextPaint();
        initializePreviewSettings(EditorActivity.getDeviceWidth(context));
        typeface = Typeface.DEFAULT;
        gradientBounds = new Rect(0, 0, 0, 0);
        drawSolidBackground();
        writeQuote();
    }

    private void initializePreviewSettings(int deviceWidth) {
        previewResolution = deviceWidth;
        previewStaticLayoutWidth = previewResolution - DEFAULT_PADDING_FACTOR;
        previewPaddingFactor = (previewResolution - previewStaticLayoutWidth) / 2;
        BigDecimal bg = BigDecimal.valueOf(previewStaticLayoutWidth).divide(BigDecimal.valueOf(EXPORTED_STATIC_LAYOUT_WIDTH), 6, RoundingMode.DOWN);
        textScaleFactor = bg.floatValue();
    }

    private void drawSolidBackground() {
        gradientBounds.set(previewResolution, previewResolution, 0, 0);
        backgroundPaint.setShader(null);
        backgroundPaint.setColor(solidBackgroundColor);
    }

    private void drawGradient() {

        LinearGradient linearGradient =
                new LinearGradient(previewResolution, 0, 0, previewResolution,
                gradientStartColor,
                gradientEndColor,
                Shader.TileMode.CLAMP);

        backgroundPaint.setDither(true);
        backgroundPaint.setShader(linearGradient);

    }

    private void writeQuote() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(quoteText != null && user != null) {
            textPaint.setAntiAlias(true);
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize * textScaleFactor);
            textPaint.setTypeface(typeface);

            textLayout = new StaticLayout(quoteText,
                    textPaint,
                    previewStaticLayoutWidth,
                    Layout.Alignment.ALIGN_CENTER,
                    1,
                    1,
                    true);

            textTranslateY = getYcenter();
            textTranslateX = previewPaddingFactor;

            TextPaint authorTextPaint = new TextPaint();
            String authorName = user.getDisplayName();
            authorTextPaint.setTypeface(typeface);
            authorTextPaint.setAntiAlias(true);
            authorTextPaint.setTextSize(textPaint.getTextSize()/2);
            authorTextPaint.setColor(textColor);
            authorLayout = new StaticLayout(authorName,
                    authorTextPaint,
                    getAuthorWidth(authorName, authorTextPaint),
                    Layout.Alignment.ALIGN_NORMAL,
                    1,
                    1,
                    true);

            authorTranslateX =  previewResolution - getAuthorWidth(authorName, authorTextPaint) - DEFAULT_PADDING_FACTOR;
            authorTranslateY = (getYcenter() + getTextBlockHeight()) + DEFAULT_PADDING_FACTOR + textPaint.getTextSize()/2;

        }

    }

    private int getAuthorWidth(String authorName, TextPaint authorTextPaint) {
        Rect rect = new Rect();
        authorTextPaint.getTextBounds(authorName, 0, authorName.length(), rect);
        return rect.width();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(gradientBounds, backgroundPaint);
        canvas.save();
        canvas.translate(textTranslateX, textTranslateY);
        if(textLayout != null) {
            textLayout.draw(canvas);
        }
        canvas.restore();
        canvas.save();
        canvas.translate(authorTranslateX, authorTranslateY);
        if(authorLayout != null)
            authorLayout.draw(canvas);
        canvas.restore();
    }


    private int getTextBlockHeight() {
        Rect rect = new Rect();
        textPaint.getTextBounds(quoteText, 0, quoteText.length(), rect);
        int numberOfLines = textLayout.getLineCount();
        return numberOfLines * rect.height();
    }

    public float getYcenter() {
        return (previewStaticLayoutWidth / 2) - (getTextBlockHeight() / 2) - previewPaddingFactor/2;
    }

    public void changeTextColor(int color) {
        textColor = color;
        writeQuote();
        invalidate();
        requestLayout();
    }

    public void changeTextSize(float size){
        textSize = size;
        writeQuote();
        invalidate();
        requestLayout();
    }

    public void fontStyle(String path) {
        typeface = Typeface.createFromAsset(context.getAssets(), path);
        writeQuote();
        invalidate();
        requestLayout();
    }

    public void setSolidBackground(int color) {
        this.solidBackgroundColor = color;
        drawSolidBackground();
        invalidate();
        requestLayout();
        saveSolidBackgroundState(color);
    }

    public void applyGradient(int[] gradientColors) {
        gradientStartColor = gradientColors[0];
        gradientEndColor = gradientColors[1];
        isGradient = true;
        drawGradient();
        invalidate();
        requestLayout();
        saveGradientBackgroundState(gradientColors);
    }

    public void setQuoteText(String quote) {
        quoteText = quote;
        writeQuote();
        invalidate();
        requestLayout();
    }

    public float getTextSize() {
        return textSize;
    }

    public String getQuote() {
        return quoteText;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getSolidBackground() {
        return solidBackgroundColor;
    }

    public int getGradientFirstColor() {
        return gradientStartColor;
    }

    public int getGradientSecondColor() {
        return gradientEndColor;
    }

    public void saveGradientBackgroundState(int[] colors) {
        gradientStartColor = colors[0];
        gradientEndColor = colors[1];
    }

    public void saveSolidBackgroundState(int color) {
        solidBackgroundColor = color;
    }

    public void prepareBitmap() {
        previewResolution = EXPORTED_BITMAP_RESOLUTION;
        previewPaddingFactor = EXPORTED_PADDING_FACTOR;
        previewStaticLayoutWidth = EXPORTED_STATIC_LAYOUT_WIDTH;
        textScaleFactor = 1;
        if(isGradient)
            drawGradient();
        else
            drawSolidBackground();
        writeQuote();
        invalidate();
        requestLayout();
    }
}
