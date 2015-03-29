#bloodhound-gps

![logo](bloodhound/src/main/res/drawable-hdpi/dog.png?raw=true  "Logo")

An experimental Android project to play around with location services, geofencing, maps, sqlite, content provider and backend server integration.

Related Project: https://github.com/alanwgeorge/bloodhound-gps-backend

##Tech Employed
* ContentProvider: access to location and geofence info
* SQLite: persist locations and geofences
* LocationServices.FusedLocationApi: obtain location information
* LocationServices.GeofencingApi: register/unregister geofences
* GoogleMap and SupportMapFragment: display location, location diffs, and create/edit geofences
* ListView and CursorAdaptor: viewing location and location diff info
* Gson: Marshal data to and from server

##Screens

GeoFence add/edit screen.  Picker on bottom right adjusts the fence radius.

![geofence edit](screenshots/GeoFenceEdit.png?raw=true  "GeoFence add/edit screen.  Picker on bottom right adjusts the fence radius.")

Location list.  Tap location to see location on a map.

![location list](screenshots/LocationList.png?raw=true  "Location list.  Tap location to see location on a map.")

Location diff list.  Tap location diff to see both starting and ending locations on a map.

![location diff list](screenshots/LocationDiffList.png?raw=true  "Location diff list.  Tap location diff to see both starting and ending locations on a map.")

Location diff map.

![location diff list](screenshots/LocationDiffMap.png?raw=true  "Location diff map.")
