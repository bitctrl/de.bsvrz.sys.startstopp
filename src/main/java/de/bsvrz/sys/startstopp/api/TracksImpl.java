package de.bsvrz.sys.startstopp.api;

import javax.ws.rs.client.Entity;

public class TracksImpl implements Tracks {


	  @Override
	  public GetTracksResponse getTracks() {
	    Track track = new Track();
	    track.setAlbumId("17");
	    track.setSongTitle("Booyakasha");
	    
	    Object json = Entity.json(track);
	    System.out.println(json);
	    return GetTracksResponse.respond200WithApplicationJson(track);
	  }

	  @Override
	  public void putTracks(Track entity) {


	  }
	}
