package com.ubudu.gmaps.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.ByteArrayOutputStream;

/**
 * <p>Google Maps Android V2 tile overlay provider for cached URL tiles. Caches in memory and/or on disk.</p>
 * <p>Uses the famous <a href="https://github.com/nostra13/Android-Universal-Image-Loader/">Android Universal Image Loader</a> library for downloading and caching.</p>
 * <p>Usage example:<pre>
 googleMap.addTileOverlay(new CachingUrlTileProvider(this, 256, 256) {
@Override
public String getTileUrl(int x, int y, int z) {
return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png",x,y,z);
}
}.createTileOverlayOptions());
 * </pre></p>
 */
public abstract class CachingUrlTileProvider implements TileProvider {

    private final int mTileWidth;
    private final int mTileHeight;
    private final DisplayImageOptions mOptions;

    public CachingUrlTileProvider(Context ctx, int mTileWidth, int mTileHeight) {
        this.mTileWidth = mTileWidth;
        this.mTileHeight = mTileHeight;

        // if ImageLoader has not been instantiated by parent application yet
        if (!ImageLoader.getInstance().isInited()) {
            // Create global configuration and initialize ImageLoader with this config
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx).build();
            ImageLoader.getInstance().init(config);
        }

        // init ImageLoader display options
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true);
        setDisplayImageOptions(builder);
        mOptions = builder.build();
    }

    @Override
    public Tile getTile(int x, int y, int z) {
        byte[] tileImage = getTileImage(x, y, z);
        if (tileImage != null) {
            return new Tile(mTileWidth / 2, mTileHeight / 2, tileImage);
        }
        return NO_TILE;
    }

    /**
     * Synchronously loads the requested Tile image either from cache or from the web.</p>
     * Background threading/pooling is done by the google maps api so we can do it all synchronously.
     *
     * @param x x coordinate of the tile
     * @param y y coordinate of the tile
     * @param z the zoom level
     * @return byte data of the image or <i>null</i> if the image could not be loaded.
     */
    private byte[] getTileImage(int x, int y, int z) {
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(getTileUrl(x, y, z), mOptions);
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Creates a new TileOverlayOptions object to be used with {@link com.google.android.gms.maps.GoogleMap#addTileOverlay(TileOverlayOptions) map.addTileOverlay()}
     * @return a TileOverlayOptions with this object set as a tile provider plus fadeIn set to false
     */
    public TileOverlayOptions createTileOverlayOptions() {
        TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(this);

        // set fadeIn to false for all GMS versions that support it
        try {
            Class.forName("com.google.android.gms.maps.model.TileOverlayOptions")
                    .getMethod("fadeIn", boolean.class)
                    .invoke(tileOverlayOptions, false);
        } catch (Exception e) {
        }

        return tileOverlayOptions;
    }

    /**
     * Allows you to set additional ImageLoader display options.<p/>
     * See https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Display-Options for possible options.<p/>
     * Disabling the cache option DOES NOT MAKE SENSE.
     *
     * @param optionsBuilder options builder for setting de downloader settings
     */
    protected void setDisplayImageOptions(DisplayImageOptions.Builder optionsBuilder) {

    }

    /**
     * Return the url to your tiles. For example:
     * <pre>
     public String getTileUrl(int x, int y, int z) {
     return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png",x,y,z);
     }
     </pre>
     * See <a href="http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames</a> for more details
     *
     * @param x x coordinate of the tile
     * @param y y coordinate of the tile
     * @param z the zoom level
     * @return the url to the tile specified by the parameters
     */
    public abstract String getTileUrl(int x, int y, int z);
}