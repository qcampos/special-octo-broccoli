package notification_security.upem.fr.securitynotification.map;

import java.io.Serializable;

/**
 * Created by anis on 03/03/16.
 */
public class Alert implements Serializable{
    public String title;
    public double lat;
    public double lng;

    public Alert(String title, double lat, double lng){
        this.title = title;
        this.lat = lat;
        this.lng = lng;
    }
}
