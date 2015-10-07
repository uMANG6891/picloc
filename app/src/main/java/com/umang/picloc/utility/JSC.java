package com.umang.picloc.utility;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSC extends JSONObject {

	public static JSONObject jArrToObj(JSONArray ja, int index) {
		JSONObject jo = null;
		try {
			jo = ja.getJSONObject(index);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}

	public static String jArrToString(JSONArray ja, int index) {
		String str = "";
		try {
			str = ja.getString(index);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public static int jArrToInt(JSONArray ja, int index) {
		int n = 0;
		try {
			n = ja.getInt(index);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}

	public static JSONObject strToJOb(String jsonString) {
		JSONObject jObject = null;
		try {
			jObject = new JSONObject(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jObject;
	}

	public static JSONArray strToJAr(String jsonString) {
		JSONArray ja = null;
		try {
			ja = new JSONArray(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ja;
	}

	public static String getJString(JSONObject j, String key) {
		try {
			return j.getString(key);
		} catch (JSONException e) {
		}
		return null;
	}

	public static boolean getJBoolean(JSONObject j, String key) {
		try {
			return j.getBoolean(key);
		} catch (JSONException e) {
		}
		return false;
	}

	public static int getJInt(JSONObject j, String key) {
		try {
			return j.getInt(key);
		} catch (JSONException e) {
		}
		return 0;
	}

	public static long getJLong(JSONObject j, String key) {
		try {
			return j.getLong(key);
		} catch (JSONException e) {
		}
		return 0;
	}

	public static JSONArray replaceJObj(JSONArray ja, JSONObject jo, int pos) {
		try {
			ja.put(pos, jo);
			return ja;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray removeJObj(JSONArray ja, int pos) {
		JSONArray returnJA = new JSONArray();
		try {
			for (int i = 0; i < ja.length(); i++) {
				if (i != pos) {
					returnJA.put(ja.get(i));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnJA;
	}

	public static JSONObject removeJObjKeyValue(JSONObject jo, String key) {
		try {
			for (int i = 0; i < jo.names().length(); i++) {
				if (jo.names().getString(i).contentEquals(key)) {
					jo.remove(key);
					break;
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}

	public static JSONArray mergeJArray(JSONArray one, JSONArray two) {
		JSONArray t = new JSONArray();
		for (int i = 0; i < one.length(); i++) {
			t.put(JSC.jArrToObj(one, i));
		}
		for (int i = 0; i < two.length(); i++) {
			t.put(JSC.jArrToObj(two, i));
		}
		return t;
	}

	public static JSONObject putJObject(JSONObject j, String key, Object value) {
		try {
			j.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return j;
	}
}
