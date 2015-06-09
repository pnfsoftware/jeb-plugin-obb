package com.pnf;

import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.properties.IPropertyManager;
import com.pnfsoftware.jeb.core.units.AbstractUnitIdentifier;
import com.pnfsoftware.jeb.core.units.IBinaryFrames;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class ObbPlugin extends AbstractUnitIdentifier{	
	private ObbData obbData;

	public ObbPlugin() {
		super(PluginData.ID, 0);
	}

	public boolean identify(byte[] stream, IUnit unit) {
		// Create obbData object to attempt to parse stream
		obbData = new ObbData();

		// identification success happens when parsing success occurs
		return obbData.parseObbFile(stream);
	}

	public void initialize(IPropertyDefinitionManager parent, IPropertyManager pm) {
		super.initialize(parent, pm);
		/** Add any necessary property definitions here **/
	}

	@Override
	public IUnit prepare(String name, byte[] data, IUnitProcessor processor, IUnit parent) {
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
