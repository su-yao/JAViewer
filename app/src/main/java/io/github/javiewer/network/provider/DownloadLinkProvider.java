package io.github.javiewer.network.provider;

import java.util.List;

import io.github.javiewer.adapter.item.DownloadLink;
import io.github.javiewer.adapter.item.MagnetLink;
import io.github.javiewer.network.BtSearchSite;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Project: JAViewer
 */
public abstract class DownloadLinkProvider {
    public static DownloadLinkProvider getProvider(String name) {
        if (name == null) {
            return null;
        }
        String key = name.toLowerCase().trim();
        BtSearchSite site = BtSearchSite.find(key);
        if (site != null) {
            return new BtSearchLinkProvider(site);
        }
        if ("btso".equals(key)) {
            return new BTSOLinkProvider();
        }
        if ("torrentkitty".equals(key)) {
            return new BtSearchLinkProvider(BtSearchSite.TORRENTKITTY);
        }
        return null;
    }

    public abstract Call<ResponseBody> search(String keyword, int page);

    public abstract List<DownloadLink> parseDownloadLinks(String htmlContent);

    public abstract Call<ResponseBody> get(String url);

    public abstract MagnetLink parseMagnetLink(String htmlContent);
}
