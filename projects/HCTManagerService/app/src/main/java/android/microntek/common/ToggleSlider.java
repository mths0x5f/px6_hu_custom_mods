package android.microntek.common;

import android.content.Context;
import android.microntek.HctUtil;
import android.microntek.app.MyButton;
import android.microntek.service.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
/* loaded from: classes.dex */
public class ToggleSlider extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    private MyButton mBtnDownVol;
    private MyButton mBtnUpVol;
    private TextView mLabel;
    private Listener mListener;
    private SeekBar mSlider;
    private ImageView mToggle;

    /* loaded from: classes.dex */
    public interface Listener {
        void onChanged(int i);

        void onDownVolClick();

        void onInit();

        void onUpVolClick();

        void onVolClick();
    }

    public ToggleSlider(Context context) {
        this(context, null);
    }

    public ToggleSlider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        String mCustomerSub = HctUtil.getCustomerSub();
        if (mCustomerSub.startsWith("ASUKA")) {
            View.inflate(context, R.layout.asuka_volume_slider, this);
            this.mBtnDownVol = (MyButton) findViewById(R.id.btn_down_vol);
            this.mBtnUpVol = (MyButton) findViewById(R.id.btn_up_vol);
            this.mBtnDownVol.setOnClickListener(new View.OnClickListener() { // from class: android.microntek.common.ToggleSlider.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    if (ToggleSlider.this.mListener != null) {
                        ToggleSlider.this.mListener.onDownVolClick();
                    }
                }
            });
            this.mBtnUpVol.setOnClickListener(new View.OnClickListener() { // from class: android.microntek.common.ToggleSlider.2
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    if (ToggleSlider.this.mListener != null) {
                        ToggleSlider.this.mListener.onUpVolClick();
                    }
                }
            });
        } else if ("HZC25".equals(mCustomerSub) || "HZC25_1".equals(mCustomerSub) || "HZC25_2".equals(mCustomerSub)) {
            View.inflate(context, R.layout.hzc25_volume_slider, this);
        } else {
            View.inflate(context, R.layout.volume_slider, this);
        }
        ImageView imageView = (ImageView) findViewById(R.id.toggle);
        this.mToggle = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: android.microntek.common.ToggleSlider.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (ToggleSlider.this.mListener != null) {
                    ToggleSlider.this.mListener.onVolClick();
                }
            }
        });
        SeekBar seekBar = (SeekBar) findViewById(R.id.slider);
        this.mSlider = seekBar;
        seekBar.setOnSeekBarChangeListener(this);
        this.mLabel = (TextView) findViewById(R.id.label);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onInit();
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Listener listener = this.mListener;
        if (listener != null && fromUser) {
            listener.onChanged(progress);
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStartTrackingTouch(SeekBar seekBar) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onChanged(this.mSlider.getProgress());
        }
    }

    @Override // android.widget.SeekBar.OnSeekBarChangeListener
    public void onStopTrackingTouch(SeekBar seekBar) {
        Listener listener = this.mListener;
        if (listener != null) {
            listener.onChanged(this.mSlider.getProgress());
        }
    }

    public void setOnChangedListener(Listener l) {
        this.mListener = l;
    }

    public void setMax(int max) {
        this.mSlider.setMax(max);
    }

    public void setValue(int value) {
        int i;
        int i2;
        this.mSlider.setProgress(value);
        this.mLabel.setText(String.valueOf(value));
        if ("HZC25".equals(HctUtil.getCustomer()) || "HZC25_1".equals(HctUtil.getCustomer()) || "HZC25_2".equals(HctUtil.getCustomer())) {
            ImageView imageView = this.mToggle;
            if (value <= 0) {
                i = R.drawable.hzc25_audio_vol_mute;
            } else {
                i = R.drawable.hzc25_audio_vol;
            }
            imageView.setImageResource(i);
            return;
        }
        ImageView imageView2 = this.mToggle;
        if (value <= 0) {
            i2 = 17302318;
        } else {
            i2 = 17302317;
        }
        imageView2.setImageResource(i2);
    }
}
