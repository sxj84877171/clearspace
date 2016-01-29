package com.clean.space;

import java.lang.reflect.Field;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * 更新的信息集合
 * 
 * @author Tim
 */
public class UpdateInfo {
	public String version;
	public String url;
	public String description;
	public String force = "false";

	public String zh;
	public String en;
	public String ja;
	public String it;
	public String de;
	public String fr;
	public String ko;

	public String getForce() {
		return force;
	}

	public void setForce(String force) {
		this.force = force;
	}

	public String getZh() {
		return zh;
	}

	public void setZh(String zh) {
		this.zh = zh;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getJa() {
		return ja;
	}

	public void setJa(String ja) {
		this.ja = ja;
	}

	public String getIt() {
		return it;
	}

	public void setIt(String it) {
		this.it = it;
	}

	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de;
	}

	public String getFr() {
		return fr;
	}

	public void setFr(String fr) {
		this.fr = fr;
	}

	public String getKo() {
		return ko;
	}

	public void setKo(String ko) {
		this.ko = ko;
	}

	public UpdateInfo() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// ///////////////////////////////////////////////////////////
	public static UpdateInfo parse(String jsonStr) {
		UpdateInfo updateinfo = null;
		try {
			Gson gson = new Gson();
			updateinfo = gson.fromJson(jsonStr, UpdateInfo.class);
		} catch (JsonSyntaxException e) {
		}
		return updateinfo;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String getLanguageDescription(Locale language) {
		String languageString = description;
		if (language != null) {
			Field[] fields = getClass().getFields();
			for (Field field : fields) {
				if (field.getName().equals(language.getCountry())
						|| field.getName().equals(language.getLanguage())
						|| field.getName().equals(language.getVariant())) {
					try {
						Object o = field.get(this);
						if (o != null) {
							languageString = o.toString();
						}
						break;
					} catch (IllegalAccessException e) {
					} catch (IllegalArgumentException e) {
					}
				}
			}
			if (language == null || "".equals(languageString)) {
				languageString = description;
			}
		}
		return languageString;
	}

}