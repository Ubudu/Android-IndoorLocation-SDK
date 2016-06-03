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
