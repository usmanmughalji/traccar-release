package org.traccar.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.traccar.model.Device;
import org.traccar.session.ConnectionManager;
import org.traccar.session.DeviceSession;
import org.traccar.session.cache.CacheManager;

@Path("custom")
public class CustomDeviceResource {

    private final ConnectionManager connectionManager;
    private final CacheManager cacheManager;

    @Inject
    public CustomDeviceResource(ConnectionManager connectionManager, CacheManager cacheManager) {
        this.connectionManager = connectionManager;
        this.cacheManager = cacheManager;
    }

    @DELETE
    @Path("devices/{deviceId}/disconnect/{userId}")
    public Response disconnectDevice(@PathParam("deviceId") long deviceId) {
        // Step 1: Check if the device session exists
        DeviceSession deviceSession = connectionManager.getDeviceSession(deviceId);
        if (deviceSession == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Device not found or not connected").build();
        }

        // Step 2: Disconnect the device
        connectionManager.deviceDisconnected(deviceSession.getChannel(), true);

        // Step 3: Update the device status to offline
        connectionManager.updateDevice(deviceId, Device.STATUS_OFFLINE, null);

        // Step 4: Invalidate the cache for the device
        cacheManager.removeDevice(deviceId, deviceSession.getConnectionKey());

        return Response.noContent().build();
    }
}
