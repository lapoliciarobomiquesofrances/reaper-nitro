package me.rickytheracc.reapernitro.util.services;

import me.rickytheracc.reapernitro.Reaper;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ResourceLoaderService {
    public static ArrayList<String> DEVELOPERS = new ArrayList<>();
    public static ArrayList<String> BETA = new ArrayList<>();
    public static ArrayList<String> USER = new ArrayList<>();
    public static ArrayList<Resource> serverResources = new ArrayList<>();

    public static void init() {
        for (Resource r : serverResources) if (!r.isCached()) r.cache(); // Download anything that isn't cached yet
        Reaper.cached.execute(() -> {
            while (mc.world == null) {
                try {Thread.sleep(500);} catch (Exception ignored) {} // Wait for the world to load
            }
            Reaper.log("Loading assets");
            serverResources.forEach(Resource::load);
        });
    }


    public static void bindAssetFromURL(Identifier asset, String url) {
        if (mc.world == null || asset == null || url == null) return;
        Reaper.cached.execute(() -> {
            try {
                var data = NativeImage.read(Http.get(url).sendInputStream());
                mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(data));
            } catch (Exception ignored) {
                //e.printStackTrace();
            }
        });
    }

    public static void bindAssetFromFile(Identifier asset, String fileName) {
        if (mc.world == null || asset == null || fileName == null) return;
        if (!Reaper.USER_ASSETS.exists()) return;
        Reaper.cached.execute(() -> {
            File[] userAssets = Reaper.USER_ASSETS.listFiles();
            if (userAssets == null || userAssets.length < 1) return;
            for (File f : userAssets) {
                String fn = f.getName();
                if (fn.equalsIgnoreCase(fileName) || fn.equalsIgnoreCase(fileName + ".png")) {
                    try {
                        InputStream is = new FileInputStream(f);
                        var rsc = NativeImage.read(is);
                        mc.getTextureManager().registerTexture(asset, new NativeImageBackedTexture(rsc));
                        is.close();
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    // todo finish later too tired
    /*public static void bindRandomAssetFromFile(Identifier asset) {
        if (mc.world == null || asset == null) return;
    }*/


    public static class Resource {
        private final Identifier identifier;
        private final String url;
        private final String name;

        public Resource(Identifier identifier, String url, String name) {
            this.identifier = identifier;
            this.url = url;
            this.name = name;
        }

        public Identifier getIdentifier() { return this.identifier; }
        public String getUrl() { return this.url; }
        public String getName() { return this.name;}


        public String getFileName() {return this.name + ".png";}
        public File getAsFile() {return new File(Reaper.ASSETS, this.getFileName());}
        public boolean isCached() {return this.getAsFile().exists();}

        public void cache() {
            Reaper.cached.execute(() -> {
                try {
                    File outFile = this.getAsFile();
                    if (!outFile.exists()) outFile.createNewFile();
                    InputStream is = Http.get(this.url).sendInputStream();
                    Reaper.log("Downloading asset " + this.name);
                    Reaper.log(outFile.getAbsolutePath());
                    FileUtils.copyInputStreamToFile(is, outFile);
                    is.close();
                } catch (Exception ignored) {
                    Reaper.log("Failed to download asset " + this.name);
                    //e.printStackTrace();
                }
            });
        }

        public void load() {
            Reaper.cached.execute(() -> {
                File asset = this.getAsFile();
                if (asset == null || !asset.exists()) return;
                try {
                    InputStream is = new FileInputStream(asset);
                    var rsc = NativeImage.read(is);
                    mc.getTextureManager().registerTexture(this.getIdentifier(), new NativeImageBackedTexture(rsc));
                    Reaper.log("Loaded asset " + this.name);
                    is.close();
                } catch (Exception ignored) {
                    Reaper.log("Failed to load asset from cache " + this.name);
                    //e.printStackTrace();
                }
            });
        }
    }
}
