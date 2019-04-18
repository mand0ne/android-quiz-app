package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private Resources res;
    private Context context;
    private ArrayList<?> list;

    @SuppressWarnings("unchecked")
    public CustomAdapter(@NonNull Context context, ArrayList<?> list, Resources res) {
        super(context, 0, (List<Object>) list);
        this.context = context;
        this.list = list;
        this.res = res;
    }

    public void setList(ArrayList<?> list) {
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
                    if (trenutniKviz.getKategorija().getId().equals("-1"))
                        image.setImageResource(res.
                                getIdentifier("ba.unsa.etf.rma:drawable/quizico", null, null));
                    else if(trenutniKviz.getKategorija().getId().equals("-3"))
                        image.setImageResource(res.
                                getIdentifier("ba.unsa.etf.rma:drawable/undefinedquiz", null, null));
                    else
                        image.setImageDrawable(iconHelper.
                                getIcon(Integer.valueOf(trenutniKviz.getKategorija().getId())).getDrawable(context));
                }
            });

            ((TextView) listItem.findViewById(R.id.naziv)).setText(trenutniKviz.getNaziv());
        } else if (list.get(position) instanceof Pitanje) {
            Pitanje pitanje = (Pitanje) list.get(position);

            ((ImageView) listItem.findViewById(R.id.ikona)).setImageResource(res.getIdentifier("ba.unsa.etf.rma:drawable/questionico", null, null));
            ((TextView) listItem.findViewById(R.id.naziv)).setText(pitanje.getNaziv());
        }

        return listItem;
    }

    public View getFooterView(ViewGroup parent, String text) {
        View fview = LayoutInflater.from(context).inflate(R.layout.element_liste, parent, false);
        ((TextView) fview.findViewById(R.id.naziv)).setText(text);

        return fview;
    }

}
