package com.pnf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.IO;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import com.pnfsoftware.jeb.util.logging.ILogger;

public class ObbPlugin extends AbstractUnitIdentifier{
	public static final ILogger LOG = GlobalLog.getLogger(ObbPlugin.class);
	private static final int[] OBB_SIG = {(byte) 0x83, (byte) 0x99, (byte) 0x05, (byte) 0x01};
	public static String ID = "obb_plugin";
	public static String OBB_NAME = "obb_file";
	public static String FAT_IMAGE_NAME = "obb_image";

	public ObbPlugin() {
		super(OBB_NAME, 1); // Give ObbPlugin higher priority than FatPlugin to make sure we enter this plugin first
	}

	public boolean canIdentify(IInput stream, IUnitCreator unit) {
		// Check for obb signature
		try (SeekableByteChannel ch = stream.getChannel()){
			ch.position(ch.size() - OBB_SIG.length);
			ByteBuffer buff = ByteBuffer.allocate(OBB_SIG.length);
			ch.read(buff);
			return checkBytes(buff, 0, OBB_SIG);
		} catch (IOException e) {
			return false;
		}
	}

	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
		super.initialize(parent);
		/** Add any necessary property definitions here **/
	}

	@Override
	public IUnit prepare(String name, IInput data, IUnitProcessor processor, IUnitCreator parent) {
		// Parse obb data into object
		ObbData obbData = new ObbData();
		byte[] bytes = null;

		try(InputStream stream = data.getStream()){
			bytes = IO.readInputStream(stream);
		}catch(IOException e){
		}

		obbData.parseObbFile(bytes);

		// Create IUnit of type ObbUnit to delgate processing
		ObbUnit obbUnit = new ObbUnit(obbData, name, data, processor, parent, pdm);
		obbUnit.process();

		// Return the newly created ObbUnit
		return obbUnit;
	}

	@Override
	public PluginInformation getPluginInformation() {
		return new PluginInformation("Android OBB Plugin", "", "1.0", "PNF Software");
	}
}
