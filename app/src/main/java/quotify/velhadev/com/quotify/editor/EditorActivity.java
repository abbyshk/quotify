package quotify.velhadev.com.quotify.editor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.R;


public class EditorActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private final Activity activity = this;
    public static final int UPLOAD_REQUEST_CODE = 101;

    private static final int DIALOG_ID = 0;
    private static final int BACKGROUND_COLOR_ONE_ID = 1;
    private static final int BACKGROUND_COLOR_TWO_ID = 2;
    public static final int TEXT_SIZE_MIN = 45;
    public static final int TEXT_SIZE_MAX = 60;

    private int[] gradient = new int[]{Color.WHITE, Color.WHITE};
    private int solidBackgroundColor = Color.WHITE;
    private ArrayList<Pair<String, String>> fonts;
    private String quote;

    private Bitmap finalImage;
    private Button textColorSelectorButton;
    private Button firstColorPickerButton;
    private EditorView editorView;
    private SeekBar textSizeSeekBar;
    private CheckBox gradientSettingCheckbox;
    private Button secondColorPickerButton;
    private TextView firstColorTextView;
    private TextView secondColorTextView;
    private Spinner fontSpinner;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private FloatingActionButton uploadButton;
    private AlertDialog exitDialog;
    private ImageView slideUpAndDown;

    private View.OnClickListener colorPicker = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowPresets(false)
                    .setDialogId(DIALOG_ID)
                    .setColor(Color.BLACK)
                    .setShowAlphaSlider(true)
                    .show(activity);
        }
    };

    private View.OnClickListener solidColorPicker = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowPresets(false)
                    .setDialogId(BACKGROUND_COLOR_ONE_ID)
                    .setColor(Color.BLACK)
                    .setShowAlphaSlider(true)
                    .show(activity);
        }
    };

    private View.OnClickListener colorPicker2 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ColorPickerDialog.newBuilder()
                    .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                    .setAllowPresets(false)
                    .setDialogId(BACKGROUND_COLOR_TWO_ID)
                    .setColor(Color.BLACK)
                    .setShowAlphaSlider(true)
                    .show(activity);
        }
    };

    private View.OnClickListener gradientSettingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (gradientSettingCheckbox.isChecked()) {
                showGradientSettings();
            } else
                hideGradientSettings();
        }
    };


    private SeekBar.OnSeekBarChangeListener textSizeChanger =
            new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    float value = TEXT_SIZE_MIN + (i);
                    editorView.changeTextSize(value);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    private SlidingUpPanelLayout.PanelSlideListener panelSlideListener =
            new SlidingUpPanelLayout.PanelSlideListener() {

                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    fontSpinner.setAlpha(1 - slideOffset);
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        slideUpAndDown.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24px);
                    }
                    else {
                        slideUpAndDown.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24px);
                    }
                }
            };

    private AdapterView.OnItemSelectedListener fontSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            editorView.fontStyle(fonts.get(i).second);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private DialogInterface.OnClickListener exit = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };

    private DialogInterface.OnClickListener stay = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        quote = getIntent().getStringExtra(Tags.QUOTE_STRING);
        loadFonts();
        initializeEditorView();
        initializeViews();
        textSizeSeekBar.setMax(TEXT_SIZE_MAX - TEXT_SIZE_MIN);
        initializeListeners();

        SpinnerAdapter adapter = new SpinnerAdapter(this, fonts);
        fontSpinner.setAdapter(adapter);

        uploadButton = findViewById(R.id.upload);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (finalImage == null)
                    finalImage = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(finalImage);
                editorView.prepareBitmap();
                editorView.draw(canvas);

                if(finalImage != null) {
                    finalImage.setHasAlpha(true);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    finalImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] data = byteArrayOutputStream.toByteArray();

                    Intent intent = new Intent(getApplicationContext(), UploadAndDeleteActivity.class);
                    intent.putExtra(Tags.IMAGE_DATA, data);
                    startActivityForResult(intent, UPLOAD_REQUEST_CODE);
                }

            }
        });
    }



    private void initializeEditorView() {
        editorView = findViewById(R.id.bitmap_editor_view);
        int deviceWidth = getDeviceWidth(this);
        editorView.setLayoutParams(new ConstraintLayout.LayoutParams(deviceWidth, deviceWidth));
        if(quote != null && !quote.isEmpty())
            editorView.setQuoteText(quote);
    }

    private void loadFonts() {
        fonts = new ArrayList<>();
        fonts.add(FontsData.ALEO_FONT);
        fonts.add(FontsData.EXPRESSWAY_FONT);
        fonts.add(FontsData.LORA_FONT);
        fonts.add(FontsData.ALDER_FONT);
    }

    private void initializeListeners() {
        textSizeSeekBar.setOnSeekBarChangeListener(textSizeChanger);
        textColorSelectorButton.setOnClickListener(colorPicker);
        firstColorPickerButton.setOnClickListener(solidColorPicker);
        secondColorPickerButton.setOnClickListener(colorPicker2);
        gradientSettingCheckbox.setOnClickListener(gradientSettingListener);
        slidingUpPanelLayout.addPanelSlideListener(panelSlideListener);
        fontSpinner.setOnItemSelectedListener(fontSelectedListener);
        createDialog();
    }

    private void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_editor_activity_dialog_msg))
                .setPositiveButton(getString(R.string.dialog_editor_activity_dialog_stay), stay)
                .setNegativeButton(getString(R.string.dialog_editor_activity_dialog_exit), exit);
        exitDialog = builder.create();
    }

    private void initializeViews() {
        textSizeSeekBar = findViewById(R.id.text_size_seek_bar);
        textColorSelectorButton = findViewById(R.id.selected_text_color);
        firstColorPickerButton = findViewById(R.id.solid_background_selector);
        gradientSettingCheckbox = findViewById(R.id.gradient_check_box);
        secondColorPickerButton = findViewById(R.id.solid_background_color2_selector);
        firstColorTextView = findViewById(R.id.tv_select_color1_heading);
        secondColorTextView = findViewById(R.id.tv_select_color2_heading);
        fontSpinner = findViewById(R.id.font_spinner);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        slideUpAndDown = findViewById(R.id.up_button_placeholder);
        slidingUpPanelLayout.setDragView(R.id.drag_view);
    }

    private void showGradientSettings() {
        editorView.applyGradient(gradient);
        firstColorPickerButton.setBackgroundColor(editorView.getGradientFirstColor());
        secondColorPickerButton.setBackgroundColor(editorView.getGradientSecondColor());
        firstColorTextView.setText(getString(R.string.gradient_color_1));
        secondColorPickerButton.setVisibility(View.VISIBLE);
        secondColorTextView.setVisibility(View.VISIBLE);
    }

    private void hideGradientSettings() {
        editorView.setSolidBackground(solidBackgroundColor);
        firstColorPickerButton.setBackgroundColor(editorView.getSolidBackground());
        firstColorTextView.setText(getString(R.string.select_solid_background));
        secondColorPickerButton.setVisibility(View.GONE);
        secondColorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {

        switch (dialogId) {
            case DIALOG_ID:
                editorView.changeTextColor(color);
                textColorSelectorButton.setBackgroundColor(color);
                break;

            case BACKGROUND_COLOR_ONE_ID:
                if (gradientSettingCheckbox.isChecked()) {
                    gradient[0] = color;
                    editorView.applyGradient(gradient);
                } else {
                    solidBackgroundColor = color;
                    editorView.setSolidBackground(solidBackgroundColor);
                }
                firstColorPickerButton.setBackgroundColor(color);
                break;

            case BACKGROUND_COLOR_TWO_ID:
                gradient[1] = color;
                secondColorPickerButton.setBackgroundColor(color);
                editorView.applyGradient(gradient);
                break;
        }

    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Tags.SELECTED_TEXT_COLOR, editorView.getTextColor());
        outState.putBoolean(Tags.CHECKBOX_STATE, gradientSettingCheckbox.isChecked());
        outState.putInt(Tags.SOLID_BACKGROUND, editorView.getSolidBackground());
        outState.putInt(Tags.GRADIENT_FIRST_COLOR, editorView.getGradientFirstColor());
        outState.putInt(Tags.GRADIENT_SECOND_COLOR, editorView.getGradientSecondColor());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        gradient[0] = savedInstanceState.getInt(Tags.GRADIENT_FIRST_COLOR, Color.WHITE);
        gradient[1] = savedInstanceState.getInt(Tags.GRADIENT_SECOND_COLOR, Color.WHITE);
        solidBackgroundColor = savedInstanceState.getInt(Tags.SOLID_BACKGROUND, Color.WHITE);
        int textColor = savedInstanceState.getInt(Tags.SELECTED_TEXT_COLOR, Color.BLACK);
        boolean isGradientEnabled = savedInstanceState.getBoolean(Tags.CHECKBOX_STATE);

        editorView.saveSolidBackgroundState(solidBackgroundColor);
        editorView.saveGradientBackgroundState(gradient);

        if (isGradientEnabled) {
            showGradientSettings();
        } else {
            hideGradientSettings();
        }

        editorView.changeTextColor(textColor);
        textColorSelectorButton.setBackgroundColor(textColor);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean check;
        if (requestCode == UPLOAD_REQUEST_CODE) {
            check = data.getBooleanExtra(Tags.UPLOAD_HANDLED, false);
            if (check)
                finish();
            else
                Toast.makeText(this,
                        getString(R.string.classic_error),
                        Toast.LENGTH_SHORT).show();
        }
    }

    public static int getDeviceWidth(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
        int deviceWidth = displayMetrics.widthPixels;
        int deviceHeight = displayMetrics.heightPixels;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return deviceHeight;
        else
            return deviceWidth;
    }

    @Override
    public void onBackPressed() {
        if(slidingUpPanelLayout != null
                && slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else
            exitDialog.show();
    }

}
