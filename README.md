# Android-IndoorLocation-SDK - version 1.0.1

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

	compile('com.ubudu.indoorlocation:ubudu-indoor-location-sdk:1.0.1@aar') {
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
	
To use indoor location a map must be provided. Map it must be first created in the online Ubudu manager platform. To create and configure a map:

-   go on the Ubudu manager platform,
   
-   select a venue in Venues & indoor maps,
   
-	add new venue or go to the details of one of available venues,

-	click on the Maps button and click Add,

-	configure and save all the map's information.

Once the map creation process is completed the uuid (key) of the map is used by the mobile application. To download the map in the application its uuid must be given as an argument to the following method of `UbuduIndoorLocationManager`, for example:

	mIndoorLocalizationManager.loadMapWithKey("0c6e343044640133cedd1ad861f40fb6");

After calling the method above the map's data will be automatically downloaded from the Ubudu manager platform at the SDK start. To start the Indoor Location `start()` must be called:

	mIndoorLocationManager.start();

There are two callback methods in the delegate's interface that handle `start()` result:

	void startSucceed();
	void startFailed();
	
`startFailed()` is called when Indoor Location could not be started because of the network connection problems or corrupted map json file. `startSucceed()` is called when Indoor Location has been started successfully. Location events will be automatically passed to proper the delegate's methods. 

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
</tbody>
</table>

## Authors:

-   MG: Michal Gasztold \<<michal.gasztold@ubudu.biz>\>

## Legal status:

Copyright ©2016 Ubudu SAS, All right reserved.
