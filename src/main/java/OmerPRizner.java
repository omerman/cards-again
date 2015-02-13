import java.util.ArrayList;
import java.util.List;

/**
 * Created by omerpr on 23/01/2015.
 */
public class OmerPRizner {
    private List<String> omer = new ArrayList<>();
    public OmerPRizner() {
        this.omer.add("O1");
        this.omer.add("O2");
    }

    public void setOmer(List<String> omer) {
        this.omer = omer;
    }

    public List<String> getOmer() {
        return omer;
    }
}
