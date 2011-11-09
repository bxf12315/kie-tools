package org.jboss.errai.bus.server.service.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.Vfs.File;

import com.google.common.collect.AbstractIterator;

/**
 * Implementation of {@link Vfs.Dir} to support JBoss VFS for proper classpath scanning on JBoss AS.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class JBossVFSDIr implements Vfs.Dir {
  private VirtualFile virtualFile;

  public JBossVFSDIr(URL url) {
    try {
      Object content = url.getContent();
      if (content instanceof VirtualFile) {
        virtualFile = (VirtualFile) content;
      }
      else {
        throw new IllegalArgumentException(
            "URL content is not a JBoss VFS VirtualFile. Type is: " +
                (content == null ? "null" : content.getClass().getName()));
      }
    }
    catch (IOException e) {
      throw new RuntimeException("could not instantiate VFS directory", e);
    }
  }

  @Override
  public String getPath() {
    return virtualFile.getPathName();
  }

  @Override
  public Iterable<Vfs.File> getFiles() {
    return new Iterable<Vfs.File>() {
      @Override
      public Iterator<Vfs.File> iterator() {
        final List<VirtualFile> toVisit = new ArrayList<VirtualFile>(virtualFile.getChildren());

        return new AbstractIterator<Vfs.File>() {

          @Override
          protected File computeNext() {
            while (!toVisit.isEmpty()) {
              final VirtualFile nextFile = toVisit.remove(toVisit.size() - 1);
              if (nextFile.isDirectory()) {
                toVisit.addAll(nextFile.getChildren());
                continue;
              }
              return new Vfs.File() {
                @Override
                public String getName() {
                  return nextFile.getName();
                }

                @Override
                public String getRelativePath() {
                  return nextFile.getPathName();
                }

                @Override
                public String getFullPath() {
                  return nextFile.getPathName();
                }

                @Override
                public InputStream openInputStream() throws IOException {
                  return nextFile.openStream();
                }
              };
            }
            return endOfData();
          }
        };
      }
    };
  }

  @Override
  public void close() {
  }
}
