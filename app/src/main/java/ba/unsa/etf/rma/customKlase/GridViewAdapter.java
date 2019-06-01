package ba.unsa.etf.rma.customKlase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.modeli.Kviz;

public class GridViewAdapter extends ArrayAdapter<Kviz> {

    private Context context;

    public GridViewAdapter(Context c, ArrayList<Kviz> kvizovi) {
        super(c, R.layout.element_grid_view, kvizovi);
        context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View gridItem = convertView;

        if (gridItem == null)
            gridItem = LayoutInflater.from(context).inflate(R.layout.element_grid_view, parent, false);


        final ImageView image = gridItem.findViewById(R.id.ikonaKategorije);
        TextView nazivKviza = gridItem.findViewById(R.id.nazivKviza);
        TextView brojPitanjaKviza = gridItem.findViewById(R.id.brojPitanjaKviza);


        final Kviz trenutniKviz = getItem(position);

        assert trenutniKviz != null;
        nazivKviza.setText(trenutniKviz.getNaziv());

        if (trenutniKviz.getPitanja() != null)
            brojPitanjaKviza.setText(String.valueOf(trenutniKviz.getPitanja().size()));
        else
            brojPitanjaKviza.setText("");

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

        return gridItem;
    }
}
