package de.bsvrz.sys.startstopp.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "songTitle", "albumId" })
public class Track {

	/**
	 * 
	 * (Required)
	 * 
	 */
	@JsonProperty("songTitle")
	private String songTitle;
	/**
	 * 
	 * (Required)
	 * 
	 */
	@JsonProperty("albumId")
	private String albumId;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 * 
	 * (Required)
	 * 
	 * @return The songTitle
	 */
	@JsonProperty("songTitle")
	public String getSongTitle() {
		return songTitle;
	}

	/**
	 * 
	 * (Required)
	 * 
	 * @param songTitle
	 *            The songTitle
	 */
	@JsonProperty("songTitle")
	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public Track withSongTitle(String songTitle) {
		this.songTitle = songTitle;
		return this;
	}

	/**
	 * 
	 * (Required)
	 * 
	 * @return The albumId
	 */
	@JsonProperty("albumId")
	public String getAlbumId() {
		return albumId;
	}

	/**
	 * 
	 * (Required)
	 * 
	 * @param albumId
	 *            The albumId
	 */
	@JsonProperty("albumId")
	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public Track withAlbumId(String albumId) {
		this.albumId = albumId;
		return this;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Track withAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(songTitle, albumId, additionalProperties);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Track) == false) {
			return false;
		}
		Track rhs = ((Track) other);

		boolean result = Objects.equals(songTitle, rhs.songTitle);
		result |= Objects.equals(albumId, rhs.albumId);
		result |= Objects.equals(additionalProperties, rhs.additionalProperties);
		return result;
	}

}
