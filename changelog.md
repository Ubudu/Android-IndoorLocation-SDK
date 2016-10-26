# Ubudu Indoor Location SDK for Android change log

<table>
<colgroup>
<col width="12%" />
<col width="14%" />
<col width="16%" />
<col width="56%" />
</colgroup>
<thead>
<tr class="header">
<th align="left">Version</th>
<th align="left">Date</th>
<th align="left">Author</th>
<th align="left">Modifications</th>
</tr>
</thead>
<tbody>

<tr class="odd">
<td align="left">2.6.1</td>
<td align="left">2016-10-26</td>
<td align="left">MG</td>
<td align="left">
<p>Improvements:</p>
<ul>
<li>Refactored some of the initialization info logs to be more informative for developers</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed map data not being updated properly in some cases</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.6.0</td>
<td align="left">2016-10-13</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>API change: from version 2.6.0 the SDK does not fetch non rectified maps from Ubudu BO by default</li>
<li>add static int getVersionCode() API method to UbuduIndoorLocationSDK class</li>
<li>add Integer getLevel() API method to UbuduMap class. So far there was only String getExternalLevel() method available which was not enough in some use cases.</li>
<li>add UbuduPosition getLastKnownPosition() API method to UbuduIndoorLocationManager class</li>
</ul>
<p>Improvements:</p>
<ul>
<li>reduced lag of getting initial position after SDK start and on floor switching</li>
<li>app's delegate gets notified about map being changed to null after 15 seconds of user being outdoor</li>
<li>various fixes and stability improvements</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed UbuduResultListener callbacks for setNamespace method being called not on a main thread</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.5.3</td>
<td align="left">2016-10-03</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added color property to UbuduZone object. This color can be defined in the Ubudu manager platform map creation tool.</li>
</ul>
<p>Improvements:</p>
<ul>
<li>delegate's zonesChanged now never gives null list in the argument</li>
<li>GPS position update has now also closest zone and current zones list if available</li>
<li>Added few debug logs to logcat to help developers debugging integration code</li>
<li>various fixes and stability improvements</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed GPS position not being returned when no beacons are visible after app launch</li>
</ul>
</td>
</tr>


<tr class="odd">
<td align="left">2.5.2</td>
<td align="left">2016-08-26</td>
<td align="left">MG</td>
<td align="left">
<p>Improvements:</p>
<ul>
<li>delegate's zonesChanged now never gives null list in the argument</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed a bug when sometimes there was no switch to GPS after going outside the building</li>
<li>fixed a bug when sometimes there was no back switch to BLE after coming back to the building</li>
<li>fixed a bug of self permissions check being triggered even if device was Android M. This was causing crash if the target app was using google support-v4 lib version older than 23</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.5.1</td>
<td align="left">2016-08-09</td>
<td align="left">MG</td>
<td align="left">
<p>Issues addressed:</p>
<ul>
<li>fixed ANRs happening on Android M when scanning is triggered while app does not have geolocation permissions</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.5.0</td>
<td align="left">2016-07-20</td>
<td align="left">MG</td>
<td align="left">
<p>Features added</p>
<ul>
<li>added boolean isMoving() method to UbuduIndoorLocationManager object to be able to determine if the user is moving or not on the app side</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>improved positioning stability</li>
<li>improved the accuracy of BLE/GPS switching</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.4.4</td>
<td align="left">2016-06-20</td>
<td align="left">MG</td>
<td align="left">
<p>Features added</p>
<ul>
<li>GPS integration</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed an issue of position getting lost sometimes and moving in wrong directions</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.4.3</td>
<td align="left">2016-06-06</td>
<td align="left">MG</td>
<td align="left">
<p>Issues addressed:</p>
<ul>
<li>motion sensors related crash on some Samsung devices with Android 4.3</li>
<li>motion based position update bug causing unstable position changes</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.4.2</td>
<td align="left">2016-06-03</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added API to embed the indoor location data files into the application so no internet connection is needed on first launch</li>
</ul>
</td>
</tr>


