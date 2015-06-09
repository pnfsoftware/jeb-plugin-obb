package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class ObbPlugin extends AbstractUnitIdentifier{
	private static final int[] OBB_SIG = {(byte) 0x83, (byte) 0x99, (byte) 0x05, (byte) 0x01};
	public static String ID = "obb_plugin";
	public static String OBB_NAME = "obb_file";
	public static String FAT_IMAGE_NAME = "obb_image";

	public ObbPlugin() {
		super(OBB_NAME, 1); // Give ObbPlugin higher priority than FatPlugin to make sure we enter this plugin first
	}

	public boolean identify(byte[] stream, IUnit unit) {
		// Check for obb signature
		return checkBytes(stream, stream.length - OBB_SIG.length, OBB_SIG);
	}

	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
		super.initialize(parent, pm);
		/** Add any necessary property definitions here **/
	}

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit parent) {
		// Parse obb data into object
		ObbData obbData = new ObbData();
		obbData.parseObbFile(data);
		
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

	@Override
	public IUnit reload(IBinaryFrames data, IUnitProcessor processor, IUnit unit) {
		return null;
	}
}
