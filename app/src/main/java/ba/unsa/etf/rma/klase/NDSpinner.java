package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Objects;

public class NDSpinner extends android.support.v7.widget.AppCompatSpinner {

    public NDSpinner(Context context) {
        super(context);
    }

    public NDSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NDSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected)
            try {
                Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            } catch (Exception e) {
                // do nothing
            }
    }

    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected)
            try {
                Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            } catch (Exception e) {
                // do nothing
            }
    }
}