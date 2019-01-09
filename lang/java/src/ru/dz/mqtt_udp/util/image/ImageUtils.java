package ru.dz.mqtt_udp.util.image;

import java.io.InputStream;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * <p>Image utilities - load icon from file/JAR</p>
 * 
 * @author dz
 *
 */
public class ImageUtils {

	public static ImageView getIcon(String iName)
	{
		return getIcon16(iName);
	}
	
	public static ImageView getIcon16(String iName)
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

	
	
	public static java.awt.image.BufferedImage getSwingImage16( String iName )
	{
		return SwingFXUtils.fromFXImage(getImage16(iName),null);
	}

	
	

	
	public static Image getImage16(String iName) {
		return getImage( iName+"16.png" );
	}	
	
	public static Image getImage32(String iName) {
		return getImage( iName+"32.png" );
	}	

	
	
	

	public static Image getImage(String sourcePath) {
		InputStream input = null;
		try {
			Class<?> c = ImageUtils.class;
			
			input = c.getResourceAsStream("../../../../../"+sourcePath); // Filesystem
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