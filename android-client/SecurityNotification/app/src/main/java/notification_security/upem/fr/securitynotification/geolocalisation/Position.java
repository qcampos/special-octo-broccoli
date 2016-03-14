package notification_security.upem.fr.securitynotification.geolocalisation;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * A GPS Position which is the only java object the vue and the NetworkService
 * exchange.
 */
public class Position implements Parcelable {

    private final double latitude;
    private final double longitude;
    // Used if the position is bound to a specific id.
    // For example, if we clik on the UrgenceMap, the position clicked
    // Will be bound with a specific id which is represented here.
    private final String id;
    // Same here, the has voted is used when an instance of position
    // handles the data bound to a position on the UrgenceMap.
    private boolean hasVoted;

    public Position(double latitude, double longitude) {
        this(latitude, longitude, "0", false);
    }

    public Position(double latitude, double longitude, String id, boolean hasVoted) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.hasVoted = hasVoted;
    }

    protected Position(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        id = in.readString();
        hasVoted = in.readByte() != 0;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId(){
        return id;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted){
        this.hasVoted = hasVoted;
    }

    public static final Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(id);
        dest.writeByte((byte) (hasVoted ? 1 : 0));
    }
}
