package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;

public class PenaltyData implements DataContainer {

	public PenaltyData(JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public PenaltyData() {}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        return null;
    }

    public int getWarningCount() {
        return 0;
    }
}