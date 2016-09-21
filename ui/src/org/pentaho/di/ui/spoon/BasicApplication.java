package org.pentaho.di.ui.spoon;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.ui.job.dialog.JobDialogPluginType;
import org.pentaho.di.ui.repository.dialog.RepositoryDialogInterface;
import org.pentaho.di.ui.repository.dialog.RepositoryRevisionBrowserDialogInterface;
import org.pentaho.di.ui.trans.dialog.TransDialogPluginType;


public class BasicApplication implements ApplicationConfiguration {

  public void configure( Application application ) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Future<KettleException> pluginRegistryFuture = executor.submit( new Callable<KettleException>() {

      @Override
      public KettleException call() throws Exception {
        registerUIPluginObjectTypes();

        KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SPOON );
        try {
          KettleEnvironment.init( false );
        } catch ( KettleException e ) {
          return e;
        }

        return null;
      }
    } );
    KettleException registryException;
    try {
      registryException = pluginRegistryFuture.get();
      if ( registryException != null ) {
        throw registryException;
      }
    } catch ( Throwable t ) {
      // avoid calls to Messages i18n method getString() in this block
      // We do this to (hopefully) also catch Out of Memory Exceptions
      //
      t.printStackTrace();
    }

    Map<String, String> properties = new HashMap<String, String>();
    properties.put( WebClient.PAGE_TITLE, "Spoon" );
    application.addEntryPoint( "/", Spoon.class, properties );
    application.setOperationMode( Application.OperationMode.SWT_COMPATIBILITY );
  }

  private static void registerUIPluginObjectTypes() {
    RepositoryPluginType.getInstance()
                        .addObjectType( RepositoryRevisionBrowserDialogInterface.class, "version-browser-classname" );
    RepositoryPluginType.getInstance().addObjectType( RepositoryDialogInterface.class, "dialog-classname" );

    PluginRegistry.addPluginType( SpoonPluginType.getInstance() );

    SpoonPluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/repositories", false, true ) );

    LifecyclePluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/spoon", false, true ) );
    LifecyclePluginType.getInstance().getPluginFolders().add( new PluginFolder( "plugins/repositories", false, true ) );

    PluginRegistry.addPluginType( JobDialogPluginType.getInstance() );
    PluginRegistry.addPluginType( TransDialogPluginType.getInstance() );
  }
}
