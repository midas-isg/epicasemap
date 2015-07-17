epidemap
====================

Epidemic on map
---------------
This project is to stream epidemic data to overlay on the corresponding location 
on world map.

This project is a Java application based on Play Framework. 
See http://playframework.com.

-------------------------------------------------------------------------------
Instructions
------------

### Development
#### Downloading Source code
```sh
$ git clone <git-repo-url>
```

#### Setup IDE
In the directory containing this file and run to go to *activator shell*:
```sh
$ activator
```

##### For Eclipse IDE
In the *activator shell*, run to generate all files necessary for Eclipse
```activator
[]$ eclipse with-source=true
```

##### Import the project into Eclipse
###### For Eclipse 4.4 (Luna)
1. `Menu:File>Import...` to open import dialog
2. In import dialog, Select `Tree:General>Existing Project into Workspace` and Next.
3. Browse the directory containing this file. 
Then the proejct should be selectable in the Projects area.
4. Make sure to select the project and Finish.

#### Test locally on web
In the directory containing this file, run the below command 
and go to http://localhost:9000.
```sh
$ activator run
```

-------------------------------------------------------------------------------
### Deployment
#### Configuration
1. Dowload source code, See [Downloading Source code](#downloading-source-code).
2. Edit `conf/application_overrides.conf` file
  1. Change the new secret key to the output from `activator playGenerateSecret`
  2. Edit the conf file to fit the project
3. If the port needs to change, 
create an executable script file named `start.sh` with the below content:

> export PORT=9000 # default is 9000. Change to another port number.  
> ./do start


#### Starting application
Start the application by running the below command.
```sh
$ ./do start
```

or the script you just created.
```sh
$ ./start.sh
```

-------------------------------------------------------------------------------
### Upgrading version
1. Shut the running application down by running the below command.
```sh
$ ./do shutdown
```

2. Update new version by running the below command. 
It will checkout all the files tagged with the `NAME-VERSION` format. 
```sh
$ ./do update <VERSION>
```

3. Restart the application. See [Starting Application](#starting-application).

-------------------------------------------------------------------------------
