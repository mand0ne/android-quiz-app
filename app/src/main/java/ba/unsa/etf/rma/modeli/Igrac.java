package ba.unsa.etf.rma.modeli;

import android.os.Parcel;
import android.os.Parcelable;

public class Igrac implements Parcelable, Comparable<Igrac> {
    private final String nickname;
    private final Double skor;

    public Igrac(String first, Double second) {
        nickname = first;
        skor = second;
    }

    public String nickname() {
        return nickname;
    }

    public Double score() {
        return skor;
    }

    public Igrac(Parcel in) {
        this.nickname = in.readString();
        this.skor = (Double) in.readValue(Double.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nickname);
        dest.writeValue(this.skor);
    }

    public static final Parcelable.Creator<Igrac> CREATOR = new Parcelable.Creator<Igrac>() {
        @Override
        public Igrac createFromParcel(Parcel source) {
            return new Igrac(source);
        }

        @Override
        public Igrac[] newArray(int size) {
            return new Igrac[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        return this.nickname.equals(((Igrac) obj).nickname) &&
                this.skor.equals(((Igrac) obj).skor);
    }

    @Override
    public int hashCode() {
        return (nickname + skor * nickname.length()).hashCode();
    }

    @Override
    public int compareTo(Igrac o) {
        if (skor.equals(o.skor))
            return nickname.compareTo(o.nickname);

        return skor.compareTo(o.skor);
    }
}
