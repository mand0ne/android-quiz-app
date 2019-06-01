package ba.unsa.etf.rma.customKlase;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import ba.unsa.etf.rma.R;

public class RangListaAdapter extends ArrayAdapter<Pair<String, Double>> {

    private Context context;
    private ArrayList<Pair<String, Double>> list;

    public RangListaAdapter(@NonNull Context context, ArrayList<Pair<String, Double>> list) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.element_rang_liste, parent, false);

        ((TextView) listItem.findViewById(R.id.pozicija)).setText(String.valueOf(position + 1));
        ((TextView) listItem.findViewById(R.id.igrac)).setText(list.get(position).first);

        BigDecimal bd = new BigDecimal(list.get(position).second * 100.0);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        ((TextView) listItem.findViewById(R.id.skor)).setText(String.valueOf(bd.doubleValue()));

        return listItem;
    }
}
