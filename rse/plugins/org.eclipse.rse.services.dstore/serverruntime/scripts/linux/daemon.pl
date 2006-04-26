#!/usr/bin/perl -w

 
$port = "4035";
$helpFlag = "-h"; 
 
if (defined($ARGV[0]))
{	
	$port = $ARGV[0];
}


$isHelp = $helpFlag cmp $port; 
if ($isHelp == 0)
{
   print("command usage:\n");
   print("daemon.linux [<port> | <low port>-<high port>]\n");    
 0;
}
else
{
	$trace = $ENV{DSTORE_TRACING_ON};
	$user=`whoami`; chomp($user); 
	$match = $user cmp "root";

	if ($match != 0)
	{
	    print("To run the server daemon, you must be root\n");
	    0;
	}
	else
	{
	    $dir= $ENV{PWD};
	    $plugins_dir=$dir;

	    $ENV{A_PLUGIN_PATH}="$plugins_dir/";
    
	    $oldClasspath = $ENV{CLASSPATH};

		$ENV{"CLASSPATH"}="$plugins_dir:$plugins_dir/dstore_extra_server.jar:$plugins_dir/dstore_core.jar:$plugins_dir/dstore_miners.jar:$plugins_dir/clientserver.jar:$oldClasspath";

	    system("java -DA_PLUGIN_PATH=\$A_PLUGIN_PATH -DDSTORE_TRACING_ON=$trace org.eclipse.dstore.core.server.ServerLauncher $port");
	  }
}
