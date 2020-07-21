## A tool that make your apk debuggable for Charles/Fiddler with Android 6.0

## requirement
- jdk
- make sure `jarsigner` works
- Fiddler/Charles

## How to use
we can use it in GUI,command line,library  
download latest [release](https://github.com/iamyours/ApkCrack/releases) version.

### GUI
```sh
java -jar ApkCrackUI.jar
```

### command line use
config file in `config/config.properties`
- add apk path,out apk path
- add your sign file(or you can use test.jks)
- add your certFile that export from Charles/Fiddler。

``` sh
java -jar ApkCrack.jar
```

### library
add ApkCrack.jar in your project.
``` java
apkCrack.setApkFile(apkFile);
apkCrack.setCertFile(certFile);
apkCrack.setStoreFile(storeFile);
apkCrack.setStorePassword(storePassword);
apkCrack.setKeyAlias(keyAlias);
apkCrack.setKeyPassword(keyPassword);
apkCrack.start();
```

## export cert file from Charles
open Charles,then `Help` -> `SSL Proxying` -> `Save Charles Root Certificate...` -> `change Format to Binary certificate(.cer)`-> `Save` 
![](img/charles1.png) 

![](img/charles2.png)
## export cert file from Fiddler
![](img/fiddler1.png)

![](img/fiddler2.png)

## ApkCrack for mac
![](img/mac1.png)

![](img/mac2.png)

![](img/mac3.png)

![](img/charles3.png)

## ApkCrack for windows
![](img/win1.png)

![](img/win2.png)

![](img/fiddler3.png)




