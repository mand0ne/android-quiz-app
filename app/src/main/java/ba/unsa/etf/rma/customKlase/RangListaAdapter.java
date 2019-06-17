package ba.unsa.etf.rma.customKlase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.modeli.Igrac;

public class RangListaAdapter extends ArrayAdapter<Igrac> {

    private Context context;
    private ArrayList<Igrac> list;

    public RangListaAdapter(@NonNull Context context, ArrayList<Igrac> list) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("NullableProblems")
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.element_rang_liste, parent, false);

        ((TextView) listItem.findViewById(R.id.pozicija)).setText((position + 1) + ".\t");
        ((TextView) listItem.findViewById(R.id.igrac)).setText(list.get(position).nickname() + "\t");

        BigDecimal bd = new BigDecimal(list.get(position).score());
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        ((TextView) listItem.findViewById(R.id.skor)).setText(String.valueOf(bd.doubleValue()));

        return listItem;
    }
}
