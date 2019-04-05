package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.content.res. Resources;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconDialog;
import com.maltaisn.icondialog.IconHelper;

import java.util.ArrayList;
import java.util.List;
import ba.unsa.etf.rma.R;

public class CustomAdapter extends ArrayAdapter<Object> {

    private Resources res;
    private Context context;
    private ArrayList<? extends Object> list;

    public CustomAdapter(@NonNull Context context, ArrayList<? extends Object> list, Resources res) {
        super(context, 0, (List<Object>) list);
        this.context = context;
        this.list = list;
        this.res = res;
    }

    public void setList(ArrayList<? extends Object> list){
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null? list.size() : 0;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.element_liste, parent, false);

        if (list.get(position) instanceof Kviz) {
            Kviz trenutniKviz = (Kviz) list.get(position);

            String ikona = "quizico"; // trenutniKviz.getKategorija().getNaziv();

            ImageView image = listItem.findViewById(R.id.ikona);
            image.setImageResource(res.getIdentifier("ba.unsa.etf.rma:drawable/" + ikona, null, null));

            ((TextView) listItem.findViewById(R.id.naziv)).setText(trenutniKviz.getNaziv());
        }
        else if(list.get(position) instanceof  Pitanje){
            Pitanje pitanje = (Pitanje) list.get(position);
            final IconHelper iconHelper = IconHelper.getInstance(context);
            final View finalListItem = listItem;
            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    // This happens on UI thread, and is guaranteed to be called.
                    ((ImageView) finalListItem.findViewById(R.id.ikona)).setImageDrawable(iconHelper.getIcon(3).getDrawable(context));
                }
            });

            ((TextView) listItem.findViewById(R.id.naziv)).setText(pitanje.getNaziv());
        }

        return listItem;
    }

    public View getFooterView(ViewGroup parent, String text){
        View fview = LayoutInflater.from(context).inflate(R.layout.element_liste, parent, false);
        ((TextView) fview.findViewById(R.id.naziv)).setText(text);

        return fview;
    }
}
