/*******************************************************************************
 * Copyright (c) 2015 PNF Software, Inc.
 *
 *     https://www.pnfsoftware.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.pnf.plugin.obb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.pnfsoftware.jeb.core.IUnitCreator;
import com.pnfsoftware.jeb.core.events.J;
import com.pnfsoftware.jeb.core.events.JebEvent;
import com.pnfsoftware.jeb.core.input.BytesInput;
import com.pnfsoftware.jeb.core.input.IInput;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;
import com.pnfsoftware.jeb.util.IO;

public class ObbUnit extends AbstractBinaryUnit {
    private ObbData image;

    public ObbUnit(ObbData image, String name, IInput data, IUnitProcessor unitProcessor, IUnitCreator parent,
            IPropertyDefinitionManager pdm) {
        super(null, data, ObbPlugin.ID, name, unitProcessor, parent, pdm);
        this.image = image;
    }

    // Override superclass getDescription to show Obb-specific information
    public String getDescription() {

        String[] keys = ObbData.DATA_KEYS;
        Map<String, String> data = image.getData();

        // First populate description variable with superclass description
        StringBuffer desc = new StringBuffer(super.getDescription() + "\n\n");

        // Now add OBB specific information
        desc.append("Properties:\n");
        String tab = " - ";

        // Append all key-value pairs to the description received from the
        // superclass
        for(String s : keys) {
            desc.append(tab + s + " := " + data.get(s) + "\n");
        }

        return desc.toString();
    }

    public boolean process() {
        // Retrieve raw bytes passed to this unit
        byte[] data = null;

        try(InputStream stream = getInput().getStream()) {
            data = IO.readInputStream(stream);
        }
        catch(IOException e) {
        }

        // Remove the footer obb data to prevent UnitProcessor from creating an
        // ObbUnit again
        ObbData.removeFooter(data);

        // Call unit processor on modified data (will return a FAT unit)
        IUnit fatChildUnit = getUnitProcessor().process(ObbPlugin.FAT_IMAGE_NAME, new BytesInput(data), this);

        // Add new FAT unit to this unit's list of children
        this.getChildren().add(fatChildUnit);
        notifyListeners(new JebEvent(J.UnitChange));

        return true;
    }
}
