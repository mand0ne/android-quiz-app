package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import java.util.ArrayList;
import java.util.List;

import ba.unsa.etf.rma.R;


public class CustomAdapter extends ArrayAdapter<Object> {

    private Context context;
    private ArrayList<?> list;

    @SuppressWarnings("unchecked")
    public CustomAdapter(@NonNull Context context, ArrayList<?> list) {
        super(context, 0, (List<Object>) list);
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
            listItem = LayoutInflater.from(context).inflate(R.layout.element_liste, parent, false);

        if (list.get(position) instanceof Kviz) {
            final Kviz trenutniKviz = (Kviz) list.get(position);

            final ImageView image = listItem.findViewById(R.id.ikona);
            final IconHelper iconHelper = IconHelper.getInstance(context);

            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    switch (trenutniKviz.getKategorija().getId()) {
                        case "-100":
                            image.setImageResource(R.drawable.addico);
                            break;
                        case "-1":
                            image.setImageResource(R.drawable.quizico);
                            break;
                        case "-2":
                            break;
                        case "-3":
                            image.setImageResource(R.drawable.undefinedquiz);
                            break;
                        default:
                            image.setImageDrawable(iconHelper.
                                    getIcon(Integer.valueOf(trenutniKviz.getKategorija().getId())).getDrawable(context));
                            break;
                    }
                }
            });

            ((TextView) listItem.findViewById(R.id.naziv)).setText(trenutniKviz.getNaziv());
        } else if (list.get(position) instanceof Pitanje) {
            Pitanje pitanje = (Pitanje) list.get(position);

            ((ImageView) listItem.findViewById(R.id.ikona)).setImageResource(R.drawable.questionico);
            ((TextView) listItem.findViewById(R.id.naziv)).setText(pitanje.getNaziv());
        } else if (list.get(position) instanceof Kategorija) {
            final Kategorija kategorija = (Kategorija) list.get(position);

            final ImageView image = listItem.findViewById(R.id.ikona);
            final IconHelper iconHelper = IconHelper.getInstance(context);

            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    Log.i("K A T E G O R I J A", kategorija.getId());
                    switch (kategorija.getId()) {
                        case "-100":
                            image.setImageResource(R.drawable.addico);
                            break;
                        case "-1":
                            image.setImageResource(R.drawable.quizico);
                            break;
                        case "-3":
                            image.setImageResource(R.drawable.undefinedquiz);
                            break;
                        case "-2":
                            break;
                        default:
                            image.setImageDrawable(iconHelper.
                                    getIcon(Integer.valueOf(kategorija.getId())).getDrawable(context));
                            break;
                    }
                }
            });

            ((TextView) listItem.findViewById(R.id.naziv)).setText(kategorija.getNaziv());
        }

        return listItem;
    }

    public View getFooterView(ViewGroup parent, String text) {
        View fview = LayoutInflater.from(context).inflate(R.layout.element_liste, parent, false);
        ((TextView) fview.findViewById(R.id.naziv)).setText(text);

        return fview;
    }

}
