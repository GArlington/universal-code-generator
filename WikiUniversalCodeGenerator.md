## How do I start using Universal to generate code? ##
To use universal you must be behind the CBE firewall (on a wired connection in Olin Hall). If you are not, then you can not access the code generation server running on morpheus.cbe.cornell.edu. To start the graphical user interface (GUI), open a terminal window and cd to UEditor-v1.0 directory. From the command line execute:

_./StartUniversal.sh_

If this command was successful you should see the translucent Universal splash screen popup.

## What zip version should I download? ##
Because we are alpha and working towards beta status, I would suggest downloading the latest version of the zip archive on the downloads page. Once we get stabilized then we'll mark the releases as stable.

## What platform does Universal run on? ##
Universal is being developed on Mac OS 10.6 Snow Leopard. In theory it should run on older Mac OS versions with updated JVMs (at least 1.5). Linux platforms should also work. The GUI will **not** run on any windows platform. Not because Windows is bad (I'm dying inside) but because of the path  (/ versus \) issues in the code. Yes, we know how to make this dynamic and platform independent, we just didn't do it.

## Nothing works and this software is crap. ##
Relax, Go eat cookie and calm down. This is alpha so expect some bugs. Please report specific GUI issues (exceptions, crashes or system hangs) or new feature requests to our issue page. Remember we can't fix JV's crappy programming if we don't know about issues.