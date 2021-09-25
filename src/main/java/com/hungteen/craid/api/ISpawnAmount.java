package com.hungteen.craid.api;

import com.google.gson.JsonElement;

public interface ISpawnAmount {

	int getSpawnAmount();
	
	/**
	 * make sure constructer has no argument, 
	 * and use this method to initiate instance.
	 */
	void readJson(JsonElement json);
}
