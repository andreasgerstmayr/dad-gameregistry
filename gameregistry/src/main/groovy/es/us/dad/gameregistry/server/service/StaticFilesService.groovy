package es.us.dad.gameregistry.server.service

import com.darylteo.vertx.promises.groovy.Promise
import org.vertx.groovy.core.Vertx

class StaticFilesService {
    private final String web_root
    private final Vertx vertx
    private final String index_file = "index.html"

    public StaticFilesService(String web_root, Vertx vertx) {
        this.web_root = web_root + File.separator
        this.vertx = vertx

        // we are starting up, we can do sync stuff
        if (!vertx.fileSystem.propsSync(this.web_root).isDirectory())
            throw new FileNotFoundException("web root doesn't exists: " + this.web_root)
    }

    public Promise<String> getSystemPathOf(String webpath) {
        Promise<String> p = new Promise()
        if (webpath.contains(".."))
            p.reject(new IllegalArgumentException("Path contains '..'."))

        final String global_path = web_root + webpath

        // First check if global_path exists
        vertx.fileSystem.exists(global_path, { global_path_exists ->
            if (global_path_exists)
                // Then if it is a directory
                vertx.fileSystem.props(global_path, { asyncprops ->
                    if (asyncprops.isSucceeded()) {
                        if (!asyncprops.getResult().isDirectory())
                        // it is a file, just fullfil
                            p.fulfill(global_path)
                        else {
                            // It is a directory. Look for global_path/index_file
                            String index_global_path = global_path + File.separator + index_file
                            vertx.fileSystem.exists(index_global_path, { index_global_path_exists ->
                                if (index_global_path_exists)
                                    p.fulfill(index_global_path)
                                else
                                    p.reject(new FileNotFoundException("Directory '${global_path}' doesn't have an index file '${index_file}'."))
                            })
                        }
                    } else {
                        p.reject(asyncprops.getCause())
                    }
                })
            else
                p.reject(new FileNotFoundException("File '${global_path}' doesn't exists."))
        })

        return p
    }
}
