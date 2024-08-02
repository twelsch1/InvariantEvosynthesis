package datatypes;
import ec.gp.GPData;

@SuppressWarnings("serial")
public class LongData extends GPData {
	public long x;
	
	public void copyTo(final GPData gpd) {
		((LongData)gpd).x = x; 
	}	
}
