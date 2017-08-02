package de.bsvrz.sys.startstopp.api.jsonschema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Util {
	
	public static Object cloneObject( Serializable src ) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
			outputStream.writeObject(src);

			ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(byteStream.toByteArray()));
			return input.readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new IllegalStateException("Duplizieren von " + src + " ist nicht möglich!", e);
		} 
	}
}
