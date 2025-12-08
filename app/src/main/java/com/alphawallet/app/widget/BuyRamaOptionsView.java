package com.alphawallet.app.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import com.alphawallet.app.C;
import com.alphawallet.app.R;
import com.alphawallet.app.entity.DialogDismissInterface;


public class BuyRamaOptionsView extends FrameLayout implements View.OnClickListener
{
    private OnClickListener onBuyFromBitMartListener;
    private OnClickListener onBuyFromKoinparkListener;
    private OnClickListener onBuyFromRamaSwapListener;
    private DialogDismissInterface dismissInterface;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public BuyRamaOptionsView(Context context)
    {
        this(context, R.layout.dialog_buy_rama_options);
    }

    public BuyRamaOptionsView(Context context, @LayoutRes int layoutId)
    {
        super(context);
        init(layoutId);
    }

    private void init(@LayoutRes int layoutId)
    {
        LayoutInflater.from(getContext()).inflate(layoutId, this, true);
        findViewById(R.id.buy_from_bitmart).setOnClickListener(this);
        findViewById(R.id.buy_from_koinpark).setOnClickListener(this);
        findViewById(R.id.buy_from_ramaswap).setOnClickListener(this);

        //close after 30 seconds of inactivity
        handler.postDelayed(closePopup, C.STANDARD_POPUP_INACTIVITY_DISMISS);
    }

    private final Runnable closePopup = () -> {
        if (dismissInterface != null) {
            dismissInterface.dismissDialog();
        }
    };

    @Override
    public void onClick(View view)
    {
        handler.removeCallbacks(closePopup);
        
        if (view.getId() == R.id.buy_from_bitmart)
        {
            if (onBuyFromBitMartListener != null)
            {
                onBuyFromBitMartListener.onClick(view);
            }
        }
        else if (view.getId() == R.id.buy_from_koinpark)
        {
            if (onBuyFromKoinparkListener != null)
            {
                onBuyFromKoinparkListener.onClick(view);
            }
        }
        else if (view.getId() == R.id.buy_from_ramaswap)
        {
            if (onBuyFromRamaSwapListener != null)
            {
                onBuyFromRamaSwapListener.onClick(view);
            }
        }
    }

    public void setOnBuyFromBitMartListener(OnClickListener onClickListener)
    {
        this.onBuyFromBitMartListener = onClickListener;
    }

    public void setOnBuyFromKoinparkListener(OnClickListener onClickListener)
    {
        this.onBuyFromKoinparkListener = onClickListener;
    }

    public void setOnBuyFromRamaSwapListener(OnClickListener onClickListener)
    {
        this.onBuyFromRamaSwapListener = onClickListener;
    }

    public void setDismissInterface(DialogDismissInterface dismissInterface)
    {
        this.dismissInterface = dismissInterface;
    }
}
