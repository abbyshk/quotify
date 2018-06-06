package quotify.velhadev.com.quotify.editor;

import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import static quotify.velhadev.com.quotify.editor.EditorActivity.TEXT_SIZE_MAX;
import static quotify.velhadev.com.quotify.editor.EditorView.EXPORTED_PADDING_FACTOR;
import static quotify.velhadev.com.quotify.editor.EditorView.EXPORTED_STATIC_LAYOUT_WIDTH;

/**
 * Created by abhishek on 20/03/18.
 */

public final class EditorUtilities {

    public static int getQuoteLimit(CharSequence charSequence) {

        Rect rect = new Rect();

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(TEXT_SIZE_MAX);

        String text = charSequence.toString();

        textPaint.getTextBounds( text, 0, charSequence.length(), rect);

        StaticLayout textLayout = new StaticLayout(text,
                textPaint,
                EXPORTED_STATIC_LAYOUT_WIDTH,
                Layout.Alignment.ALIGN_CENTER,
                1,
                1,
                true);

        return (textLayout.getLineCount() * rect.height()) + EXPORTED_PADDING_FACTOR;
    }

}
