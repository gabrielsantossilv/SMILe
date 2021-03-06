package newsletter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojo.Album;
import pojo.AlbumPojo;
import pojo.ArtistPojo;
import pojo.LocationPojo;

/* ArtistPojo fuer Template und Newsletter Erzeugung (vgl. Artist Data Object)
 * Pro Grabber wird eine Section erzeugt.
 */
public class ArtistPojoProcessor implements Processor {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ArtistPojoProcessor.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message out = exchange.getIn();
		
		
		HashMap<String, Object> bodyMap =  (HashMap<String, Object>) out.getBody();
		
		
		HashMap<String, Object> youtubeMap = (HashMap<String, Object>) bodyMap.get("youtube");
		HashMap<String, Object> twitterMap = (HashMap<String, Object>) bodyMap.get("twitter");
		HashMap<String, Object> lastFmMap = (HashMap<String, Object>) bodyMap.get("lastFM");
		HashMap<String, Object> amazonMap = (HashMap<String, Object>) bodyMap.get("amazon");
		
		// falls keine Events vorhanden sind -> loesche HashMap mit lastFm
		if (lastFmMap != null) {
			lastFmMap.remove("_id");
		}
		
		
		Map<String,Object> headers = exchange.getIn().getHeaders();
		
		/*
		 * General Information: important for ArtistPojo 
		 * artist name, subscriber
		 * */
		ArtistPojo artistPojo = new ArtistPojo((String)headers.get("artist"));
		artistPojo.setSubscriberName((String) headers.get("subscriber"));
	
		/*
		 *  Twitter Section starts here
		 */
		if (twitterMap != null) {
			ArrayList<Object> tweets = new ArrayList<Object>(twitterMap.values());
			artistPojo.setTwitterSection(tweets);
		}
		else {
			ArrayList<Object> tweets = new ArrayList<Object>();
			tweets.add("No tweets available!");
			artistPojo.setTwitterSection(tweets);
		}
		
		/* 
		 * Youtube Section starts here
		 */
		if (youtubeMap != null) {
			artistPojo.setyChannel((String)youtubeMap.get("channel"));
			artistPojo.setyChannelName((String)youtubeMap.get("title"));
			artistPojo.setyPlaylist((String)youtubeMap.get("playlist"));
			artistPojo.setySubscriber((String)youtubeMap.get("subscribers"));
		} else {
			artistPojo.setyPlaylist("No playlist available");
			artistPojo.setyChannel("");
			artistPojo.setyChannelName("");
			artistPojo.setySubscriber("");
		}
		
		
		/*
		 *  LastFM Section starts here
		 */
		if (lastFmMap != null) {
			ArrayList<LocationPojo> locations = new ArrayList<LocationPojo>();
			ArrayList<Entry<String,Object>> lastFMLocations = new ArrayList<Entry<String,Object>>(lastFmMap.entrySet());
			
			for(Entry<String,Object> e:lastFMLocations){
				LocationPojo lp = new LocationPojo(e.getKey()); // Key: LocationName
				
				HashMap<String, Object> hashi = (HashMap<String, Object>) e.getValue(); //LocationName Datum: Webseite
				ArrayList<String> event = new ArrayList<String>();
				
				for(Entry<String,Object> eventEntry:hashi.entrySet()){
					String eventName = eventEntry.getKey();
					
					String eventWeb = (String) eventEntry.getValue();
					
					event.add(eventName + " - " + eventWeb);
				}
				/*	if(event.isEmpty()){
					event.add("No events are upcoming!");
				}*/
				lp.setEvents(event);
				locations.add(lp);
			}
			
			if(locations.isEmpty()){
				LocationPojo lp = new LocationPojo();
				lp.setLocationsEmpty();
				locations.add(lp);
			}
			
			artistPojo.setLastFMSection(locations);
		}
		else
		{
			ArrayList<LocationPojo> locations = new ArrayList<LocationPojo>();
			if(locations.isEmpty()){
				LocationPojo lp = new LocationPojo();
				lp.setLocationsEmpty();
				locations.add(lp);
			}
			artistPojo.setLastFMSection(locations);
		}
		
		
		/*
		 *  Amazon Section starts here
		 */
		if ( amazonMap != null) {
			amazonMap.remove("_id");
		}
		
		if (amazonMap != null) {
			ArrayList<Entry<String, Object>> amazonEntries = new ArrayList<Entry<String, Object>>(
					amazonMap.entrySet());
			ArrayList<AlbumPojo> albenPojo = new ArrayList<AlbumPojo>();

			for (Entry<String, Object> e : amazonEntries) {
				AlbumPojo ap = new AlbumPojo(e.getKey()); // Key: AmazonUid

				HashMap<String, Object> hashi = (HashMap<String, Object>) e.getValue(); // "title = xxx"
				ArrayList<Album> alben = new ArrayList<Album>();

				Album newAlbum = new Album();
				newAlbum.setTitle((String) hashi.get("title"));
				newAlbum.setPrice((String) hashi.get("price"));
				newAlbum.setImageurl((String) hashi.get("imageurl"));
				newAlbum.setPageurl((String) hashi.get("pageurl"));

				alben.add(newAlbum);
				ap.setAlben(alben);
				albenPojo.add(ap);
			}
			artistPojo.setAmazonSection(albenPojo);
		}
		else {
			ArrayList<AlbumPojo> albenPojo = new ArrayList<AlbumPojo>();
			
			if(albenPojo.isEmpty()){
				
				AlbumPojo ap = new AlbumPojo();
				ap.setAlbenEmpty();
				albenPojo.add(ap);
			}
			
			artistPojo.setAmazonSection(albenPojo);
		}
		
		
		
		
		// body setzten (artistPojo) - wichtig fuers Template befuellen
		out.setBody(artistPojo);
		exchange.setIn(out);	
	}

}
