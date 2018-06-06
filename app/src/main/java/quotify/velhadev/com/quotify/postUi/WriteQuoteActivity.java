package quotify.velhadev.com.quotify.postUi;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.editor.EditorActivity;
import quotify.velhadev.com.quotify.editor.EditorUtilities;
import quotify.velhadev.com.quotify.Utils.Tags;

import static quotify.velhadev.com.quotify.editor.EditorView.EXPORTED_STATIC_LAYOUT_WIDTH;

public class WriteQuoteActivity extends AppCompatActivity {


    private EditText editText;
    private FloatingActionButton accept;
    private FloatingActionButton cancel;


    private TextWatcher textWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (EditorUtilities.getQuoteLimit(charSequence) >= EXPORTED_STATIC_LAYOUT_WIDTH / 2.5) {
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(charSequence.length())});
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private View.OnClickListener acceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quote = editText.getText().toString();
            if (!quote.isEmpty()) {
                Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                intent.putExtra(Tags.QUOTE_STRING, quote);
                startActivity(intent);
                finish();
            }
        }
    };

    private View.OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_quote);
        this.setFinishOnTouchOutside(false);
        initializeViews();
        initializeListeners();
    }

    private void initializeListeners() {
        editText.setLongClickable(false);
        editText.addTextChangedListener(textWatcherListener);
        accept.setOnClickListener(acceptListener);
        cancel.setOnClickListener(cancelListener);
    }

    private void initializeViews() {
        editText = findViewById(R.id.et_quote);
        accept = findViewById(R.id.accept);
        cancel = findViewById(R.id.cancel);
    }
}
