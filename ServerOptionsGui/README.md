# ServerOptionsGui

A Qt GUI to easily browse and tweak a Server options in [SuperCollider](http://supercollider.github.io).

A screenshot of both simple and advanced views:

![ServerOptionsGui screenshot](http://yvanvolochine.com/media/images/ServerOptionsGui2.gif)

## Requirements

- SuperCollider-3.6
- Qt GUI

## Install

Clone this repository and put the whole folder in your `Extensions` directory:

    $ git clone git://github.com/gusano/ServerOptionsGui.git
    $ ln -s ServerOptionsGui ~/.local/share/SuperCollider/Extensions/ServerOptionsGui

## Usage

    s = Server.local;
    s.boot;

    g = ServerOptionsGui(s);

Note that you'll have to reboot the server for some options to be applied.