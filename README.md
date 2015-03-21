## Download all quarks

Quarks are packages of [SuperCollider](https://github.com/supercollider/supercollider) code containing classes, extension methods, documentation and server UGen plugins. The Quarks class manages downloading these packages and installing or uninstalling them.

The recommended way to use Quarks is to make sure that you have git installed and then to use the Quarks interface.

Installing git is quite simple:

http://git-scm.com/


As an alternative, this repository collects all of the community contributed Super Collider Quarks and allows them all to be downloaded.


## Download and Install all quarks

Do NOT clone or fork this repository.

Download the latest release from here:

https://github.com/supercollider-quarks/downloaded-quarks/releases

Unpack it and move it to:

    # OS X
    ~/Library/Application Support/SuperCollider/downloaded-quarks

Restart SuperCollider if its running. The Quarks interface will now see these Quarks and allow you to browse and install them.


## Using Quarks

See the SuperCollider help file `Using Quarks` for a full tutorial.

You can install Quarks using the interface:

```supercollider
Quarks.gui
```


## Making a new release (for release managers)

We will make new releases of this package every once in a while, keeping the versions synchronized with SuperCollider's releases.

The releases here should be against stable SuperCollider releases.  Users that want to select specific versions or to checkout Quarks that depend on recent changes to SuperCollider should be using git and the normal Quarks system.

### Release process

In terminal run the python script:

```shell
    python update.py
```

This will clone, update and/or checkout tags as needed. If a repository URL changes then it will remove the previous folder and clone the new one in its place.

The update script will `git add` the changes but will not commit them.

Commit your changes:

    git commit -m "updated quarks for release 3.8.0" -a

And submit a pull request.

Once a commit is tagged it will appear on the download page as a release.
