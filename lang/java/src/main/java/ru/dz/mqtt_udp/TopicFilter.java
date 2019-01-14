package ru.dz.mqtt_udp;

import java.util.function.Predicate;

/**
 * 
 * @author dz
 *
 */
public class TopicFilter implements Predicate<String> {

	private String filter;

	public TopicFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public boolean test(String topicName) {
		
		int tc = 0;
		int fc = 0;
		
		while(true)
		{
			// begin of path part
			
			if( filter.charAt(fc) == '+')
			{
				fc++; // eat +
				// matches one path part, skip all up to / or end in topic
				while( (tc < topicName.length()) && (topicName.charAt(tc) != '/') )
					tc++; // eat all non slash
				
				// now either both have /, or both at end
				
				// both finished
				if( (tc == topicName.length()) && ( fc == filter.length() ) )
					return true;

				// one finished, other not
				if( (tc == topicName.length()) != ( fc == filter.length() ) )
					return false;
				
				// both continue
				if( (topicName.charAt(tc) == '/') && (filter.charAt(fc) == '/') )
				{
					tc++; fc++;
					continue; // path part eaten
				}
				// one of them is not '/' ?
				return false;
			}
			
			// TODO check it to be at end?
			// we came to # in filter, done
			if( filter.charAt(fc) == '#')
				return true;

			// check parts to be equal
			while(true)
			{
				// both finished
				if( (tc == topicName.length()) && ( fc == filter.length() ) )
					return true;

				// one finished
				if( (tc == topicName.length()) || ( fc == filter.length() ) )
					return false;

				// both continue
				if( (topicName.charAt(tc) == '/') && (filter.charAt(fc) == '/') )
				{
					tc++; fc++;
					break; // path part eaten
				}
				// both continue

				if( topicName.charAt(tc) != filter.charAt(fc) )
				{
					return false;
				}

				// continue
				tc++; fc++;				
			}
			
			
		}
		
	}

	
	
	
}
