import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class ResourceHelper {
    public static File getResourceAsFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        try {
            // toURI instead of getPath to handle spaces.
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static InputStream getResource(@NotNull String filename) {
        try {
            URL url = ResourceHelper.class.getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }
}
