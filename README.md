# SoochowOraWIFIPW
Python3 and Java Implementations to Retrieve Clear Guest WIFI Password

This is to get Oracle Suzhou wifi(clear guest) password.<br/>
And the script has to be run in Oracle Internal network.<br/>
These scripts are for convenience, so should not be used for commerce.<br/>
For own security, my sso id and password have been cleared. They have to be re-set before using.

###For Python3
To use getFileSSORequests.py, you have to setup [Requests](http://www.python-requests.org/en/latest/)<br/>
To use wifipwqr.py, you have to setup [qrcode](https://pypi.python.org/pypi/qrcode/), which will in turn have dependncy on [Pillow](https://pypi.python.org/pypi/Pillow/2.9.0)

###For JavaImpl
It is better to use [gradle](http://gradle.org/) build project and deal with library dependencies.<br/>
Or manually download httpclient, log4j, zxing, javamail, junit and configure the library dependencies.<br/>

Then initialize SSOAuthRespository sso username and password, also in Main, set up mail to addresses.