<tr class="odd">
<td align="left">2.4.1</td>
<td align="left">2016-05-31</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added a listener interface to a `setNamespace` method so the app is notified about the application data fetching result</li>
<li>added an `ext_level` field to `UbuduMap` object. This field has be set in the Ubudu Manager Platform and is useful for handling the integration of Ubudu Indoor Location SDK with various indoor map display providers</li>
</ul>
<p>Improvements:</p>
<ul>
<li>improved motion data processing by introducing an earth reference frame</li>
<li>bluetooth and motion data fusion mode is now turned on by default</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed indoor positioning randomly switching floors when working with beacons in secured mode</li>
<li>fixed application data fetching</li>
<li>fixed OutOfMemoryError when processing very huge map on some low resources Android devices</li>
<li>fixed last position not fully reseted when floor is changed</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.3.1</td>
<td align="left">2016-04-29</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added POI handling. List of available map POIs is now available via UbuduMap's `getPois()` method</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.3.0</td>
<td align="left">2016-04-12</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added a method to the `UbuduMap` to convert `(lat,lng)` geographical coordinates to `(x,y)` cartesian coordinates on the map image</li>
<li>added tiles support for indoor location maps</li>
<li>added Ubudu application secured mode handling</li>
</ul>
<p>Improvements:</p>
<ul>
<li>improved positioning accuracy by introducing an advanced motion sensors processing based on particle filtering</li>
<li>lowered the motion data sampling frequency to reduce CPU load</li>
</ul>
<p>Issues addressed:</p>
<ul>
<li>fixed SDK not checking for updates on app restart</li>
<li>minor bugs and crashes fixes</li>
</ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.2.3</td>
<td align="left">2016-02-25</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added azimuth updates to the delegate based on device sensors (compass)</li>
</ul>
<p>Improvements:</p>
<ul>
<li>improved the use of path finding algorithm for ommiting the non-navigable areas of the maps</li>
<li>added timestamp to UbuduPositionUpdate object</li>
<li>improved remote content auto-update stability</li></ul>
<p>Issues addressed:</p>
<ul><li>Fixed auto restart after device boot</li>
<li>fixed few crashes</li></ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.1.7</td>
<td align="left">2016-02-17</td>
<td align="left">MG</td>
<td align="left">
<p>Improvements:</p>
<ul><li>removed unnecessary permissions from manifest</li>
<li>improved remote content auto-update</li></ul>
</td>
</tr>

<tr class="odd">
<td align="left">2.1.0</td>
<td align="left">2016-02-15</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul><li>automatic floors switching,</li>
<li>remote content auto-update,</li>
<li>API method allowing to choose if map overlays .png files should be fetched,</li>
<li>API method to choose between using rectified or non-rectified indoor maps.</li></ul>
<p>Improvements:</p>
<ul><li>improved motion filtering to improve positioning stability.</li></ul>
<p>Issues addressed:</p>
<ul><li>improved stability when working with other Ubudu SDKs using beacon monitoring.</li></ul>
</td>
</tr>

<tr class="odd">
<td align="left">1.0.2</td>
<td align="left">2016-01-16</td>
<td align="left">MG</td>
<td align="left">
<p>Issues addressed:</p>
<ul><li>fixed crashes which were happening if the current position was pointed inside all of the zones defined on the map,</li>
<li>fixed a crash happening when the UbuduIndoorLocationDelegate instance has not been set and is null in the UbuduIndoorLocationManager.</li></ul>
</td>
</tr>

<tr class="odd">
<td align="left">1.0.1</td>
<td align="left">2016-01-14</td>
<td align="left">MG</td>
<td align="left">Changed public API pattern so the methods parameters names are understandable.</td>
</tr>

<tr class="odd">
<td align="left">1.0.0</td>
<td align="left">2016-01-05</td>
<td align="left">MG</td>
<td align="left">Created.</td>
</tr>

</tbody>
</table>

