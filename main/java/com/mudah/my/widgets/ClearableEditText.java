package com.mudah.my.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mudah.my.R;
import com.mudah.my.configs.Config;


public class ClearableEditText extends RelativeLayout {
    LayoutInflater inflater = null;
    EditText mEditView;
    ImageButton btnClear;
    private OnSubmitListener mListener;

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context, attrs);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ClearableEditText(Context context) {
        this(context, null, 0);
    }

    void initViews(Context context, AttributeSet attrs) {

        inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.clearable_edit_text, this, true);
        if (!isInEditMode()) {
            mEditView = (EditText) findViewById(R.id.edt_input);
            btnClear = (ImageButton) findViewById(R.id.btn_clear);
            btnClear.setVisibility(RelativeLayout.INVISIBLE);
            btnClear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditView.setText("");
                }
            });

            if (attrs != null) {
                TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText);
                String hint = values.getString(R.styleable.ClearableEditText_hint);
                mEditView.setHint(hint);
                values.recycle();

                setAttributes(attrs);
            }

            showHideClearButton();

        }
    }

    public void addTextChangedListener(TextWatcher watcher) {
        mEditView.addTextChangedListener(watcher);
    }

    public void showHideClearButton() {
        mEditView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                if (s.length() > 0)
                    btnClear.setVisibility(RelativeLayout.VISIBLE);
                else
                    btnClear.setVisibility(RelativeLayout.INVISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    public void setMaxLength(int maxLength) {
        mEditView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
    }

    public Editable getText() {
        Editable text = mEditView.getText();
        return text;
    }

    public void setText(String s) {
        mEditView.setText(s);
    }

    public EditText getEditTextView() {
        return mEditView;
    }

    private void setAttributes(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        /* Remember to update setAttribute() when editing this: */
        String[] editTextAttributes = {"elipsis", "inputType", "minLines", "maxLines", "imeOptions",
                "singleLine", "gravity"};

        for (String editTextAttribute : editTextAttributes) {
            String hexAsString = attrs.getAttributeValue("http://schemas.android.com/apk/res/android",
                    editTextAttribute);
            if (hexAsString != null) {
                if (editTextAttribute.equals("singleLine")) {
                    mEditView.setSingleLine(Boolean.valueOf(hexAsString));
                } else {
                    int value = Integer.decode(hexAsString);
                    if (editTextAttribute.equals("imeOptions")) {
                        setImeOptions(value);
                    } else if (editTextAttribute.equals("inputType")) {
                        setInputType(value);

                        // This hack is needed for password fields. After setting the input value,
                        // our selected font is overridden and lost. Thus, we need to set it again.
                        if ((value & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0) {
                            setEditViewFont();
                        }
                    } else if (editTextAttribute.equals("minLines")) {
                        setMinLines(value);
                    } else if (editTextAttribute.equals("maxLines")) {
                        setMaxLines(value);
                    } else if (editTextAttribute.equals("gravity")) {
                        mEditView.setGravity(value);
                    }
                }
            }
        }

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mEditView.onTouchEvent(event);
            }
        });

        mEditView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (mListener == null || (event != null && event.getAction() != KeyEvent.ACTION_DOWN)) {
                    return false;
                } else if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || event == null
                        || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    String query = v.getText().toString();
                    if (query.length() < Config.MAX_FREE_TEXT_LENGTH) {
                        mListener.onSearchSubmit(query);
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getWindowToken(), 0);
                        mListener.onSearchSubmit(query);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Set ime option
     *
     * @param imeOptions The ime option
     */
    public void setImeOptions(int imeOptions) {
        mEditView.setImeOptions(imeOptions);
    }

    /**
     * Set the input type
     *
     * @param type The type
     */
    public void setInputType(int type) {
        mEditView.setInputType(type);
    }

    /**
     * Set minimum number of lines
     *
     * @param minlines The number of lines
     */
    public void setMinLines(int minlines) {
        mEditView.setMinLines(minlines);
    }

    /**
     * Set max number of lines
     *
     * @param maxlines The number of lines
     */
    public void setMaxLines(int maxlines) {
        mEditView.setMaxLines(maxlines);
    }

    public void setHint(String value) {
        mEditView.setHint(value);
    }

    public void focusEditText() {
        mEditView.requestFocus();
        mEditView.setSelection(mEditView.getText().length());
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditView, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setEditViewFont() {
        mEditView.setTextColor(getResources().getColor(R.color.scm_text));
        mEditView.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setSubmitListener(OnSubmitListener mListener) {
        this.mListener = mListener;
    }

    public interface OnSubmitListener {
        public void onSearchSubmit(String query);

        public void onSearchError(String error);
    }
}
