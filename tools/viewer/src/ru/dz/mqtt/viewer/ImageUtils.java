package ru.dz.mqtt.viewer;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
/*
// TODO use ru.dz.mqtt_udp.util.image ImageUtils instead
@Deprecated
public class ImageUtils {

	public static ImageView getIcon(String iName)
	{
		//return getImageView( iName+"24.png" );
		return getImageView( iName+"16.png" );
	}

	public static ImageView getIcon32(String iName)
	{
		return getImageView( iName+"32.png" );
	}
	
	
	public static ImageView getImageView(String sourcePath)
	{
		Image newImage = ImageUtils.getImage(sourcePath);
		return new ImageView(newImage);		
	}
	
	// sourcePath: /org/o7planning/javafx/icon/java-16.png
	public static Image getImage(String sourcePath) {
		InputStream input = null;
		try {
			Class<?> c = ImageUtils.class;
			
			input = c.getResourceAsStream("../../../../"+sourcePath); // Filesystem
			if( input == null )
				input = c.getResourceAsStream("/"+sourcePath); // JAR
			if( input == null )
			{
				System.err.println("Image not found: "+sourcePath);
				return null;
			}
			Image img = new Image(input);
			return img;
		} finally {
			closeQuietly(input);
		}

	}

	private static void closeQuietly(InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {

		}
	}
}

*/
