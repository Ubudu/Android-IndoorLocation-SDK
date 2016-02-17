# Android-IndoorLocation-SDK - version 2.1.0

## Ubudu Indoor Location SDK for Android

For information on pricing, features, examples and our fantastic iBeacon compatible beacons please check our web-site
[http://www.ubudu.com](http://www.ubudu.com). It is totally free to develop with Ubudu SDKs and we only charge usage... above a certain threshold.

Indoor location provides a solution for mobile devices to estimate their position within an indoor venue. The position is computed in real time and then referenced to the map of the venue. The computation is based on the signal broadcasts received from beacons placed inside the venue. Users create their maps in the ubudu Manager and attach it to a particular venue.

## Api reference
[http://www.ubudu.com/docs/android/indoor_location_sdk/index.html](http://www.ubudu.com/docs/android/indoor_location_sdk/index.html)

## Docs

More docs about using the Indoor Location SDK can be found in the Ubudu Knowledge Base:

[http://community.ubudu.com/help/kb/indoor-location](http://community.ubudu.com/help/kb/indoor-location)

## Installing

To use the library in an Android Studio project simply add:

	compile('com.ubudu.indoorlocation:ubudu-indoor-location-sdk:2.1.0@aar') {
        transitive = true }

to your project dependencies and run gradle build.

A jar file of the SDK is also available in the `/Ubudu-IndoorLocation-SDK` directory of this repository. To use it in your project (e.g. in Eclipse IDE) drop the jar file into the `libs` folder and configure the Java build path to include the library.

## Android application integration

Indoor Location is available inside the Ubudu SDK. Before using it the SDK must be instantiated:

	UbuduIndoorLocationSDK mSdk = UbuduIndoorLocationSDK.getSharedInstance(getApplicationContext());
	
Indoor Location uses the delegate pattern to communicate with the application. Delegate class must implement `com.ubudu.indoorlocation.UbuduIndoorLocationDelegate` interface which defines all the events that must be handled by the application:

	public class MyIndoorLocationDelegate implements UbuduIndoorLocationDelegate{ ... }
	
The `UbuduIndoorLocationManager` instance is available after `UbuduIndoorLocationSDK` initialization:
	
	MyIndoorLocationDelegate myIndoorLocationDelegate = new MyIndoorLocationDelegate(getApplicationContext());
	UbuduIndoorLocationManager mIndoorLocationManager = mSdk.getIndoorLocationManager();
	
The delegate object must be then passed to the indoor location manager:

	mIndoorLocationManager.setIndoorLocationDelegate(mIndoorLocationDelegate);
	
To use indoor location an Ubudu Application must be first defined in the Manager Platform. The namespace uid must be then provided at the SDK initialization stage. To create an Indoor Location application please do the following:

- 	go to the Ubudu Manager Platform ([http://manager.ubudu.com]()),
   
-   open details of one of the available venues (or create a new one) in `Venues & indoor maps ` section,

-	click on the `Maps` button and click `Add`,

-	configure and save all the map's information according to the map creation tool,

- 	when your maps are ready go to the `Applications` section,

-	choose one of the applications and edit it,

-	go to the bottom of the edit page till you see `Indoor Location venues` section where you can add venues that have been created on your account to the application,

-	press `Update Application` button to save your changes.

Once the application is ready its uid (namespace) is used by the mobile SDK to fetch all data and start indoor positioning. To plug the application to the Indoor Location SDK within your mobile app please to the following: 

	mSdk.setNamespace("1843291458ae318c504ab93bbd2cdd68a9002cde");

After calling the method above the application's data will be automatically downloaded from the Ubudu Manager Platform. To start the Indoor Location `start()` must be called:

	mIndoorLocationManager.start(new UbuduStartCallback() {
	
                @Override
                public void success() {
                    // SDK started
                }

                @Override
                public void failure() {
                    // SDK start failure
                }

                @Override
                public void restartedAfterContentAutoUpdated() {
                    // SDK restarted after remote data auto-update
                }
                
            });
            	
`failure()` callback is called when Indoor Location could not be started because of the network connection problems or corrupted map json file. `success()` callback is called when Indoor Location has been started successfully. Location events will be automatically passed to proper the delegate's methods. 

To stop indoor location simply call:

	mIndoorLocationManager.stop();
	
This method stops the bluetooth monitoring related to Indoor Location.

## Modifications

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
<td align="left">1.0.0</td>
<td align="left">2016-01-05</td>
<td align="left">MG</td>
<td align="left">Created.</td>
</tr>
<tr class="odd">
<td align="left">1.0.1</td>
<td align="left">2016-01-14</td>
<td align="left">MG</td>
<td align="left">Changed public API pattern so the methods parameters names are understandable.</td>
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
</tbody>
</table>

## Authors:

-   MG: Michal Gasztold \<<michal.gasztold@ubudu.biz>\>

## Legal status:

Copyright ©2016 Ubudu SAS, All right reserved.
