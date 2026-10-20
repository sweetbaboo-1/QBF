package org.peartcraft;

import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;
import java.util.ArrayList;
import java.util.List;

public class XaeroMapWaypointCreator implements WayPointCreator {
    @Override
    public List<String> createWaypoints(List<SchematicBlockPos> positions, List<String> blocks, ConfigData data) {
        int x = Math.min(data.x1(), data.x2());
        int y = Math.min(data.y1(), data.y2());
        int z = Math.min(data.z1(), data.z2());

        List<String> waypoints = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            SchematicBlockPos pos = positions.get(i);
            waypoints.add(String.format("waypoint:%d:UB:%d:%d:%d:0:false:0:gui.xaero_default:false:0:0:true",
                    i, x + pos.x, y + pos.y, z + pos.z));
        }
        return waypoints;
    }
}
