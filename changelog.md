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
<td align="left">2.3.0</td>
<td align="left">2016-04-12</td>
<td align="left">MG</td>
<td align="left">
<p>Features added:</p>
<ul>
<li>added a method to the `UbuduMap` to convert `(lat,lng)` geographical coordinates to `(x,y)` cartesian coordinates on the map image</li>
<li>added tiles support for indoor location maps</li>
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

