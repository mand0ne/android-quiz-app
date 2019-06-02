package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

public class IgraPair implements Parcelable {
    private final String igrac;
    private final Double skor;

    public IgraPair(String first, Double second) {
        igrac = first;
        skor = second;
    }

    public String first() {
        return igrac;
    }

    public Double second() {
        return skor;
    }

    public IgraPair(Parcel in) {
        this.igrac = in.readString();
        this.skor = (Double) in.readValue(Double.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.igrac);
        dest.writeValue(this.skor);
    }

    public static final Parcelable.Creator<IgraPair> CREATOR = new Parcelable.Creator<IgraPair>() {
        @Override
        public IgraPair createFromParcel(Parcel source) {
            return new IgraPair(source);
        }

        @Override
        public IgraPair[] newArray(int size) {
            return new IgraPair[size];
        }
    };
}
