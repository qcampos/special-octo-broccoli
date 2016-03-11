package notification_security.upem.fr.securitynotification.map;

import java.io.Serializable;

/**
 * A GPS Position which is the only java object the vue and the NetworkService
 * exchange.
 */
public class Position implements Serializable {

    public final double latitude;
    public final double longitude;
    // Used if the position is bound to a specific id.
    // For example, if we clik on the UrgenceMap, the position clicked
    // Will be bound with a specific id which is represented here.
    private final long id;
    // Same here, the has voted is used when an instance of position
    // handles the data bound to a position on the UrgenceMap.
    private final boolean hasVoted;

    public Position(double latitude, double longitude) {
        this(latitude, longitude, -1L, false);
    }

    public Position(double latitude, double longitude, long id, boolean hasVoted) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.hasVoted = hasVoted;
    }

    @Override
    public String toString() {
        return "[lat : " + latitude + " , longitude : " + longitude + " ]";
    }
}
