package ba.unsa.etf.rma.customKlase;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Objects;

// Napravljen zbog "DodajKvizAkt" i kategorije "Dodaj kategoriju"
public class CustomSpinner extends android.support.v7.widget.AppCompatSpinner {

    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected)
            try {
                Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            } catch (Exception ignored) {
            }
    }

    @Override
    public void setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected)
            try {
                Objects.requireNonNull(getOnItemSelectedListener()).onItemSelected(this, getSelectedView(), position, getSelectedItemId());
            } catch (Exception ignored) {
            }
    }
}